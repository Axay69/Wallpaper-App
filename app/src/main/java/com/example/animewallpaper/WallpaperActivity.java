
package com.example.animewallpaper;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.animewallpaper.adapters.WallpaperPagerAdapter;
import com.example.animewallpaper.databinding.ActivityWallpaperBinding;
import com.example.animewallpaper.models.Photo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;

public class WallpaperActivity extends AppCompatActivity {

    private ActivityWallpaperBinding binding;
    private ViewPager2 viewPager;
    private ArrayList<Photo> photosList;
    private List<String> wallpaperUrls;
    private Photo currentPhoto;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int currPos = -1;
    private int prevPos = -1;
    private FirebaseUser currentUser;
    private boolean isLiked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWallpaperBinding.inflate(getLayoutInflater());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(binding.getRoot());

        initFirebase();

        loadIntentData(); // Load data from the intent

        setButtonListeners();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    private void loadIntentData() {
        Intent intent = getIntent();
        photosList = intent.getParcelableArrayListExtra("photosList");
        currentPhoto = intent.getParcelableExtra("photo");


        if (photosList != null && !photosList.isEmpty()) {
            wallpaperUrls = new ArrayList<>();
            for (Photo photo : photosList) {
                wallpaperUrls.add(photo.getSrc().getLarge()); // Assuming the `getLarge()` method returns the large URL
            }
        } else {
            wallpaperUrls = new ArrayList<>();
        }

        if (currentPhoto != null) {
            if (currentUser != null) {
                checkIfLikedAndLoadPhoto();
            }
        }

        setUpViewPager();

        // Set ViewPager to start at the position of the current photo
        currPos = wallpaperUrls.indexOf(currentPhoto.getSrc().getLarge());
        if (currPos != -1) {
            viewPager.setCurrentItem(currPos, false); // Go to the specific photo
        }
        
        loadBlurImage();
    }

    private void setUpViewPager() {
        viewPager = binding.viewPager;
        WallpaperPagerAdapter adapter = new WallpaperPagerAdapter(this, wallpaperUrls);
        viewPager.setAdapter(adapter);
    }

    private void loadBlurImage() {
        if (currentPhoto != null) {
            Glide.with(this)
                    .load(currentPhoto.getSrc().getTiny())  // Load the original image
                    .apply(new RequestOptions()
                            .placeholder(new ColorDrawable(Color.DKGRAY))  // Fast placeholder
                            .override(300, 300)  // Resize to a smaller size for faster loading
                            .diskCacheStrategy(DiskCacheStrategy.ALL)  // Cache both original & resized
                            .skipMemoryCache(false))  // Enable memory cache
                    .apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))  // Apply blur transformation
                    .into(binding.imgBlurredWallpaper);
        } else {
            Toast.makeText(this, "Photo is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setButtonListeners() {
        binding.imgBack.setOnClickListener(view -> finish()); // Finish the activity

        binding.imgInfoMenu.setOnClickListener(view -> showPopupMenu(view)); // Show the menu on info icon click

        binding.imgPreview.setOnClickListener(view -> previewWallpaper()); // Preview the wallpaper

        binding.imgLike.setOnClickListener(view -> toggleLikeStatus()); // Toggle like status

        binding.imgDownload.setOnClickListener(view -> downloadWallpaper()); // Download wallpaper

        binding.btnSetAsWallpaper.setOnClickListener(view -> showSetAsWallpaperDialog()); // Show dialog to set as wallpaper

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                addToLikeCollection();

                prevPos = currPos;
                currPos = position;
                currentPhoto = photosList.get(currPos); // Update current photo
                loadBlurImage();
                checkIfLikedAndLoadPhoto();
            }
        });
    }

    private void checkIfLikedAndLoadPhoto() {
        DocumentReference likeRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("liked_wallpapers")
                .document(String.valueOf(currentPhoto.getId()));

        likeRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    isLiked = true;
                    binding.imgLike.setImageResource(R.drawable.ic_like_fill);
                } else {
                    isLiked = false;
                    binding.imgLike.setImageResource(R.drawable.ic_like);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WallpaperActivity.this, "Failed to check like status", Toast.LENGTH_SHORT).show();
            }
        });
    }




    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.END); // This aligns the menu to the right
        popupMenu.getMenuInflater().inflate(R.menu.menu_wallpaper, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.action_share_wallpaper:
                    shareWallpaper(); // Handle share wallpaper
                    return true;
                case R.id.action_about_wallpaper:
                    showWallpaperInfo(); // Show wallpaper details
                    return true;
                default:
                    return false;
            }
        });
        popupMenu.show();
    }

    private void shareWallpaper() {
        int currentPosition = viewPager.getCurrentItem();
        String currentWallpaperUrl = wallpaperUrls.get(currentPosition);

        Glide.with(this)
                .asBitmap()
                .load(currentWallpaperUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @NonNull Transition<? super Bitmap> transition) {
                        try {
                            String fileName = "wallpaper_" + System.currentTimeMillis() + ".png";
                            File cachePath = new File(getCacheDir(), "images");
                            cachePath.mkdirs(); // Make sure the directory exists
                            File file = new File(cachePath, fileName);
                            FileOutputStream stream = new FileOutputStream(file);
                            resource.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            stream.close();

                            Uri imageUri = FileProvider.getUriForFile(WallpaperActivity.this, getPackageName() + ".provider", file);

                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("image/*");
                            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(Intent.createChooser(shareIntent, "Share wallpaper via"));
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(WallpaperActivity.this, "Failed to share wallpaper", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // Handle placeholder if needed
                    }
                });
    }

    private void showWallpaperInfo() {
        // Retrieve current wallpaper info
        Photo currentPhoto = getCurrentPhoto(); // Implement this method to get the current Photo object
        String resolution = currentPhoto.getWidth() + " x " + currentPhoto.getHeight();
        String size = "Unknown"; // Replace with actual size if available
        String photographer = currentPhoto.getPhotographer();
        String photographerUrl = currentPhoto.getPhotographer_url();
        String altText = currentPhoto.getAlt();

        // Create a message to display
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Resolution: ").append(resolution).append("\n")
                .append("Size: ").append(size).append("\n")
                .append("Photographer: ").append(photographer).append("\n")
                .append("Photographer URL: ").append(photographerUrl).append("\n")
                .append("Alt Text: ").append(altText);

        // Build the AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("Wallpaper Information")
                .setMessage(messageBuilder.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    private Photo getCurrentPhoto() {
        int currentPosition = viewPager.getCurrentItem();
        return photosList.get(currentPosition); // Modify this according to how you manage the list of wallpapers
    }

    private void previewWallpaper() {
        int currentPosition = viewPager.getCurrentItem();
        String currentWallpaperUrl = wallpaperUrls.get(currentPosition);


        // Launch PreviewActivity to show wallpaper in full-screen
        Intent previewIntent = new Intent(WallpaperActivity.this, PreviewActivity.class);
        previewIntent.putExtra("photo_url", currentWallpaperUrl);
        startActivity(previewIntent);
    }

    private void toggleLikeStatus() {
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to like wallpapers", Toast.LENGTH_SHORT).show();
            return;
        }

        isLiked = !isLiked;
        if (isLiked) {
            binding.imgLike.setImageResource(R.drawable.ic_like_fill);
        } else {
            binding.imgLike.setImageResource(R.drawable.ic_like);
        }

    }

    private void downloadWallpaper() {
        if (currentPhoto != null) {
            // Show a loading dialog
            AlertDialog loadingDialog = new AlertDialog.Builder(this)
                    .setTitle("Downloading Wallpaper")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            loadingDialog.show();

            // Download image using Glide
            Glide.with(this)
                    .asBitmap()
                    .load(currentPhoto.getSrc().getOriginal())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @NonNull Transition<? super Bitmap> transition) {
                            // Save Bitmap to the device
                            saveBitmapToDevice(resource);

                            // Add to Firestore
                            if (currentUser != null) {
                                addToDownloadedCollection();
                            }

                            loadingDialog.dismiss(); // Dismiss the loading dialog
                        }

                        @Override
                        public void onLoadCleared(@NonNull Drawable placeholder) {
                            // Handle placeholder if needed
                        }
                    });
        } else {
            Toast.makeText(this, "Photo is not available", Toast.LENGTH_SHORT).show();
        }

    }

    private void addToDownloadedCollection() {
        DocumentReference downloadRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("downloaded_wallpapers")
                .document(String.valueOf(currentPhoto.getId()));

        downloadRef.set(currentPhoto).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(WallpaperActivity.this, "Added to downloaded wallpapers", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WallpaperActivity.this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveBitmapToDevice(Bitmap bitmap) {
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "AnimeWallpaper");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        File file = new File(directory, "wallpaper_" + currentPhoto.getId() + ".jpg");
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            Toast.makeText(this, "Wallpaper downloaded successfully!", Toast.LENGTH_SHORT).show();

            // Notify the MediaScanner of the new file
            MediaScannerConnection.scanFile(this, new String[]{file.getAbsolutePath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                @Override
                public void onScanCompleted(String path, Uri uri) {
                    // You can show a message if needed
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to download wallpaper", Toast.LENGTH_SHORT).show();
        }
    }


    private void showSetAsWallpaperDialog() {
        String[] options = {"Home Screen", "Lock Screen", "Both"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Wallpaper");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0:
                        setPhotoAsWallpaper(WallpaperManager.FLAG_SYSTEM);
                        break;
                    case 1:
                        setPhotoAsWallpaper(WallpaperManager.FLAG_LOCK);
                        break;
                    case 2:
                        setPhotoAsWallpaper(WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                        break;
                }
            }
        });
        builder.show();
    }

    private void setPhotoAsWallpaper(int flag) {
        if (currentPhoto != null) {
            // Show a loading dialog
            AlertDialog loadingDialog = new AlertDialog.Builder(this)
                    .setTitle("Setting Wallpaper")
                    .setMessage("Please wait...")
                    .setCancelable(false)
                    .create();
            loadingDialog.show();

            // Load the image as a Bitmap using Glide
            Glide.with(this)
                    .asBitmap()
                    .load(currentPhoto.getSrc().getOriginal())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @NonNull Transition<? super Bitmap> transition) {
                            try {
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(WallpaperActivity.this);
                                wallpaperManager.setBitmap(resource, null, true, flag);

                                // Use runOnUiThread to handle UI operations (toasts, dialog dismiss) on the main thread
                                runOnUiThread(() -> {
                                    Toast.makeText(WallpaperActivity.this, "Wallpaper set successfully!", Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();  // Safely dismiss the loading dialog

                                    // Add to Firestore after setting wallpaper
                                    if (currentUser != null) {
                                        addSetWallpaperToFirestore();
                                    } else {
                                        Toast.makeText(WallpaperActivity.this, "User not logged in", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                runOnUiThread(() -> {
                                    Toast.makeText(WallpaperActivity.this, "Failed to set wallpaper: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    loadingDialog.dismiss();  // Dismiss the dialog on failure
                                });
                            }
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            runOnUiThread(loadingDialog::dismiss);  // Ensure the dialog is dismissed even if the load is canceled
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            runOnUiThread(() -> {
                                Toast.makeText(WallpaperActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                                loadingDialog.dismiss();  // Dismiss the dialog on failure
                            });
                        }
                    });
        } else {
            Toast.makeText(this, "Photo is not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSetWallpaperToFirestore() {
        DocumentReference downloadRef = db.collection("users")
                .document(currentUser.getUid())
                .collection("set_wallpapers")
                .document(String.valueOf(currentPhoto.getId()));

        downloadRef.set(currentPhoto)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> Toast.makeText(WallpaperActivity.this, "Added to set_wallpapers", Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e -> runOnUiThread(() -> Toast.makeText(WallpaperActivity.this, "Failed to add: " + e.getMessage(), Toast.LENGTH_LONG).show()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        addToLikeCollection();
    }

    private void addToLikeCollection() {
        if (currentUser != null && currentPhoto != null) {
            DocumentReference likeRef = db.collection("users")
                    .document(currentUser.getUid())
                    .collection("liked_wallpapers")
                    .document(String.valueOf(currentPhoto.getId()));

            if (isLiked) {
                likeRef.set(currentPhoto); // Ensure like status is updated
            } else {
                likeRef.delete(); // Remove like status
            }
        }
    }

}
