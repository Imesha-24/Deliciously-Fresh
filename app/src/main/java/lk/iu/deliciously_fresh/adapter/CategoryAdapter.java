package lk.iu.deliciously_fresh.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.List;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.model.Category;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private final Context context;
    private final List<Category> categories;
    private final OnCategoryClickListener listener;

    public CategoryAdapter(Context context, List<Category> categories,
                           OnCategoryClickListener listener) {
        this.context    = context;
        this.categories = categories;
        this.listener   = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context)
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvName.setText(cat.getName());

        ImageLoader.loadWithRadius(context, cat.getImageUrl(), holder.ivImage, 24);

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(cat));
    }

    @Override
    public int getItemCount() { return categories.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView  tvName;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_category_image);
            tvName  = itemView.findViewById(R.id.tv_category_name);
        }
    }
}
