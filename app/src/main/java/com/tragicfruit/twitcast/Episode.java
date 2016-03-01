package com.tragicfruit.twitcast;

import java.util.Date;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class Episode {
    private String mTitle;
    private int mEpisodeNumber;
    private Date mAiringDate;
    private String mShowNotes;
    private String mVideoHdUrl;
    private String mVideoLargeUrl;
    private String mVideoSmallUrl;
    private String mVideoAudioUrl;
    private int mShowId;
    private int mRunningTimeInMinutes;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getEpisodeNumber() {
        return mEpisodeNumber;
    }

    public void setEpisodeNumber(int episodeNumber) {
        mEpisodeNumber = episodeNumber;
    }

    public Date getAiringDate() {
        return mAiringDate;
    }

    public void setAiringDate(Date airingDate) {
        mAiringDate = airingDate;
    }

    public String getShowNotes() {
        return mShowNotes;
    }

    public void setShowNotes(String showNotes) {
        mShowNotes = showNotes;
    }

    public String getVideoHdUrl() {
        return mVideoHdUrl;
    }

    public void setVideoHdUrl(String videoHdUrl) {
        mVideoHdUrl = videoHdUrl;
    }

    public String getVideoLargeUrl() {
        return mVideoLargeUrl;
    }

    public void setVideoLargeUrl(String videoLargeUrl) {
        mVideoLargeUrl = videoLargeUrl;
    }

    public String getVideoSmallUrl() {
        return mVideoSmallUrl;
    }

    public void setVideoSmallUrl(String videoSmallUrl) {
        mVideoSmallUrl = videoSmallUrl;
    }

    public String getVideoAudioUrl() {
        return mVideoAudioUrl;
    }

    public void setVideoAudioUrl(String videoAudioUrl) {
        mVideoAudioUrl = videoAudioUrl;
    }

    public int getShowId() {
        return mShowId;
    }

    public void setShowId(int showId) {
        mShowId = showId;
    }

    public int getRunningTimeInMinutes() {
        return mRunningTimeInMinutes;
    }

    public void setRunningTimeInMinutes(int runningTimeInMinutes) {
        mRunningTimeInMinutes = runningTimeInMinutes;
    }
}
