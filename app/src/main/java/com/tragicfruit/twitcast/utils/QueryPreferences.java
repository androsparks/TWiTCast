package com.tragicfruit.twitcast.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;

import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.episode.StreamQuality;
import com.tragicfruit.twitcast.stream.StreamSource;

/**
 * Created by Jeremy on 6/03/2016.
 */
public class QueryPreferences {
    private static final String TAG = QueryPreferences.class.getSimpleName();

    private static final String PREF_STREAM_QUALITY = "stream_quality";
    private static final String PREF_STREAM_SOURCE = "stream_source";
    private static final String PREF_CAST_DEVICE_AUDIO = "cast_device_audio";
    private static final String PREF_GRID_SPAN_COUNT = "grid_span_count";
    private static final String PREF_FORCE_REFETCH_SHOWS = "force_refetch";

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

    public static StreamSource getStreamSource(Context context) {
        try {
            String streamSourceValue = PreferenceManager.getDefaultSharedPreferences(context)
                    .getString(PREF_STREAM_SOURCE, Constants.DEFAULT_SOURCE.toString());
            return StreamSource.valueOf(streamSourceValue);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Invalid stream source, setting to default");
            setStreamSource(context, Constants.DEFAULT_SOURCE);
            return Constants.DEFAULT_SOURCE;
        }
    }

    public static void setStreamSource(Context context, StreamSource source) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putString(PREF_STREAM_SOURCE, source.toString())
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

    public static int getGridSpanCount(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getInt(PREF_GRID_SPAN_COUNT, 2);
    }

    public static void setGridSpanCount(Context context, int spanCount) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(PREF_GRID_SPAN_COUNT, spanCount)
                .apply();
    }

    public static boolean getForceRefetchShows(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_FORCE_REFETCH_SHOWS, true);
    }

    public static void setForceRefetchShows(Context context, boolean force) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_FORCE_REFETCH_SHOWS, force)
                .apply();
    }
}
