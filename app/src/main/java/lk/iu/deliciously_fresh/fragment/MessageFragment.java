package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.MessageAdapter;
import lk.iu.deliciously_fresh.model.Message;

public class MessageFragment extends Fragment {

    private RecyclerView rvMessages;
    private ProgressBar progressBar;
    private TextView tvEmpty;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public MessageFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvMessages = view.findViewById(R.id.rv_messages);
        progressBar = view.findViewById(R.id.progress_messages);
        tvEmpty = view.findViewById(R.id.tv_empty_messages);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        rvMessages.setLayoutManager(new LinearLayoutManager(requireContext()));
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(requireContext(), messageList);
        rvMessages.setAdapter(messageAdapter);

        loadMessages();
    }

    private void loadMessages() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Please login to see messages");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        // Fetching messages: broadcast or specific to this user
        db.collection("messages")
          .orderBy("createdAt", Query.Direction.DESCENDING)
          .addSnapshotListener((value, error) -> {
              progressBar.setVisibility(View.GONE);

              if (error != null) {
                  Toast.makeText(requireContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
                  return;
              }

              if (value != null) {
                  messageList.clear();
                  String currentUserId = user.getUid();

                  for (DocumentSnapshot doc : value.getDocuments()) {
                      Message message = doc.toObject(Message.class);
                      if (message != null) {
                          message.setId(doc.getId());
                          
                          // Display if it's broadcast OR specifically directed to the user
                          if ("broadcast".equals(message.getType())) {
                              messageList.add(message);
                          } else if ("direct".equals(message.getType()) && currentUserId.equals(message.getToUserId())) {
                              messageList.add(message);
                          }
                      }
                  }

                  messageAdapter.notifyDataSetChanged();

                  if (messageList.isEmpty()) {
                      tvEmpty.setVisibility(View.VISIBLE);
                  } else {
                      tvEmpty.setVisibility(View.GONE);
                  }
              }
          });
    }
}