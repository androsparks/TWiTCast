package com.tragicfruit.twitcast;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class Show {
    private String mTitle;
    private String mCoverArtUrl;
    private Drawable mCoverArt;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getCoverArtUrl() {
        return mCoverArtUrl;
    }

    public void setCoverArtUrl(String coverArtUrl) {
        mCoverArtUrl = coverArtUrl;
    }

    public Drawable getCoverArt() {
        return mCoverArt;
    }

    public void setCoverArt(Drawable coverArt) {
        mCoverArt = coverArt;
    }
}
