package com.example.animewallpaper;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.animewallpaper.adapters.WallpaperAdapter2;
import com.example.animewallpaper.databinding.FragmentDownloadBinding;
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

public class DownloadFragment extends Fragment {

    private WallpaperAdapter2 adapter;
    private List<Photo> downloadedWallpapersList;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FragmentDownloadBinding binding;

    public DownloadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        downloadedWallpapersList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDownloadBinding.inflate(inflater, container, false);

        binding.recyclerViewDownload.setLayoutManager(new GridLayoutManager(getContext(), 2));

        adapter = new WallpaperAdapter2(downloadedWallpapersList, getContext(), "DownloadFragment");
        binding.recyclerViewDownload.setAdapter(adapter);

        fetchDownloadedWallpapers();

        // Swipe refresh functionality
        binding.swipeRefreshLayoutDownload.setOnRefreshListener(() -> {
            refreshData();
        });


        binding.toolbarDownload.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
        // Clear the current list and fetch fresh data
        downloadedWallpapersList.clear();
        adapter.notifyDataSetChanged();

        // Fetch fresh downloaded wallpapers
        fetchDownloadedWallpapers();
        binding.swipeRefreshLayoutDownload.setRefreshing(false);
    }

    private void fetchDownloadedWallpapers() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            CollectionReference downloadedWallpapersRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("downloaded_wallpapers");

            downloadedWallpapersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        downloadedWallpapersList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            Photo photo = document.toObject(Photo.class);
                            downloadedWallpapersList.add(photo);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        // Handle any errors here
                    }
                }
            });
        }
    }
}
