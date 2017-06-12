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
    public static final String LOGO_URL = "https://www.dropbox.com/s/5tnbxitzu2rcxs9/twitlogo_600x600.png?raw=1";
    public static final String LOGO_SMALL_URL = "https://www.dropbox.com/s/348kv63v3179cog/twitlogo_300x300.png?raw=1";

    public static final String AUDIO_CONTENT_TYPE = "audio/mpeg3";
    public static final String VIDEO_CONTENT_TYPE = "video/mp4";
    public static final String LIVE_AUDIO_CONTENT_TYPE = "audio/mpeg";
    public static final String LIVE_VIDEO_CONTENT_TYPE = "video";

    public static final StreamQuality DEFAULT_QUALITY = StreamQuality.VIDEO_HD;
    public static final StreamSource DEFAULT_SOURCE = StreamSource.FLOSOFT_HIGH;

    public static final int MAX_NUMBER_OF_EPISODES = 250;

    public static final int[] EXCLUDED_SHOWS = {
            1683, // TWiT Bits
            1647, // Radio Leo
            65161 // All TWiT.tv Shows
    };

    public static final String STREAM_FLOSOFT_HIGH = "http://hls.twit.tv/flosoft/smil:twitStreamHi.smil/playlist.m3u8";
    public static final String STREAM_FLOSOFT_LOW = "http://hls.twit.tv/flosoft/smil:twitStream.smil/playlist.m3u8";
    public static final String STREAM_FLOSOFT_ABR = "http://hls.twit.tv/flosoft/smil:twitStreamAll.smil/playlist.m3u8";
    public static final String STREAM_TWITCH_HD = "http://video-edge-8fab14.syd01.hls.ttvnw.net/v0/CscBK-k3Nz5XUHCXV64ZMuEOf0AWQ837p-WYb5BPqFW9bCW4MwmUqgxfOELdcCzxl3t9byVY1s_siTRAxwsYqVhHURGde2llfLTvokt8LbQHmCQzvpLLvM91Jzc0FsHQewjb3ypLySMf7dMq3gQkudlvX-iXdZTiNL9xQ98ykYKmJ721fBzLI8mbKOQInEhUXnNVIIMPyWYol_XdRoxn4SG3dJ7d6drHF5i1M26Zk4j-g3m4GH-NsM2sVOHpfBiyke5Zx99wBh63zhIQvNJhRyQL0F9WkpcxgrpQLBoMBJ6mDsXM4ymbh-n3/index-live.m3u8";
    public static final String STREAM_TWITCH_HIGH = "http://video-edge-8fab14.syd01.hls.ttvnw.net/v0/CscBVZUT_PohyRgeQ4ZH77XYHnuEXPvtRqTsool4XtpLRckdruSde73_KPyPybyVVF21Os_EpqPrd-VEnjvW6e0yshfFYlv23Ry56CcHS8Hy7NTlixqQj5wO0nbhm4gvuCjfO6Eozww5wLf9uKaMID6cwBxvYRr0a3-oAmX6IXCYWcRXbJ4ofiiSoKEnP7ze8CytesmkTTPVb29okdXfIowOuzVjR6hfH-Q_xwNj4zvyYsqwVPI7BYuUfaSdVn_aMd4xKHGBHcl6pxIQfiNthTdfbBOnjYTviGSStxoMPMo2G1ncIJh3c_7I/index-live.m3u8";
    public static final String STREAM_TWITCH_LOW = "http://video-edge-8fab14.syd01.hls.ttvnw.net/v0/CscBpsTydRT64dMac1oFmHTRxBhoCIX-q3pO72sevptCI6zE31wiQBdECSXCihLREf4utywUbzXj1rU2CdK9yK5x1cTR_1mtJcFtYpMYFQ8QoC_Mwg1BZ9ot8MgeohTFD74WNOgzx7oS2c15BXVb_I8Y4LrpTLsdFTjosk_FVB7X5F0uVRmSNVDVphMsPuryM4TbWhhsuiSSvWKC_4lF5i6hA2k9zwmqcCvX4TcFgDpe0I7H6EyV0Aie0_fumme3dfGmKXBjs3PqlRIQta-h61v9-lkFrVIUD-kXCBoM4ztQEB_fgmWHSx5m/index-live.m3u8";
    public static final String STREAM_USTREAM = "http://iphone-streaming.ustream.tv/uhls/1524/streams/live/iphone/playlist.m3u8";
    public static final String STREAM_AUDIO = "http://twit.am/listen";

    public static final String GOOGLE_CALENDAR_ID = "mg877fp19824mj30g497frm74o@group.calendar.google.com";

    public static final String TWITTER_USERNAME = "tragicfruit";
}
