package com.tragicfruit.twitcast;

import java.util.List;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class TWiTDatabase {
    private List<Show> mShows;
    private static TWiTDatabase sTWiTDatabase;

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
}
