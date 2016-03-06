package com.tragicfruit.twitcast.database;

import com.tragicfruit.twitcast.Episode;
import com.tragicfruit.twitcast.Show;

import java.util.List;

/**
 * Created by Jeremy on 6/03/2016.
 */
public interface TWiTDatabase {
    List<Show> loadShows();

    void saveShows();

    List<Episode> loadEpisodes();

    void saveEpisodes();
}
