package com.example.animewallpaper.models;

public class Category {
    private String name;
    private String imageUrl;
    private String avgColor;

    public Category(String name, String imageUrl, String avgColor) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.avgColor = avgColor;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getAvgColor() {
        return imageUrl;
    }


}
