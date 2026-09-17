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
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import java.util.List;
import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.Fruit;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class FruitAdapter extends RecyclerView.Adapter<FruitAdapter.ViewHolder> {

    public interface OnFruitClickListener {
        void onFruitClick(Fruit fruit);
    }

    private final Context             context;
    private final List<Fruit>       fruits;
    private final OnFruitClickListener listener;

    public FruitAdapter(Context context, List<Fruit> fruits,
                        OnFruitClickListener listener) {
        this.context  = context;
        this.fruits   = fruits;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_fruit, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Fruit p = fruits.get(position);

        h.tvName.setText(p.getTitle());
        h.tvDesc.setText(p.getDescription());
        h.tvPrice.setText("LKR " + (int) p.getPrice());
        h.rbRating.setRating(p.getRating());

        if (p.getStockCount() > 10) {
            h.tvStock.setText("In Stock");
            h.tvStock.setBackgroundResource(R.drawable.stock_badge_bg);
        } else if (p.getStockCount() > 0) {
            h.tvStock.setText("Low Stock");
            h.tvStock.setBackgroundResource(R.drawable.stock_badge_bg);
        } else {
            h.tvStock.setText("Out of Stock");
            h.tvStock.setBackgroundResource(R.drawable.stock_badge_bg);
        }

        // Load image — handles both Base64 strings and http URLs
        ImageLoader.load(context, p.getImageUrl(), h.ivImage);

        h.itemView.setOnClickListener(v -> listener.onFruitClick(p));
    }

    @Override
    public int getItemCount() { return fruits.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvName, tvDesc, tvPrice, tvStock;
        RatingBar rbRating;

        ViewHolder(@NonNull View v) {
            super(v);
            ivImage  = v.findViewById(R.id.iv_fruit_image);
            tvName   = v.findViewById(R.id.tv_fruit_name);
            tvDesc   = v.findViewById(R.id.tv_fruit_desc);
            tvPrice  = v.findViewById(R.id.tv_fruit_price);
            tvStock  = v.findViewById(R.id.tv_fruit_stock);
            rbRating = v.findViewById(R.id.rb_fruit_rating);
        }
    }
}
