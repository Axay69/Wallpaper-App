package com.example.animewallpaper.Extra;

import android.app.WallpaperManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;

public class WallpaperChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WallpaperChangeReceiver", "Wallpaper change broadcast received");

        // Fetch the wallpaper URL from Firestore randomly
        WallpaperHelper wallpaperHelper = new WallpaperHelper();
        wallpaperHelper.getWallpaperUrl(context, new WallpaperHelper.WallpaperUrlCallback() {
            @Override
            public void onSuccess(String wallpaperUrl) {
                // Load the wallpaper as a Bitmap
                Glide.with(context)
                        .asBitmap()
                        .load(wallpaperUrl)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                                try {
                                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                                    // Set the wallpaper for the home screen
                                    wallpaperManager.setBitmap(resource, null, true, WallpaperManager.FLAG_SYSTEM);
                                    Toast.makeText(context, "Wallpaper changed successfully!", Toast.LENGTH_SHORT).show();
                                } catch (IOException e) {
                                    Toast.makeText(context, "Failed to set wallpaper: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onLoadCleared(Drawable placeholder) {}

                            @Override
                            public void onLoadFailed(Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                Toast.makeText(context, "Failed to load wallpaper image", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(context, "Failed to retrieve wallpaper: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
