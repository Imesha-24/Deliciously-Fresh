package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.OrdersAdapter;

public class OrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private TextView tvOrdersEmpty;

    private final List<OrdersAdapter.OrderSummary> orders = new ArrayList<>();
    private OrdersAdapter adapter;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private ListenerRegistration listenerRegistration;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_orders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvOrders = view.findViewById(R.id.rv_orders);
        tvOrdersEmpty = view.findViewById(R.id.tv_orders_empty);

        rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new OrdersAdapter(requireContext(), orders);
        rvOrders.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to view orders", Toast.LENGTH_SHORT).show();
            tvOrdersEmpty.setVisibility(View.VISIBLE);
            return;
        }

        Query query = db.collection("users")
                .document(user.getUid())
                .collection("orders")
                .orderBy("createdAt", Query.Direction.DESCENDING);

        listenerRegistration = query.addSnapshotListener((snapshots, e) -> {
            if (!isAdded()) return;

            if (e != null) {
                tvOrdersEmpty.setVisibility(View.VISIBLE);
                return;
            }

            orders.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    OrdersAdapter.OrderSummary summary = new OrdersAdapter.OrderSummary();
                    summary.id = doc.getId();
                    summary.orderId = doc.getString("orderId");
                    summary.status = doc.getString("status");
                    summary.paymentNo = doc.getString("paymentNo");

                    Object totalObj = doc.get("total");
                    double total = 0.0;
                    if (totalObj instanceof Number) {
                        total = ((Number) totalObj).doubleValue();
                    }
                    summary.total = total;
                    orders.add(summary);
                }
            }

            adapter.notifyDataSetChanged();
            tvOrdersEmpty.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }
}