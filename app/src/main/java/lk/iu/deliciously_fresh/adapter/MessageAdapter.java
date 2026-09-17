package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private final Context context;
    private final List<Message> messageList;

    public MessageAdapter(Context context, List<Message> messageList) {
        this.context = context;
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (message.getType() != null && message.getType().equals("direct")) {
            holder.tvTitle.setText("Direct Message");
        } else {
            holder.tvTitle.setText("Broadcast Update");
        }

        holder.tvText.setText(message.getText());

        if (message.getCreatedAt() != null) {
            CharSequence dateString = DateFormat.format("MMM d, yyyy 'at' h:mm a", message.getCreatedAt().toDate());
            holder.tvDate.setText(dateString);
        } else {
            holder.tvDate.setText("Just now");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvText, tvDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_message_title);
            tvText = itemView.findViewById(R.id.tv_message_text);
            tvDate = itemView.findViewById(R.id.tv_message_date);
        }
    }
}
