package com.example.androidprogrammingbook05;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GalleryItem {
    @SerializedName("title")
    private String mCaption;
    @SerializedName("id")
    private String mID;
    @SerializedName("url_s")
    private String mUrl;
    @SerializedName("owner")
    private String mOwner;

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }


    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String caption) {
        mCaption = caption;
    }

    public String getID() {
        return mID;
    }

    public void setID(String ID) {
        mID = ID;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    @Override
    public String toString() {
        return mCaption;
    }

    public Uri getPhotoPageUri(){
        return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mID)
                .build();
    }
}

class GalleryItemRaw {
    @SerializedName("photos")
    Photos mPhotos;
}

class Photos {
    @SerializedName("photo")
    List<GalleryItem> mItems;
    @SerializedName("page")
    int mPage;
    @SerializedName("pages")
    int mPages;
    @SerializedName("perpage")
    int mPerPage;
    @SerializedName("total")
    int mTotal;
    public Photos(List<GalleryItem> items, int page, int pages){
        mItems = items;
        mPage = page;
        mPages = pages;
    }
}

