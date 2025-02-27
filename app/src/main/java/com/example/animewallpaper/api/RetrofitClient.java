package com.example.animewallpaper.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//public class RetrofitClient {
//
//    private static final String BASE_URL = "https://api.pexels.com/";
//    private static RetrofitClient retrofitClient;
//    private static Retrofit retrofit = null;
//
//    private OkHttpClient.Builder builder = new OkHttpClient.Builder();
//    private HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
//
//    public RetrofitClient() {
//        Gson gson = new GsonBuilder()
//                .setLenient()
//                .create();
//        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
//
//        retrofit = new Retrofit.Builder()
//                .baseUrl(BASE_URL)
//                .addConverterFactory(GsonConverterFactory.create())
//                .client(builder.build())
//                .build();
//    }
//
//    public static synchronized RetrofitClient getInstance() {
//        if (retrofitClient == null) {
//            retrofitClient= new RetrofitClient();
//        }
//
//        return retrofitClient;
//    }
//
//    public PexelApi getApi() {
//        return retrofit.create(PexelApi.class);
//    }
//}

public class RetrofitClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.pexels.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit;
    }
}