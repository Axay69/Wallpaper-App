package com.example.animewallpaper.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Photo implements Parcelable {
    private int id;
    private int width;
    private int height;
    private String url;
    private String photographer;
    private String photographer_url;
    private int photographer_id;
    private String avg_color;
    private Src src;
    private boolean liked;
    private String alt;

    public Photo() {
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPhotographer() {
        return photographer;
    }

    public void setPhotographer(String photographer) {
        this.photographer = photographer;
    }

    public String getPhotographer_url() {
        return photographer_url;
    }

    public void setPhotographer_url(String photographer_url) {
        this.photographer_url = photographer_url;
    }

    public int getPhotographer_id() {
        return photographer_id;
    }

    public void setPhotographer_id(int photographer_id) {
        this.photographer_id = photographer_id;
    }

    public String getAvg_color() {
        return avg_color;
    }

    public void setAvg_color(String avg_color) {
        this.avg_color = avg_color;
    }

    public Src getSrc() {
        return src;
    }

    public void setSrc(Src src) {
        this.src = src;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    // Parcelable implementation

    protected Photo(Parcel in) {
        id = in.readInt();
        width = in.readInt();
        height = in.readInt();
        url = in.readString();
        photographer = in.readString();
        photographer_url = in.readString();
        photographer_id = in.readInt();
        avg_color = in.readString();
        liked = in.readByte() != 0;
        alt = in.readString();
        src = in.readParcelable(Src.class.getClassLoader());
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(url);
        dest.writeString(photographer);
        dest.writeString(photographer_url);
        dest.writeInt(photographer_id);
        dest.writeString(avg_color);
        dest.writeByte((byte) (liked ? 1 : 0));
        dest.writeString(alt);
        dest.writeParcelable(src, flags);
    }

    public static class Src implements Parcelable {
        private String original;
        private String large2x;
        private String large;
        private String medium;
        private String small;
        private String portrait;
        private String landscape;
        private String tiny;

        // Getters and Setters


        public Src() {
        }

        public String getOriginal() {
            return original;
        }

        public void setOriginal(String original) {
            this.original = original;
        }

        public String getLarge2x() {
            return large2x;
        }

        public void setLarge2x(String large2x) {
            this.large2x = large2x;
        }

        public String getLarge() {
            return large;
        }

        public void setLarge(String large) {
            this.large = large;
        }

        public String getMedium() {
            return medium;
        }

        public void setMedium(String medium) {
            this.medium = medium;
        }

        public String getSmall() {
            return small;
        }

        public void setSmall(String small) {
            this.small = small;
        }

        public String getPortrait() {
            return portrait;
        }

        public void setPortrait(String portrait) {
            this.portrait = portrait;
        }

        public String getLandscape() {
            return landscape;
        }

        public void setLandscape(String landscape) {
            this.landscape = landscape;
        }

        public String getTiny() {
            return tiny;
        }

        public void setTiny(String tiny) {
            this.tiny = tiny;
        }

        // Parcelable implementation

        protected Src(Parcel in) {
            original = in.readString();
            large2x = in.readString();
            large = in.readString();
            medium = in.readString();
            small = in.readString();
            portrait = in.readString();
            landscape = in.readString();
            tiny = in.readString();
        }

        public static final Creator<Src> CREATOR = new Creator<Src>() {
            @Override
            public Src createFromParcel(Parcel in) {
                return new Src(in);
            }

            @Override
            public Src[] newArray(int size) {
                return new Src[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString(original);
            dest.writeString(large2x);
            dest.writeString(large);
            dest.writeString(medium);
            dest.writeString(small);
            dest.writeString(portrait);
            dest.writeString(landscape);
            dest.writeString(tiny);
        }
    }
}
