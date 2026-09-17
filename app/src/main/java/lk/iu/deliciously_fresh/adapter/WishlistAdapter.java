package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.WishlistItem;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    public interface WishlistListener {
        void onOpen(@NonNull WishlistItem item);
        void onRemove(@NonNull WishlistItem item);
        void onAddToCart(@NonNull WishlistItem item);
    }

    private final Context context;
    private final List<WishlistItem> items;
    private final WishlistListener listener;

    public WishlistAdapter(Context context, List<WishlistItem> items, WishlistListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        WishlistItem item = items.get(position);

        h.tvTitle.setText(item.getTitle() != null ? item.getTitle() : "Fruit");
        h.tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f / 1kg", item.getPrice()));
        h.rbRating.setRating(item.getRating());
        h.tvRating.setText(String.format(Locale.getDefault(), "%.1f", item.getRating()));

        boolean inStock = item.getStockCount() > 0;
        h.tvStock.setText(inStock ? ("In stock: " + item.getStockCount()) : "Out of stock");
        h.tvStock.setTextColor(context.getColor(inStock ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));

        ImageLoader.load(context, item.getImage(), h.ivImage);

        h.btnAddToCart.setEnabled(inStock);
        h.btnAddToCart.setAlpha(inStock ? 1f : 0.5f);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOpen(item);
        });
        h.btnRemove.setOnClickListener(v -> {
            if (listener != null) listener.onRemove(item);
        });
        h.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCart(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvTitle, tvPrice, tvStock, tvRating;
        RatingBar rbRating;
        MaterialButton btnRemove, btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_wishlist_image);
            tvTitle = itemView.findViewById(R.id.tv_wishlist_title);
            tvPrice = itemView.findViewById(R.id.tv_wishlist_price);
            tvStock = itemView.findViewById(R.id.tv_wishlist_stock);
            rbRating = itemView.findViewById(R.id.rb_wishlist_rating);
            tvRating = itemView.findViewById(R.id.tv_wishlist_rating_value);
            btnRemove = itemView.findViewById(R.id.btn_wishlist_remove);
            btnAddToCart = itemView.findViewById(R.id.btn_wishlist_add_to_cart);
        }
    }
}

