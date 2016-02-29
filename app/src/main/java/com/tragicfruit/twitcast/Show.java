package com.tragicfruit.twitcast;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class Show {
    private List<Episode> mEpisodes;
    private String mTitle;
    private String mCoverArtUrl;
    private Drawable mCoverArt;
    private int mId;
    private String mCleanPath;

    public Show() {
        mEpisodes = new ArrayList<>();
    }

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

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getCleanPath() {
        return mCleanPath;
    }

    public void setCleanPath(String cleanPath) {
        mCleanPath = cleanPath;
    }

    public void addEpisode(Episode episode) {
        mEpisodes.add(episode);
    }

    public List<Episode> getEpisodes() {
        return mEpisodes;
    }
}
