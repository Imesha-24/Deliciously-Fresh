package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.HomeProduct;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class HomeProductAdapter extends RecyclerView.Adapter<HomeProductAdapter.ViewHolder> {

    public interface HomeProductActionListener {
        void onCardClick(@NonNull HomeProduct product);
        void onAddToCartClick(@NonNull HomeProduct product);
    }

    private final Context context;
    private final List<HomeProduct> items;
    private final HomeProductActionListener listener;

    public HomeProductAdapter(Context context, List<HomeProduct> items, HomeProductActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_home_product, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        HomeProduct p = items.get(position);

        h.tvName.setText(p.getTitle() != null ? p.getTitle() : "Product");
        h.rbRating.setRating(p.getRating());
        h.tvRating.setText(String.format(Locale.getDefault(), "%.1f", p.getRating()));

        boolean inStock = p.getStockCount() > 0;
        h.tvAvailability.setText(inStock ? "In stock: " + p.getStockCount() : "Out of stock");
        h.tvAvailability.setTextColor(
                context.getColor(inStock ? android.R.color.holo_green_dark : android.R.color.holo_red_dark)
        );

        if (p.isHasOffer() && p.getOfferPricePerKg() > 0 && p.getOfferPricePerKg() < p.getPricePerKg()) {
            h.tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f / 1kg", p.getOfferPricePerKg()));
            h.tvOldPrice.setVisibility(View.VISIBLE);
            h.tvOldPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", p.getPricePerKg()));
            h.tvOldPrice.setPaintFlags(h.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            h.tvOfferBadge.setVisibility(View.VISIBLE);
            h.tvOfferBadge.setText(p.getOfferPercent() > 0 ? (p.getOfferPercent() + "% OFF") : "OFFER");
        } else {
            h.tvPrice.setText(String.format(Locale.getDefault(), "LKR %.2f / 1kg", p.getPricePerKg()));
            h.tvOldPrice.setVisibility(View.GONE);
            h.tvOfferBadge.setVisibility(View.GONE);
        }

        h.btnAddToCart.setEnabled(inStock);
        h.btnAddToCart.setAlpha(inStock ? 1f : 0.5f);

        ImageLoader.load(context, p.getImage(), h.ivImage);

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCardClick(p);
        });
        h.btnAddToCart.setOnClickListener(v -> {
            if (listener != null) listener.onAddToCartClick(p);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvPrice, tvOldPrice, tvAvailability, tvRating, tvOfferBadge;
        RatingBar rbRating;
        MaterialButton btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_home_product_image);
            tvName = itemView.findViewById(R.id.tv_home_product_name);
            tvPrice = itemView.findViewById(R.id.tv_home_product_price);
            tvOldPrice = itemView.findViewById(R.id.tv_home_product_old_price);
            tvAvailability = itemView.findViewById(R.id.tv_home_product_availability);
            tvRating = itemView.findViewById(R.id.tv_home_product_rating_value);
            tvOfferBadge = itemView.findViewById(R.id.tv_home_product_offer_badge);
            rbRating = itemView.findViewById(R.id.rb_home_product_rating);
            btnAddToCart = itemView.findViewById(R.id.btn_home_add_to_cart);
        }
    }
}

