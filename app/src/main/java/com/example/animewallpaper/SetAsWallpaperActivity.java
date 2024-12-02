package com.example.animewallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Bundle;
import android.util.Log;

import com.example.animewallpaper.adapters.WallpaperAdapter2;
import com.example.animewallpaper.databinding.ActivitySetAsWallpaperBinding;
import com.example.animewallpaper.models.Photo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class SetAsWallpaperActivity extends AppCompatActivity {

    private WallpaperAdapter2 adapter;
    private List<Photo> setAsWallpaperList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private ActivitySetAsWallpaperBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySetAsWallpaperBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        setAsWallpaperList = new ArrayList<>();

        binding.recyclerViewSetAs.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
        adapter = new WallpaperAdapter2(setAsWallpaperList, getApplicationContext(), "SetAsWallpaperActivity");
        binding.recyclerViewSetAs.setAdapter(adapter);

        fetchSetAsWallpapers();

        // Set up refresh listener
        binding.swipeRefreshLayoutSetAs.setOnRefreshListener(() -> {
            refreshData();
        });


    }

    private void refreshData() {
        // Clear the current list and re-fetch liked wallpapers
        setAsWallpaperList.clear();
        adapter.notifyDataSetChanged();

        fetchSetAsWallpapers();

        // Stop refreshing animation
        binding.swipeRefreshLayoutSetAs.setRefreshing(false);
    }

    private void fetchSetAsWallpapers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            CollectionReference likedWallpapersRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("set_wallpapers");

            likedWallpapersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        setAsWallpaperList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Photo photo = document.toObject(Photo.class);
                            setAsWallpaperList.add(photo);
                        }

                        Log.d("SetAsWallpaperActivity", setAsWallpaperList.toString());
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors if necessary
                    }
                }
            });
        }
    }
}