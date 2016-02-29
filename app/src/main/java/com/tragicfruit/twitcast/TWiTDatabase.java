package com.tragicfruit.twitcast;

import android.util.Log;

import java.util.List;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class TWiTDatabase {
    private static final String TAG = "TWiTDatabase";

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

    public void addEpisode(Episode episode) {
        Show showForEpisode = getShowFromEpisode(episode);
        if (showForEpisode != null) {
            showForEpisode.addEpisode(episode);
            Log.d(TAG, episode.getTitle() + " added to " + showForEpisode.getTitle());
        } else {
            Log.d(TAG, "No show found for " + episode.getTitle());
        }
    }

    private Show getShowFromEpisode(Episode episode) {
        for (Show show: mShows) {
            String showCleanPath = show.getCleanPath();
            String episodeCleanPath = episode.getCleanPath();

            if (episodeCleanPath.contains(showCleanPath)) {
                return show;
            }
        }

        return null;
    }

    public int getEpisodeCount() {
        return mEpisodeCount;
    }
}
