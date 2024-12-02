package com.example.animewallpaper;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.animewallpaper.databinding.FragmentProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;
    FirebaseFirestore firestore;
    FirebaseAuth auth;
    FirebaseUser user;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        setupToolbarMenu();

        if (user != null) {
            fetchUserProfile();
            fetchTotalWallpapers();
        }

        setOnClickListeners();

        return view;
    }

    private void setOnClickListeners() {
        binding.editProfileButton.setOnClickListener(v -> {
            editUserProfile();
        });

        binding.swipeRefreshProfile.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                if (user != null) {
                    fetchUserProfile();
                    fetchTotalWallpapers();
                }


                binding.swipeRefreshProfile.setRefreshing(false);
            }
        });

        binding.settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
            }
        });
    }

    private void setupToolbarMenu() {
        binding.toolbarProfile.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.action_about_us) {
                startActivity(new Intent(getActivity(), AboutUsActivity.class));
                return true;

            } else if (id == R.id.action_share) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                String shareBody = "Check out this awesome wallpaper app!\n\nDownload it here: https://play.google.com/store/apps/details?id=com.google.android.apps.wallpaper";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(shareIntent, "Share via"));
                return true;

            } else if (id == R.id.action_logout) {
                // Sign out and go to login screen
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
                return true;

            } else if (id == R.id.action_setting) {
                // Open settings activity
                startActivity(new Intent(getActivity(), SettingActivity.class));
                return true;
            }

            return false;
        });
    }

    private void fetchUserProfile() {
        DocumentReference docRef = firestore.collection("users")
                .document(user.getUid())
                .collection("profile")
                .document("user_profile");

        docRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("UserName");
                String email = documentSnapshot.getString("UserEmailId");
                String pass = documentSnapshot.getString("UserPassword");

                binding.userName.setText(name);
                binding.userEmail.setText(email);
                binding.userPassword.setText(pass);

                if (name != null && !name.isEmpty()) {
                    binding.userProfilePicture.setText(String.valueOf(name.charAt(0)).toUpperCase());
                }
            } else {
                Toast.makeText(getContext(), "Profile not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
        });
    }

    private void editUserProfile() {
        // Inflate dialog layout
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.dialog_edit_profile, null);

        // Create AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Find EditTexts inside the dialog
        EditText editName = dialogView.findViewById(R.id.editUserName);
        EditText editEmail = dialogView.findViewById(R.id.editUserEmail);
        EditText editPassword = dialogView.findViewById(R.id.editUserPassword);

        // Pre-fill current profile data
        editName.setText(binding.userName.getText().toString());
        editEmail.setText(binding.userEmail.getText().toString());
        editPassword.setText(binding.userPassword.getText().toString());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String updatedName = editName.getText().toString();
            String updatedEmail = editEmail.getText().toString();
            String updatedPassword = editPassword.getText().toString();

            // Update profile in Firestore
            Map<String, Object> updatedData = new HashMap<>();
            updatedData.put("UserName", updatedName);
            updatedData.put("UserEmailId", updatedEmail);
            updatedData.put("UserPassword", updatedPassword);

            DocumentReference docRef = firestore.collection("users")
                    .document(user.getUid())
                    .collection("profile")
                    .document("user_profile");

            docRef.update(updatedData)
                    .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Profile updated", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error updating profile", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        // Show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void fetchTotalWallpapers() {
        // Fetch total liked wallpapers
        firestore.collection("users")
                .document(user.getUid())
                .collection("liked_wallpapers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalLikedWallpapers = queryDocumentSnapshots.size();
                    binding.likedWallpapersCount.setText(String.valueOf(totalLikedWallpapers));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching liked wallpapers", Toast.LENGTH_SHORT).show();
                });

        // Fetch total downloaded wallpapers
        firestore.collection("users")
                .document(user.getUid())
                .collection("downloaded_wallpapers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalDownloadedWallpapers = queryDocumentSnapshots.size();
                    binding.downloadedWallpapersCount.setText(String.valueOf(totalDownloadedWallpapers));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching downloaded wallpapers", Toast.LENGTH_SHORT).show();
                });

        // Fetch total wallpapers set as wallpaper
        firestore.collection("users")
                .document(user.getUid())
                .collection("set_wallpapers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int totalSetWallpapers = queryDocumentSnapshots.size();
                    binding.setAsWallpapersCount.setText(String.valueOf(totalSetWallpapers));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error fetching set as wallpapers", Toast.LENGTH_SHORT).show();
                });
    }



}
