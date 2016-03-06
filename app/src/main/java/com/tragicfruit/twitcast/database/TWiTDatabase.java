package com.tragicfruit.twitcast.database;

import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.show.Show;

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
