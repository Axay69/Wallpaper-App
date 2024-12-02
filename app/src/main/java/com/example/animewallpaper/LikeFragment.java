package com.example.animewallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.animewallpaper.adapters.WallpaperAdapter2;
import com.example.animewallpaper.databinding.FragmentLikeBinding;
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

public class LikeFragment extends Fragment {

    private WallpaperAdapter2 adapter;
    private List<Photo> likedWallpapersList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FragmentLikeBinding binding;

    public LikeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        likedWallpapersList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLikeBinding.inflate(inflater, container, false);

        binding.recyclerViewLike.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new WallpaperAdapter2(likedWallpapersList, getContext(), "LikeFragment");
        binding.recyclerViewLike.setAdapter(adapter);

        fetchLikedWallpapers();

        // Set up refresh listener
        binding.swipeRefreshLayoutLike.setOnRefreshListener(() -> {
            refreshData();
        });

        binding.toolbarLike.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.action_about_us) {
                    // Open About Us page
                    Intent intent = new Intent(getActivity(), AboutUsActivity.class);
                    startActivity(intent);
                    return true;

                } else if (id == R.id.action_share) {
                    // Share the application
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    String shareBody = "Check out this awesome wallpaper app! Download it from Demo link\n\nhttps://play.google.com/store/apps/details?id=com.google.android.apps.wallpaper&hl=en_IN";
                    String shareSubject = "Wallpaper App";

                    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareSubject);
                    shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);

                    startActivity(Intent.createChooser(shareIntent, "Share via"));
                    return true;

                } else if (id == R.id.action_logout) {
                    // Logout the user
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                    return true;
                }

                return false;
            }
        });


        return binding.getRoot();
    }

    private void refreshData() {
        // Clear the current list and re-fetch liked wallpapers
        likedWallpapersList.clear();
        adapter.notifyDataSetChanged();

        fetchLikedWallpapers();

        // Stop refreshing animation
        binding.swipeRefreshLayoutLike.setRefreshing(false);
    }

    private void fetchLikedWallpapers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            CollectionReference likedWallpapersRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("liked_wallpapers");

            likedWallpapersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        likedWallpapersList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Photo photo = document.toObject(Photo.class);
                            likedWallpapersList.add(photo);
                        }

                        Log.d("LikeFragment", likedWallpapersList.toString());
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle errors if necessary
                    }
                }
            });
        }
    }
} 