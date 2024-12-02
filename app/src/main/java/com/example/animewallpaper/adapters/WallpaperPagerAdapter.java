
package com.example.animewallpaper.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.example.animewallpaper.R;
import com.example.animewallpaper.WallpaperActivity;
import com.example.animewallpaper.models.Photo;
import com.jsibbold.zoomage.ZoomageView;

import java.util.ArrayList;
import java.util.List;

public class WallpaperPagerAdapter extends RecyclerView.Adapter<WallpaperPagerAdapter.WallpaperViewHolder> {

    private List<String> wallpaperUrls;
    private ArrayList<Photo> photosList;
    private Context context;
    private ZoomageView currentPhotoView;

    public WallpaperPagerAdapter(Context context, List<String> wallpaperUrls) {
        this.context = context;
        this.wallpaperUrls = wallpaperUrls;
        this.photosList = photosList;
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_wallpaper, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        String wallpaperUrl = wallpaperUrls.get(position);

        Glide.with(context)
                .load(wallpaperUrl)
                .placeholder(new ColorDrawable(Color.DKGRAY))
                .into(holder.wallpaperImageView);

        currentPhotoView = holder.wallpaperImageView;

    }

    @Override
    public int getItemCount() {
        return wallpaperUrls.size();
    }

    public ZoomageView getCurrentPhotoView() {
        return currentPhotoView;
    }


    public static class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ZoomageView wallpaperImageView;

        public WallpaperViewHolder(@NonNull View itemView) {
            super(itemView);
            wallpaperImageView = itemView.findViewById(R.id.wallpaperImageView);
        }
    }
}
