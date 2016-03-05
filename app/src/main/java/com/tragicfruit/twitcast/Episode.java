package com.tragicfruit.twitcast;

import java.util.Date;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class Episode {
    private String mTitle;
    private Date mPublicationDate;
    private String mSubtitle;
    private String mShowNotes;
    private String mVideoHdUrl;
    private String mVideoLargeUrl;
    private String mVideoSmallUrl;
    private String mVideoAudioUrl;
    private String mRunningTime;
    private Show mShow;

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public Date getPublicationDate() {
        return mPublicationDate;
    }

    public void setPublicationDate(Date publicationDate) {
        mPublicationDate = publicationDate;
    }

    public String getSubtitle() {
        return mSubtitle;
    }

    public void setSubtitle(String subtitle) {
        mSubtitle = subtitle;
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

    public String getRunningTime() {
        return mRunningTime;
    }

    public void setRunningTime(String runningTime) {
        mRunningTime = runningTime;
    }

    public Show getShow() {
        return mShow;
    }

    public void setShow(Show show) {
        mShow = show;
    }

    public String getDisplayTitle() {
        return mTitle.replace(mShow.getTitle() + " ", "");
    }

    public void cleanTitle() {
        int indexOfFirstDigit = 0;

        for (int i = 0; i < mTitle.length(); i++) {
            char c = mTitle.charAt(i);
            if (Character.isDigit(c)) {
                indexOfFirstDigit = i;
                break;
            }
        }

        mTitle = mShow.getTitle() + " " + mTitle.substring(indexOfFirstDigit);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Episode) {
            Episode otherEpisode = (Episode) o;

            return mTitle.equals(otherEpisode.getTitle());
        } else {
            return false;
        }
    }
}
