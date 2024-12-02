package com.example.animewallpaper.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.animewallpaper.Extra.BorderSpan;
import com.example.animewallpaper.R;
import com.example.animewallpaper.models.Category;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private List<Category> categoryList;
    private Context context;
    private OnCategoryClickListener listener;

    public CategoryAdapter(List<Category> categoryList, Context context, OnCategoryClickListener listener) {
        this.categoryList = categoryList;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);

        holder.categoryTextView.setText(category.getName());

        // Load image using Glide
        String avgColor = category.getAvgColor(); // returns color like "#938584"
        int placeholderColor;

        try {
            placeholderColor = Color.parseColor(avgColor);
        } catch (IllegalArgumentException e) {
            placeholderColor = Color.GRAY;
        }
        Glide.with(context)
                .load(category.getImageUrl())  // Use smaller versions for thumbnails if possible
                .apply(new RequestOptions()
                        .placeholder(new ColorDrawable(placeholderColor))  // Fast placeholder
                        .override(400, 400)  // Resize to a smaller size for faster loading
                        .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original & resized
                        .skipMemoryCache(false)  // Enable memory cache
                )
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Toast.makeText(context, "Image load failed at : " + holder.getAdapterPosition() + " " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(holder.categoryImageView);

        // Set click listener
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category.getName()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImageView;
        TextView categoryTextView;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImageView = itemView.findViewById(R.id.categoryImageView);
            categoryTextView = itemView.findViewById(R.id.categoryTextView);
        }
    }

    // Interface for click listener
    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }
}
