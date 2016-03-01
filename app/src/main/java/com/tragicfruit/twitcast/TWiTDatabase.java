package com.tragicfruit.twitcast;

import android.util.Log;

import java.util.List;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class TWiTDatabase {
    private static final String TAG = "TWiTDatabase";

    private int[] mExcludedShows = {
            1683, // TWiT Bits
            1647, // Radio Leo
            65161 // All TWiT.tv Shows
    };
    private List<Show> mShows;
    private static TWiTDatabase sTWiTDatabase;

    private int mEpisodeCount;

    public static TWiTDatabase get() {
        if (sTWiTDatabase == null) {
            sTWiTDatabase = new TWiTDatabase();
        }

        return sTWiTDatabase;
    }

    private TWiTDatabase() {

    }

    public List<Show> getShows() {
        return mShows;
    }

    public void setShows(List<Show> shows) {
        mShows = shows;
    }

    public Show getShow(int id) {
        for (Show show: mShows) {
            if (show.getId() == id) {
                return show;
            }
        }
        return null;
    }

    public void addEpisode(Episode episode) {
        Show showForEpisode = getShowFromEpisode(episode);
        if (showForEpisode != null) {
            showForEpisode.addEpisode(episode);
            mEpisodeCount += 1;
            Log.d(TAG, episode.getTitle() + " added to " + showForEpisode.getTitle());
        } else {
            Log.d(TAG, "No show found for " + episode.getTitle());
        }
    }

    private Show getShowFromEpisode(Episode episode) {
        for (Show show: mShows) {
            int showId = show.getId();
            int episodeShowId = episode.getShowId();

            if (showId == episodeShowId) {
                return show;
            }
        }

        return null;
    }

    public int getEpisodeCount() {
        return mEpisodeCount;
    }

    public boolean isExcludedShow(Show show) {
        int showId = show.getId();

        for (int excludedShowId: mExcludedShows) {
            if (showId == excludedShowId) {
                return true;
            }
        }

        return false;
    }
}
