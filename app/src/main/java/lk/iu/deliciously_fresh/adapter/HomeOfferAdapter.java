package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.graphics.Paint;
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
import lk.iu.deliciously_fresh.model.HomeProduct;
import lk.iu.deliciously_fresh.util.ImageLoader;

/**
 * Separate adapter/layout for "Offers" cards.
 */
public class HomeOfferAdapter extends RecyclerView.Adapter<HomeOfferAdapter.ViewHolder> {

    private final Context context;
    private final List<HomeProduct> items;
    private final HomeProductAdapter.HomeProductActionListener listener;

    public HomeOfferAdapter(Context context,
                              List<HomeProduct> items,
                              HomeProductAdapter.HomeProductActionListener listener) {
        this.context = context;
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_offer_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        HomeProduct p = items.get(position);

        h.tvOfferTitle.setText(p.getTitle() != null ? p.getTitle() : "Offer");

        boolean inStock = p.getStockCount() > 0;
        h.tvOfferAvailability.setText(inStock ? ("Available: " + p.getStockCount()) : "Out of stock");
        h.btnAddToCart.setEnabled(inStock);
        h.btnAddToCart.setAlpha(inStock ? 1f : 0.5f);

        h.rbRating.setRating(p.getRating());
        h.tvOfferRating.setText(String.format(Locale.getDefault(), "%.1f", p.getRating()));

        boolean hasRealOffer = p.isHasOffer()
                && p.getOfferPricePerKg() > 0
                && p.getOfferPricePerKg() < p.getPricePerKg();

        if (hasRealOffer) {
            int off = p.getOfferPercent();
            h.tvDiscount.setVisibility(View.VISIBLE);
            h.tvDiscount.setText(off > 0 ? (off + "% OFF") : "OFFER");

            h.tvOfferPrice.setText(String.format(Locale.getDefault(), "LKR %.2f / 1kg", p.getOfferPricePerKg()));

            h.tvOldPrice.setVisibility(View.VISIBLE);
            h.tvOldPrice.setText(String.format(Locale.getDefault(), "LKR %.2f", p.getPricePerKg()));
            h.tvOldPrice.setPaintFlags(h.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            h.tvDiscount.setVisibility(View.GONE);
            h.tvOfferPrice.setText(String.format(Locale.getDefault(), "LKR %.2f / 1kg", p.getPricePerKg()));
            h.tvOldPrice.setVisibility(View.GONE);
        }

        ImageLoader.load(context, p.getImage(), h.ivOfferImage);

        h.btnView.setOnClickListener(v -> {
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
        ImageView ivOfferImage;
        TextView tvOfferTitle;
        TextView tvDiscount;
        TextView tvOfferPrice;
        TextView tvOldPrice;
        TextView tvOfferAvailability;
        RatingBar rbRating;
        TextView tvOfferRating;
        MaterialButton btnView;
        MaterialButton btnAddToCart;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivOfferImage = itemView.findViewById(R.id.iv_offer_image);
            tvOfferTitle = itemView.findViewById(R.id.tv_offer_title);
            tvDiscount = itemView.findViewById(R.id.tv_offer_discount);
            tvOfferPrice = itemView.findViewById(R.id.tv_offer_price);
            tvOldPrice = itemView.findViewById(R.id.tv_offer_old_price);
            tvOfferAvailability = itemView.findViewById(R.id.tv_offer_availability);
            rbRating = itemView.findViewById(R.id.rb_offer_rating);
            tvOfferRating = itemView.findViewById(R.id.tv_offer_rating_value);
            btnView = itemView.findViewById(R.id.btn_offer_view);
            btnAddToCart = itemView.findViewById(R.id.btn_offer_add_to_cart);
        }
    }
}

