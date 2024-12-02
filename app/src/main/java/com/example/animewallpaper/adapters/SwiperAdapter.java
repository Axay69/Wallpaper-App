package com.example.animewallpaper.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.animewallpaper.R;
import com.example.animewallpaper.models.WallpaperModel;

import java.util.List;
import java.util.Random;

public class SwiperAdapter extends RecyclerView.Adapter<SwiperAdapter.MyViewHolder> {

    private List<WallpaperModel> wallpaperModelList;
    private Context context;

    public SwiperAdapter(List<WallpaperModel> wallpaperModelList, Context context) {
        this.wallpaperModelList = wallpaperModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public SwiperAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.swiper_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SwiperAdapter.MyViewHolder holder, int position) {
        Random random = new Random();

        int color = Color.argb(255,random.nextInt(256), random.nextInt(256), random.nextInt(256));

        holder.constraintLayout.setBackgroundColor(color);

        Glide.with(context)
                .load(wallpaperModelList.get(position).getImage())
                .timeout(6500)
                .placeholder(new ColorDrawable(color))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, @Nullable Object model, @NonNull Target<Drawable> target, boolean isFirstResource) {
                        holder.constraintLayout.setBackgroundColor(Color.TRANSPARENT);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(@NonNull Drawable resource, @NonNull Object model, Target<Drawable> target, @NonNull DataSource dataSource, boolean isFirstResource) {
                        holder.constraintLayout.setBackgroundColor(Color.TRANSPARENT);
                        return false;
                    }
                })
                .into(holder.imageView);

    }

    @Override
    public int getItemCount() {
        return wallpaperModelList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private ConstraintLayout constraintLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imgSwiper);
            constraintLayout = itemView.findViewById(R.id.constraintLayoutSwiperItem);
        }
    }
}
