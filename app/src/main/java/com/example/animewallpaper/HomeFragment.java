package com.example.animewallpaper;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.widget.NestedScrollView;
import androidx.cursoradapter.widget.CursorAdapter;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.appcompat.widget.SearchView;

import com.example.animewallpaper.adapters.SuggestionCursorAdapter;
import com.example.animewallpaper.adapters.WallpaperAdapter2;
import com.example.animewallpaper.api.PexelApi;
import com.example.animewallpaper.api.RetrofitClient;
import com.example.animewallpaper.databinding.FragmentHomeBinding;
import com.example.animewallpaper.models.PexelsResponse;
import com.example.animewallpaper.models.Photo;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

//    private static final String API_KEY = "your_pexel_api_key_here";
    private String query = "";
    private int pageCount = 1;
    private static final int PER_PAGE = 20;

    private FragmentHomeBinding binding;
    private List<Photo> photosList;
    private WallpaperAdapter2 adapter;
    private boolean isLoading = false;

    public HomeFragment() {
        // Required empty public constructor
        setRetainInstance(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        // Set toolbar as action bar
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
//            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        }

        setRetainInstance(true);
        initRecyclerView();
        fetchWallpapers();

        binding.swipeRefreshLayoutHome.setOnRefreshListener(() -> {
            refreshData();  // Call the refresh function
        });

        binding.toolbarHome.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
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
                } else if (id == R.id.action_search) {
                    MenuItem searchItem = item;
                    SearchView searchView = (SearchView) searchItem.getActionView();
                    setupSearchView(searchView);

                    return true;
                }

                return false;
            }
        });

        return binding.getRoot();
    }

    private void refreshData() {
        Toast.makeText(getContext(), "Refreshing...", Toast.LENGTH_SHORT).show();

        // Clear the current list and reset page count
        photosList.clear();
        adapter.notifyDataSetChanged();
        pageCount = 1;

        query = "";
        MenuItem searchItem = binding.toolbarHome.getMenu().findItem(R.id.action_search);
        if (searchItem != null && searchItem.isActionViewExpanded()) {
            searchItem.collapseActionView();
        }

        // Fetch fresh wallpapers
        fetchWallpapers();

        // Stop refreshing animation
        binding.swipeRefreshLayoutHome.setRefreshing(false);
    }

    private void updateSuggestions(String query) {
        Set<String> searchQueries = SearchUtils.getSearchQueries(getContext());
        final MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1});

        int id = 0;
        for (String searchQuery : searchQueries) {
            if (searchQuery.toLowerCase().startsWith(query.toLowerCase())) {
                Log.d("Suggestion", "Adding suggestion: " + searchQuery);
                cursor.addRow(new Object[]{id++, searchQuery});
            }
        }

//        binding.searchView.getSuggestionsAdapter().changeCursor(cursor);
    }

    private void initRecyclerView() {
        binding.recyclerViewHome.setHasFixedSize(true);
        binding.recyclerViewHome.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        photosList = new ArrayList<>();
        adapter = new WallpaperAdapter2(photosList, getContext(), "HomeFragment");
        binding.recyclerViewHome.setAdapter(adapter);

        setUpPagination();
    }

    private void fetchWallpapers() {
        isLoading = true;

        Call<PexelsResponse> call;

        if (query == null || query.isEmpty()) {
            // Generate a random page number (ensure it's within a reasonable range)
            int randomPage = (int) (Math.random() * 100) + 1; // For example, random page between 1 and 100
            call = RetrofitClient.getClient()
                    .create(PexelApi.class)
                    .getRandomWallpaper(PER_PAGE, randomPage);
            Log.d("HomeFragment", "Fetching random wallpapers: page " + randomPage);

        }  else {

            call = RetrofitClient.getClient()
                    .create(PexelApi.class)
                    .getWallpapersBySearch(query, PER_PAGE, pageCount);
            Log.d("HomeFragment", "Fetching wallpapers for query: " + query + ", page: " + pageCount);

        }

        call.enqueue(new Callback<PexelsResponse>() {
            @Override
            public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                Log.d("HomeFragment", "API Response: " + response);
                if (response.isSuccessful() && response.body() != null) {
                    List<Photo> newPhotos = response.body().getPhotos();
                    if (newPhotos.size() == 0) {
                        Toast.makeText(getContext(), "No results found", Toast.LENGTH_SHORT).show();
                    }
                    photosList.addAll(newPhotos);
                    adapter.notifyDataSetChanged();
                } else {
                    showToast("Error loading data");
                }
                isLoading = false;
            }

            @Override
            public void onFailure(Call<PexelsResponse> call, Throwable throwable) {
                Log.e("HomeFragment", "API Call failed", throwable);
                isLoading = false;
                showToast("Failed to load data");
                binding.swipeRefreshLayoutHome.setRefreshing(false);
            }
        });
    }

    private void setUpPagination() {
        binding.nestedScrollViewHome.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (view, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (!isLoading && isScrolledToBottom()) {
                pageCount++;
                fetchWallpapers();
            }
        });
    }

    private boolean isScrolledToBottom() {
        View contentView = binding.nestedScrollViewHome.getChildAt(0);
        return contentView != null &&
                binding.nestedScrollViewHome.getScrollY() + binding.nestedScrollViewHome.getHeight() >= contentView.getMeasuredHeight() - 2000;
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    public void setupSearchView(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Implement search functionality here
//                showToast("Submitted query :- " + query);
                HomeFragment.this.query = query;
                photosList.clear();
                pageCount = 1;
                fetchWallpapers();
                SearchUtils.addSearchQuery(getContext(), query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Update suggestions
                updateSuggestions(newText);
                return true;
            }
        });
    }

    public static class SearchUtils {

        private static final String PREFS_NAME = "search_prefs";
        private static final String KEY_SEARCHES = "searches";

        public static void addSearchQuery(Context context, String query) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            Set<String> searches = new HashSet<>(prefs.getStringSet(KEY_SEARCHES, new HashSet<>()));
            searches.add(query);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putStringSet(KEY_SEARCHES, searches);
            editor.apply();
        }

        public static Set<String> getSearchQueries(Context context) {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            return prefs.getStringSet(KEY_SEARCHES, new HashSet<>());
        }
    }


}
