package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.CartItem;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class CartItemsAdapter extends RecyclerView.Adapter<CartItemsAdapter.ViewHolder> {

    public interface CartItemActionListener {
        void onIncrement(@NonNull CartItem item);
        void onDecrement(@NonNull CartItem item);
        void onRemove(@NonNull CartItem item);
    }

    private final Context context;
    private final List<CartItem> items;
    private final boolean editable;
    private final CartItemActionListener listener;

    public CartItemsAdapter(Context context,
                              List<CartItem> items,
                              boolean editable,
                              CartItemActionListener listener) {
        this.context = context;
        this.items = items;
        this.editable = editable;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        CartItem item = items.get(position);

        h.tvTitle.setText(item.getTitle());
        h.tvUnitPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", item.getPrice()));
        double lineTotal = item.getPrice() * item.getQuantity();
        h.tvLineTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", lineTotal));
        h.tvQuantity.setText(String.valueOf(item.getQuantity()));

        ImageLoader.load(context, item.getImage(), h.ivImage);

        h.btnRemove.setVisibility(editable ? View.VISIBLE : View.GONE);

        h.btnMinus.setOnClickListener(v -> {
            if (!editable || listener == null) return;
            listener.onDecrement(item);
        });
        h.btnPlus.setOnClickListener(v -> {
            if (!editable || listener == null) return;
            listener.onIncrement(item);
        });
        h.btnRemove.setOnClickListener(v -> {
            if (!editable || listener == null) return;
            listener.onRemove(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle;
        TextView tvUnitPrice;
        TextView tvLineTotal;
        FrameLayout btnMinus;
        FrameLayout btnPlus;
        TextView tvQuantity;
        MaterialButton btnRemove;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_cart_image);
            tvTitle = itemView.findViewById(R.id.tv_cart_title);
            tvUnitPrice = itemView.findViewById(R.id.tv_cart_unit_price);
            tvLineTotal = itemView.findViewById(R.id.tv_cart_line_total);
            btnMinus = itemView.findViewById(R.id.btn_cart_minus);
            btnPlus = itemView.findViewById(R.id.btn_cart_plus);
            tvQuantity = itemView.findViewById(R.id.tv_cart_quantity);
            btnRemove = itemView.findViewById(R.id.btn_cart_remove);
        }
    }
}

