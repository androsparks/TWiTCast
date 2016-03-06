package com.tragicfruit.twitcast;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Jeremy on 6/03/2016.
 */
public class QueryPreferences {
    private static final String PREF_STREAM_QUALITY = "stream_quality";

    public static StreamQuality getStreamQuality(Context context) {
        String streamQualityValue = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_STREAM_QUALITY, StreamQuality.VIDEO_HD.toString());
        return StreamQuality.valueOf(streamQualityValue);
    }

    public static void setStreamQuality(Context context, StreamQuality quality) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_STREAM_QUALITY, quality.toString())
                .apply();
    }
}
