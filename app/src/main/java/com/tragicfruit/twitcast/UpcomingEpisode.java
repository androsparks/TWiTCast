package com.tragicfruit.twitcast;

import java.util.Date;

/**
 * Created by Jeremy on 14/03/2016.
 */
public class UpcomingEpisode {
    private Date mAiringDate;
    private String mTitle;

    public Date getAiringDate() {
        return mAiringDate;
    }

    public void setAiringDate(Date airingDate) {
        mAiringDate = airingDate;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }
}
