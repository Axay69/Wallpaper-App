package com.example.animewallpaper.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WallpaperAdapter2 extends RecyclerView.Adapter<WallpaperAdapter2.WallpaperViewHolder> {

    private List<Photo> photosList;
    private Context context;
    private String fragmentName;

    public WallpaperAdapter2(List<Photo> photosList, Context context) {
        this.photosList = photosList;
        this.context = context;
    }

    public WallpaperAdapter2(List<Photo> photosList, Context context, String fragmentName) {
        this.photosList = photosList;
        this.context = context;
        this.fragmentName = fragmentName;
    }

    @NonNull
    @Override
    public WallpaperViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_item_home_wallpaper, parent, false);
        return new WallpaperViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WallpaperViewHolder holder, int position) {
        Photo photo = photosList.get(position);
        holder.bind(photo);

        // Handle long press for removing wallpaper
        holder.imgWallpaper.setOnLongClickListener(view -> {
            if ("LikeFragment".equals(fragmentName)) {
                showRemoveDialog(photo, position, "liked_wallpapers");
            } else if ("DownloadFragment".equals(fragmentName)) {
                showRemoveDialog(photo, position, "downloaded_wallpapers");
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return photosList.size();
    }

    // Method to show a confirmation dialog before removing wallpaper
    private void showRemoveDialog(Photo photo, int position, String collection) {
        String message = collection.equals("liked_wallpapers") ? "Remove this wallpaper from your likes?" : "Remove this wallpaper from your downloads?";

        new AlertDialog.Builder(context)
                .setTitle("Remove Wallpaper")
                .setMessage(message)
                .setPositiveButton("Yes", (dialog, which) -> removeWallpaper(photo, position, collection))
                .setNegativeButton("No", null)
                .show();
    }

    // Method to remove wallpaper from Firebase and the local list
    private void removeWallpaper(Photo photo, int position, String collection) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            CollectionReference wallpapersRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection(collection);

            // Remove from Firebase
            wallpapersRef.document(String.valueOf(photo.getId())).delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Remove from local list and notify the adapter
                    photosList.remove(position);
                    notifyItemRemoved(position);
                    String toastMessage = collection.equals("liked_wallpapers") ? "Wallpaper removed from likes" : "Wallpaper removed from downloads";
                    Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Failed to remove wallpaper", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // ViewHolder for wallpaper items
    class WallpaperViewHolder extends RecyclerView.ViewHolder {
        ImageView imgWallpaper;

        public WallpaperViewHolder(View itemView) {
            super(itemView);
            imgWallpaper = itemView.findViewById(R.id.imgWallpaperHome);
        }

        public void bind(Photo photo) {
            String avgColor = photo.getAvg_color(); // returns color like "#938584"
            int placeholderColor;

            try {
                placeholderColor = Color.parseColor(avgColor);
            } catch (IllegalArgumentException e) {
                placeholderColor = Color.GRAY;
            }

            if (photo.getSrc() != null && photo.getSrc().getOriginal() != null) {
                Glide.with(context)
                        .load(photo.getSrc().getTiny())  // Use smaller versions for thumbnails if possible
                        .apply(new RequestOptions()
                                .placeholder(new ColorDrawable(placeholderColor))  // Fast placeholder
                                .override(150, 300)  // Resize to a smaller size for faster loading
                                .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original & resized
                                .skipMemoryCache(false)  // Enable memory cache
                        )
                        .listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                Toast.makeText(context, "Image load failed at : " + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(imgWallpaper);

                imgWallpaper.setOnClickListener(view -> {
                    // Start WallpaperActivity with the image URL
                    Intent intent = new Intent(context, WallpaperActivity.class);
                    if (!(context instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    intent.putExtra("image_url", photo.getSrc().getTiny());
                    intent.putParcelableArrayListExtra("photosList", (ArrayList<? extends Parcelable>) photosList); // If Parcelable
                    intent.putExtra("photo", (Parcelable) photo);
                    context.startActivity(intent);
                });
            } else {
                Toast.makeText(context, "Image URL is missing", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
