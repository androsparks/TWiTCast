package com.tragicfruit.twitcast.constants;

import com.tragicfruit.twitcast.episode.StreamQuality;

/**
 * Created by Jeremy on 4/03/2016.
 */
public class Constants {
    public static final String BRICKHOUSE_AUDIO_FEED = "http://feeds.twit.tv/brickhouse.xml";
    public static final String BRICKHOUSE_VIDEO_SMALL_FEED = "http://feeds.twit.tv/brickhouse_video_small.xml";
    public static final String BRICKHOUSE_VIDEO_LARGE_FEED = "http://feeds.twit.tv/brickhouse_video_large.xml";
    public static final String BRICKHOUSE_VIDEO_HD_FEED = "http://feeds.twit.tv/brickhouse_video_hd.xml";

    public static final String COVER_ART_FOLDER = "cover_art";
    public static final String COVER_ART_LARGE_FOLDER = "cover_art_large";

    public static final String AUDIO_CONTENT_TYPE = "audio/mpeg3";
    public static final String VIDEO_CONTENT_TYPE = "video/mp4";

    public static final StreamQuality DEFAULT_QUALITY = StreamQuality.VIDEO_HD;

    public static final int MAX_NUMBER_OF_EPISODES = 250;

    public static final int[] EXCLUDED_SHOWS = {
            1683, // TWiT Bits
            1647, // Radio Leo
            65161 // All TWiT.tv Shows
    };
}
