package com.tragicfruit.twitcast.constants;

import com.tragicfruit.twitcast.episode.StreamQuality;
import com.tragicfruit.twitcast.stream.StreamSource;

/**
 * Created by Jeremy on 4/03/2016.
 */
public class Constants {
    public static final String TWIT_API_ID = "e71e1b45";
    public static final String TWIT_API_KEY = "6b602234dcdf7ead0fdc7e524c799b36";
    public static final String GOOGLE_CAST_APP_ID = "C5E513B9";
    public static final String GOOGLE_CALENDAR_API_KEY = "AIzaSyCk69iAvIOkeufkOHfWcsciYoaYSTbNIVU";

    public static final String BRICKHOUSE_AUDIO_FEED = "http://feeds.twit.tv/brickhouse.xml";
    public static final String BRICKHOUSE_VIDEO_SMALL_FEED = "http://feeds.twit.tv/brickhouse_video_small.xml";
    public static final String BRICKHOUSE_VIDEO_LARGE_FEED = "http://feeds.twit.tv/brickhouse_video_large.xml";
    public static final String BRICKHOUSE_VIDEO_HD_FEED = "http://feeds.twit.tv/brickhouse_video_hd.xml";

    public static final String COVER_ART_FOLDER = "cover_art";
    public static final String LOGO_FOLDER = "logo";
    public static final String LOGO_FILE = "twitlogo_1400x1400.png";
    public static final String LOGO_URL = "https://www.dropbox.com/s/s8rjrg51n47fv1f/twitlogo_1400x1400.png?raw=1";

    public static final String AUDIO_CONTENT_TYPE = "audio/mpeg3";
    public static final String VIDEO_CONTENT_TYPE = "video/mp4";
    public static final String LIVE_AUDIO_CONTENT_TYPE = "audio/mpeg";
    public static final String LIVE_VIDEO_CONTENT_TYPE = "video";

    public static final StreamQuality DEFAULT_QUALITY = StreamQuality.VIDEO_HD;
    public static final StreamSource DEFAULT_SOURCE = StreamSource.BIT_GRAVITY_HIGH;

    public static final int MAX_NUMBER_OF_EPISODES = 250;

    public static final int[] EXCLUDED_SHOWS = {
            1683, // TWiT Bits
            1647, // Radio Leo
            65161 // All TWiT.tv Shows
    };

    public static final String STREAM_BIT_GRAVITY_HIGH = "http://twit.live-s.cdn.bitgravity.com/cdn-live/_definst_/twit/live/high/playlist.m3u8";
    public static final String STREAM_BIT_GRAVITY_LOW = "http://twit.live-s.cdn.bitgravity.com/cdn-live/_definst_/twit/live/low/playlist.m3u8";
    public static final String STREAM_FLOSOFT = "http://hls.twit.tv/flosoft/smil:twitStreamAll.smil/playlist.m3u8";
    public static final String STREAM_AUDIO = "http://twit.am/listen";

    public static final String GOOGLE_CALENDAR_ID = "mg877fp19824mj30g497frm74o@group.calendar.google.com";

    public static final String TWITTER_USERNAME = "tragicfruit";
}
