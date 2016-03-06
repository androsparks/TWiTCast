package com.tragicfruit.twitcast.database;

/**
 * Created by Jeremy on 5/03/2016.
 */
public class TWiTDbSchema {
    public static final class ShowTable {
        public static final String NAME = "shows";

        public static final class Cols {
            public static final String TITLE = "title";
            public static final String COVER_ART_SMALL_URL = "cover_art_small_url";
            public static final String COVER_ART_URL = "cover_art_url";
            public static final String COVER_ART_LARGE_URL = "cover_art_large_url";
            public static final String COVER_ART_LOCAL_PATH = "cover_art_local_path";
            public static final String ID = "id";
            public static final String DESCRIPTION = "description";
            public static final String VIDEO_HD_FEED = "video_hd_feed";
            public static final String VIDEO_LARGE_FEED = "video_large_feed";
            public static final String VIDEO_SMALL_FEED = "video_small_feed";
            public static final String AUDIO_FEED = "audio_feed";
            public static final String LOADED_ALL_EPISODES = "loaded_all_episodes";
        }
    }

    public static final class EpisodeTable {
        public static final String NAME = "episodes";

        public static final class Cols {
            public static final String TITLE = "title";
            public static final String PUBLICATION_DATE = "publication_date";
            public static final String SUBTITLE = "subtitle";
            public static final String SHOW_NOTES = "show_notes";
            public static final String VIDEO_HD_URL = "video_hd_url";
            public static final String VIDEO_LARGE_URL = "video_large_url";
            public static final String VIDEO_SMALL_URL = "video_small_url";
            public static final String AUDIO_URL = "audio_url";
            public static final String RUNNING_TIME = "running_time";
            public static final String SHOW_ID = "show_id";
        }
    }
}
