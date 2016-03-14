package com.tragicfruit.twitcast.episode;

import android.text.format.DateFormat;

import com.tragicfruit.twitcast.show.Show;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
    private String mAudioUrl;
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

    public String getDisplayDate() {
        // today
        Calendar calendar = new GregorianCalendar();

        // midnight today
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (mPublicationDate.after(calendar.getTime())) {
            return "Today";
        }

        // midnight yesterday
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "Yesterday";
        }

        // midnight 2 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "2 days ago";
        }

        // midnight 3 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "3 days ago";
        }

        // midnight 4 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "4 days ago";
        }

        // midnight 5 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "5 days ago";
        }

        // midnight 6 days ago
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        if (mPublicationDate.after(calendar.getTime())) {
            return "6 days ago";
        }

        return DateFormat.format("MMM d", mPublicationDate).toString();
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

    public String getAudioUrl() {
        return mAudioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        mAudioUrl = audioUrl;
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

    public String getShortTitle() {
        return mTitle.replace(mShow.getTitle(), mShow.getShortCode());
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
