package com.example.animewallpaper.api;

import com.example.animewallpaper.models.PexelsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface PexelApi {

    // API key for authorization
    String AUTH_HEADER = "Authorization: da5jkySFdTej5Y2yV7FzwWjSGWakRm7Oa8Aw02Tb2N8JfrsczYXKi8R5";

    // 1. Random Wallpapers
    @Headers(AUTH_HEADER)
    @GET("v1/curated")
    Call<PexelsResponse> getRandomWallpaper(
            @Query("per_page") int perPage,
            @Query("page") int pageCount
    );

    // 2. Curated Wallpapers
    @Headers(AUTH_HEADER)
    @GET("v1/curated")
    Call<PexelsResponse> getCuratedWallpapers(
            @Query("per_page") int perPage,
            @Query("page") int pageCount
    );

    // 3. Popular Wallpapers (simulating popular by setting specific sort order - Pexels does not have explicit "popular" endpoint)
    @Headers(AUTH_HEADER)
    @GET("v1/search")
    Call<PexelsResponse> getPopularWallpapers(
            @Query("query") String query,
            @Query("order_by") String orderBy,  // Ordering wallpapers by popularity
            @Query("per_page") int perPage,
            @Query("page") int pageCount
    );

    // 4. Wallpapers by Category (e.g., nature, minimalistic, abstract, amoled, dark, calm)
    @Headers(AUTH_HEADER)
    @GET("v1/search")
    Call<PexelsResponse> getWallpapersByCategory(
            @Query("query") String category,   // Category name as query parameter
            @Query("per_page") int perPage,
            @Query("page") int pageCount
    );

    // 5. Search Wallpapers
    @Headers(AUTH_HEADER)
    @GET("v1/search")
    Call<PexelsResponse> getWallpapersBySearch(
            @Query("query") String query,
            @Query("per_page") int perPage,
            @Query("page") int pageCount
    );
}
