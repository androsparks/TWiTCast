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
    private static final String PREF_CAST_DEVICE_AUDIO = "cast_device_audio";

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

    public static boolean isCastDeviceAudioOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_CAST_DEVICE_AUDIO, false);
    }

    public static void setCastDeviceAudioOnly(Context context, boolean audioOnly) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_CAST_DEVICE_AUDIO, audioOnly)
                .apply();
    }
}
