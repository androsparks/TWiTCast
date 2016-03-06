package com.tragicfruit.twitcast.show;

import android.graphics.drawable.Drawable;

import com.tragicfruit.twitcast.episode.Episode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class Show {
    private List<Episode> mEpisodes;
    private String mTitle;
    private String mCoverArtSmallUrl;
    private String mCoverArtUrl;
    private String mCoverArtLargeUrl;
    private String mCoverArtLocalPath;
    private Drawable mCoverArt;
    private int mId;
    private String mDescription;
    private String mVideoHdFeed;
    private String mVideoLargeFeed;
    private String mVideoSmallFeed;
    private String mAudioFeed;
    private boolean mLoadedAllEpisodes;

    public Show() {
        mEpisodes = new ArrayList<>();
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getCoverArtSmallUrl() {
        return mCoverArtSmallUrl;
    }

    public void setCoverArtSmallUrl(String coverArtSmallUrl) {
        mCoverArtSmallUrl = coverArtSmallUrl;
    }

    public String getCoverArtUrl() {
        return mCoverArtUrl;
    }

    public void setCoverArtUrl(String coverArtUrl) {
        mCoverArtUrl = coverArtUrl;
    }

    public String getCoverArtLargeUrl() {
        return mCoverArtLargeUrl;
    }

    public void setCoverArtLargeUrl(String coverArtLargeUrl) {
        mCoverArtLargeUrl = coverArtLargeUrl;
    }

    public String getCoverArtLocalPath() {
        return mCoverArtLocalPath;
    }

    public void setCoverArtLocalPath(String coverArtLocalPath) {
        mCoverArtLocalPath = coverArtLocalPath;
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

    public List<Episode> getEpisodes() {
        return mEpisodes;
    }

    public void addEpisode(Episode episode) {
        mEpisodes.add(episode);
    }

    public void removeEpisode(Episode episode) {
        mEpisodes.remove(episode);
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getVideoHdFeed() {
        return mVideoHdFeed;
    }

    public void setVideoHdFeed(String videoHdFeed) {
        mVideoHdFeed = videoHdFeed;
    }

    public String getVideoLargeFeed() {
        return mVideoLargeFeed;
    }

    public void setVideoLargeFeed(String videoLargeFeed) {
        mVideoLargeFeed = videoLargeFeed;
    }

    public String getVideoSmallFeed() {
        return mVideoSmallFeed;
    }

    public void setVideoSmallFeed(String videoSmallFeed) {
        mVideoSmallFeed = videoSmallFeed;
    }

    public String getAudioFeed() {
        return mAudioFeed;
    }

    public void setAudioFeed(String audioFeed) {
        mAudioFeed = audioFeed;
    }

    public boolean hasLoadedAllEpisodes() {
        return mLoadedAllEpisodes;
    }

    public void setLoadedAllEpisodes(boolean loadedAllEpisodes) {
        mLoadedAllEpisodes = loadedAllEpisodes;
    }
}
