package com.example.animewallpaper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.animewallpaper.R;
import com.example.animewallpaper.adapters.CategoryAdapter;
import com.example.animewallpaper.adapters.WallpaperAdapter2;
import com.example.animewallpaper.api.PexelApi;
import com.example.animewallpaper.api.RetrofitClient;
import com.example.animewallpaper.databinding.FragmentCategoryBinding;
import com.example.animewallpaper.models.Category;
import com.example.animewallpaper.models.PexelsResponse;
import com.example.animewallpaper.models.Photo;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private List<Category> categoryList;
    private static final int PER_PAGE = 20;

    private int pageCount = 1;
    private List<Photo> wallpaperList;
    private String currentCategory = "";
    private String currentType = "";

    private CategoryAdapter categoryAdapter;
    private WallpaperAdapter2 wallpaperAdapter;
    private String[] categories_array = {
            "Abstract", "Animals", "Nature", "Minimalistic", "Cityscapes",
            "Technology", "Art", "Fashion", "Architecture", "Space",
            "Sports", "Fantasy", "Retro", "Textures", "Travel", "Music"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);

        // Initialize category RecyclerView
        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(categoryList, getContext(), categoryName -> showWallpapersForCategory(categoryName));

        binding.recyclerViewCategory.setLayoutManager(new GridLayoutManager(getContext(), 1));
        binding.recyclerViewCategory.setAdapter(categoryAdapter);

        // Initialize wallpaper RecyclerView but keep it hidden for now
        wallpaperList = new ArrayList<>();
        wallpaperAdapter = new WallpaperAdapter2(wallpaperList, getContext(), "CategoryFragment");
        binding.recyclerViewWallpapers.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewWallpapers.setAdapter(wallpaperAdapter);
        binding.recyclerViewWallpapers.setVisibility(View.GONE);

// Add scroll listener to load more wallpapers based on the current category/type
        binding.recyclerViewWallpapers.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@Nullable RecyclerView recyclerView, int dx, int dy) {
                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == wallpaperList.size() - 1) {
                    // Only fetch more wallpapers if there's a current valid category or type
                    pageCount++;
                    if (!currentCategory.isEmpty()) {
                        fetchWallpapersForCategory(currentCategory);  // Load more wallpapers for the category
                    } else {
                        fetchWallpapersByType(currentType);  // Load more wallpapers by type (latest, popular, etc.)
                    }
                }
            }
        });

        fetchCategoryImages();

        binding.swipeRefreshLayoutCategory.setOnRefreshListener(() -> refreshData());

        // Handle back button clicks to go back to category list
        binding.toolbarCategory.setNavigationOnClickListener(v -> showCategoryList());

        binding.toolbarCategory.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.action_latest:
                        binding.toolbarCategory.setTitle("Latest Wallpapers");
                        wallpaperList.clear();
                        wallpaperAdapter.notifyDataSetChanged();
                        fetchWallpapersByType("latest");
                        return true;

                    case R.id.action_popular:
                        binding.toolbarCategory.setTitle("Popular Wallpapers");
                        wallpaperList.clear();
                        wallpaperAdapter.notifyDataSetChanged();
                        fetchWallpapersByType("popular");
                        return true;
//
//                    case R.id.action_special:
//                        binding.toolbarCategory.setTitle("Special Collection");
//                        wallpaperList.clear();
//                        wallpaperAdapter.notifyDataSetChanged();
//                        fetchWallpapersByType("special");
//                        return true;

                }

                return false;
            }
        });

        return binding.getRoot();
    }

    // Show wallpapers for selected category
    private void showWallpapersForCategory(String categoryName) {
        binding.recyclerViewCategory.setVisibility(View.GONE);
        binding.recyclerViewWallpapers.setVisibility(View.VISIBLE);

        binding.toolbarCategory.setTitle(categoryName);
        binding.toolbarCategory.setNavigationIcon(R.drawable.ic_back);

        wallpaperList.clear();  // Clear old wallpapers
        wallpaperAdapter.notifyDataSetChanged();  // Notify the adapter about data changes
        pageCount = 1;  // Reset page count for new category
        currentCategory = categoryName;
        currentType = "";  // Reset the type, since we're using category
        fetchWallpapersForCategory(categoryName);
    }

    // Show category list and hide the wallpaper list
    private void showCategoryList() {
        binding.recyclerViewWallpapers.setVisibility(View.GONE);
        binding.recyclerViewCategory.setVisibility(View.VISIBLE);

        // Reset the toolbar title and hide the back button
        binding.toolbarCategory.setTitle("Categories");
        binding.toolbarCategory.setNavigationIcon(null);
    }

    private void refreshData() {
        Toast.makeText(getContext(), "Refreshing...", Toast.LENGTH_SHORT).show();

        // Clear the current list and reset page count
        categoryList.clear();
        categoryAdapter.notifyDataSetChanged();

        fetchCategoryImages();
        binding.swipeRefreshLayoutCategory.setRefreshing(false);
    }

    private void fetchCategoryImages() {
        for (String category : categories_array) {
            Call<PexelsResponse> call = RetrofitClient.getClient()
                    .create(PexelApi.class)
                    .getWallpapersBySearch(category, 1, 1); // Get a single image per category

            call.enqueue(new Callback<PexelsResponse>() {
                @Override
                public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<Photo> photos = response.body().getPhotos();
                        if (!photos.isEmpty()) {
                            String imageUrl = photos.get(0).getSrc().getOriginal();
                            String avgColor = photos.get(0).getAvg_color();
                            categoryList.add(new Category(category, imageUrl, avgColor));
                            categoryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        showToast("Error loading category images");
                    }
                }

                @Override
                public void onFailure(Call<PexelsResponse> call, Throwable t) {
                    showToast("Failed to load category images");
                }
            });
        }
    }

    private void fetchWallpapersForCategory(String categoryName) {
        Call<PexelsResponse> call = RetrofitClient.getClient()
                .create(PexelApi.class)
                .getWallpapersByCategory(categoryName, PER_PAGE, pageCount); // Fetch 20 wallpapers per page

        call.enqueue(new Callback<PexelsResponse>() {
            @Override
            public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Photo> newPhotos = response.body().getPhotos();
                    wallpaperList.addAll(newPhotos);
                    wallpaperAdapter.notifyDataSetChanged();
                } else {
                    showToast("Error loading wallpapers");
                }
            }

            @Override
            public void onFailure(Call<PexelsResponse> call, Throwable t) {
                showToast("Failed to load wallpapers");
            }
        });
    }


    // Fetch wallpapers by type (latest, popular, special)
    private void fetchWallpapersByType(String type) {
        binding.toolbarCategory.setNavigationIcon(R.drawable.ic_back);

        binding.recyclerViewCategory.setVisibility(View.GONE);
        binding.recyclerViewWallpapers.setVisibility(View.VISIBLE);

        currentType = type;  // Save the current type
        currentCategory = "";  // Reset the category, since we're using type

        Call<PexelsResponse> call;
        if (type.equals("latest")) {
            showToast("Latest");
            call = RetrofitClient.getClient().create(PexelApi.class).getCuratedWallpapers(PER_PAGE, pageCount);
        } else if (type.equals("popular")) {
            showToast("Popular");
            call = RetrofitClient.getClient().create(PexelApi.class).getPopularWallpapers("wallpapers", "popular", PER_PAGE, pageCount);
        } else {
            call = RetrofitClient.getClient().create(PexelApi.class).getRandomWallpaper(PER_PAGE, pageCount);
        }

        call.enqueue(new Callback<PexelsResponse>() {
            @Override
            public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    wallpaperList.addAll(response.body().getPhotos());
                    wallpaperAdapter.notifyDataSetChanged();
                } else {
                    showToast("Error loading wallpapers");
                }
            }

            @Override
            public void onFailure(Call<PexelsResponse> call, Throwable t) {
                showToast("Failed to load wallpapers");
            }
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}
