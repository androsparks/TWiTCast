package com.tragicfruit.twitcast.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.episode.StreamQuality;

/**
 * Created by Jeremy on 6/03/2016.
 */
public class QueryPreferences {
    private static final String PREF_STREAM_QUALITY = "stream_quality";

    public static StreamQuality getStreamQuality(Context context) {
        String streamQualityValue = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_STREAM_QUALITY, Constants.DEFAULT_QUALITY.toString());
        return StreamQuality.valueOf(streamQualityValue);
    }

    public static void setStreamQuality(Context context, StreamQuality quality) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_STREAM_QUALITY, quality.toString())
                .apply();
    }
}
