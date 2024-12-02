package com.example.animewallpaper.Extra;

import android.content.Context;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WallpaperHelper {

    public interface WallpaperUrlCallback {
        void onSuccess(String wallpaperUrl);
        void onFailure(String errorMessage);
    }

    public void getWallpaperUrl(Context context, WallpaperUrlCallback callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        String userId = auth.getCurrentUser().getUid();
        CollectionReference likedWallpapersRef = firestore.collection("users").document(userId).collection("liked_wallpapers");

        likedWallpapersRef.get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> wallpaperUrls = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String wallpaperUrl = document.getString("imageUrl"); // Assuming "imageUrl" is the field name
                        wallpaperUrls.add(wallpaperUrl);
                    }

                    if (!wallpaperUrls.isEmpty()) {
                        Random random = new Random();
                        String randomWallpaperUrl = wallpaperUrls.get(random.nextInt(wallpaperUrls.size()));
                        callback.onSuccess(randomWallpaperUrl);
                    } else {
                        callback.onFailure("No wallpapers found.");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("WallpaperHelper", "Error getting wallpapers", e);
                    callback.onFailure("Failed to retrieve wallpapers.");
                });
    }
}
