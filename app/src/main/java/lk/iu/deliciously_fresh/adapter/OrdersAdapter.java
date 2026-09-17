package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Locale;

import lk.iu.deliciously_fresh.R;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    public static class OrderSummary {
        public String id;
        public String orderId;
        public String status;
        public String paymentNo;
        public double total;
    }

    private final Context context;
    private final List<OrderSummary> orders;

    public OrdersAdapter(Context context, List<OrderSummary> orders) {
        this.context = context;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        OrderSummary order = orders.get(position);
        h.tvOrderPayment.setText(
                "Payment: " + (order.paymentNo != null && !order.paymentNo.isEmpty() ? order.paymentNo : "-"));
        h.tvOrderTotal.setText(
                String.format(Locale.getDefault(), "LKR %.2f", order.total));
        h.tvOrderStatus.setText(
                "Status: " + (order.status != null ? order.status : "-"));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderPayment;
        TextView tvOrderTotal;
        TextView tvOrderStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvOrderPayment = itemView.findViewById(R.id.tv_order_payment);
            tvOrderTotal = itemView.findViewById(R.id.tv_order_total);
            tvOrderStatus = itemView.findViewById(R.id.tv_order_status);
        }
    }
}

