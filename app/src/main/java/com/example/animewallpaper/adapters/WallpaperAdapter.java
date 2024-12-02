package com.example.animewallpaper.adapters;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.animewallpaper.R;
import com.example.animewallpaper.WallpaperActivity;
import com.example.animewallpaper.models.WallpaperModel;

import java.util.List;
import java.util.Random;

public class WallpaperAdapter extends RecyclerView.Adapter<WallpaperAdapter.MyViewHolder> {

    private List<WallpaperModel> wallpaperModelList;
    private Context context;


    public WallpaperAdapter(List<WallpaperModel> wallpaperModelList, Context context) {
        this.wallpaperModelList = wallpaperModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.raw_item_home_wallpaper, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Random random = new Random();

        int color = Color.argb(255,random.nextInt(256), random.nextInt(256), random.nextInt(256));

        Glide.with(context)
                .load(wallpaperModelList.get(position).getImage())
                .timeout(2500)
                .placeholder(new ColorDrawable(color))
                .into(holder.imgWallpaper);

        // set wallpaper on click
        holder.imgWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String id = wallpaperModelList.get(position).getId();

                Intent intent = new Intent(context, WallpaperActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("id", id);
                context.startActivity(intent);

            }
        });

    }

    @Override
    public int getItemCount() {
        return wallpaperModelList.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView imgWallpaper;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            imgWallpaper = itemView.findViewById(R.id.imgWallpaperHome);

        }
    }
}
