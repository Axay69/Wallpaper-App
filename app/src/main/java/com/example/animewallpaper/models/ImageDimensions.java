package com.example.animewallpaper.models;

import com.google.gson.annotations.SerializedName;

public class ImageDimensions {

    @SerializedName("medium")
    private String medium;
    @SerializedName("large")
    private String large;
    @SerializedName("original")
    private String original;

    public ImageDimensions(String medium, String large, String original) {
        this.medium = medium;
        this.large = large;
        this.original = original;
    }

    public String getMedium() {
        return medium;
    }

    public void setMedium(String medium) {
        this.medium = medium;
    }

    public String getLarge() {
        return large;
    }

    public void setLarge(String large) {
        this.large = large;
    }

    public String getOriginal() {
        return original;
    }

    public void setOriginal(String original) {
        this.original = original;
    }
}

