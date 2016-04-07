package com.tragicfruit.twitcast.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.util.Log;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.database.TWiTDbSchema.EpisodeTable;
import com.tragicfruit.twitcast.database.TWiTDbSchema.ShowTable;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.show.Show;
import com.tragicfruit.twitcast.stream.Stream;
import com.tragicfruit.twitcast.utils.QueryPreferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class TWiTLab implements TWiTDatabase {
    private static final String TAG = "TWiTDatabase";

    private List<Show> mShows;
    private List<Episode> mEpisodes;
    private ConcurrentMap<String, Stream> mStreams;
    private static TWiTLab sTWiTLab;

    private Context mContext;
    private SQLiteDatabase mSQLiteDatabase;

    public static TWiTLab get(Context context) {
        if (sTWiTLab == null) {
            sTWiTLab = new TWiTLab(context);
        }

        return sTWiTLab;
    }

    private TWiTLab(Context context) {
        mContext = context.getApplicationContext();
        mSQLiteDatabase = new TWiTBaseHelper(mContext).getWritableDatabase();

        try {
            mShows = loadShows();
            loadCoverArt();
        } catch (Exception e) {
            mShows = new ArrayList<>();
            Log.e(TAG, "Error loading shows", e);
        }

        try {
            mEpisodes = loadEpisodes();
        } catch (Exception e) {
            mEpisodes = new ArrayList<>();
            Log.e(TAG, "Error loading episodes", e);
        }

        try {
            linkShowsAndEpisodes();
        } catch (Exception e) {
            Log.e(TAG, "Error linking shows and episodes", e);
            mShows = new ArrayList<>();
            mEpisodes = new ArrayList<>();
        }

        mStreams = loadStreams();
    }

    // TODO: currently hardcoded, need to get streams from TWiT API
    private ConcurrentMap<String, Stream> loadStreams() {
        ConcurrentMap<String, Stream> streams = new ConcurrentHashMap<>();

        // BitGravity High
        Stream stream = new Stream();
        stream.setTitle(mContext.getString(R.string.bitgravity_high_stream));
        stream.setSource(Constants.STREAM_BIT_GRAVITY_HIGH);
        stream.setType(Constants.LIVE_VIDEO_CONTENT_TYPE);
        streams.put(stream.getTitle(), stream);

        // BitGravity Low
        stream = new Stream();
        stream.setTitle(mContext.getString(R.string.bitgravity_low_stream));
        stream.setSource(Constants.STREAM_BIT_GRAVITY_LOW);
        stream.setType(Constants.LIVE_VIDEO_CONTENT_TYPE);
        streams.put(stream.getTitle(), stream);

        // Flosoft
        stream = new Stream();
        stream.setTitle(mContext.getString(R.string.flosoft_stream));
        stream.setSource(Constants.STREAM_FLOSOFT);
        stream.setType(Constants.LIVE_VIDEO_CONTENT_TYPE);
        streams.put(stream.getTitle(), stream);

        // Audio
        stream = new Stream();
        stream.setTitle(mContext.getString(R.string.audio_stream));
        stream.setSource(Constants.STREAM_AUDIO);
        stream.setType(Constants.LIVE_AUDIO_CONTENT_TYPE);
        streams.put(stream.getTitle(), stream);

        return streams;
    }

    public ConcurrentMap<String, Stream> getStreams() {
        return mStreams;
    }

    private void loadCoverArt() {
        double reduceFactor = 1.0 / QueryPreferences.getGridSpanCount(mContext);
        for (Show show : mShows) {
            show.setCoverArt(show.getCoverArtLocalPath(), mContext, reduceFactor);
        }
    }

    private void linkShowsAndEpisodes() {
        for (Episode episode : mEpisodes) {
            Show showForEpisode = episode.getShow();
            if (showForEpisode != null) {
                showForEpisode.addEpisode(episode);
            } else {
                Log.e(TAG, "No show found for " + episode.getTitle() + " - removed.");
                mEpisodes.remove(episode);
            }
        }
    }

    public List<Show> getShows() {
        return mShows;
    }

    public void setShows(List<Show> shows) {
        mShows = shows;
    }

    public Show getShow(int id) {
        for (Show show: mShows) {
            if (show.getId() == id) {
                return show;
            }
        }
        return null;
    }

    public List<Episode> getEpisodes() {
        return mEpisodes;
    }

    public boolean addEpisodes(List<Episode> episodeList) {
        boolean newEpisodes = false;
        for (Episode episode : episodeList) {
            Show showForEpisode = getShowFromEpisode(episode);

            if (showForEpisode == null) {
                Log.d(TAG, "No show found for " + episode.getTitle());
                continue;
            }

            if (mEpisodes.contains(episode)) {
                int index = mEpisodes.indexOf(episode);
                if (episodeHasAllUrls(mEpisodes.get(index))) {
                    continue;
                } else {
                    mEpisodes.remove(episode);
                    showForEpisode.removeEpisode(episode);
                }
            }

            newEpisodes = true;
            mEpisodes.add(episode);
            showForEpisode.addEpisode(episode);
            episode.setShow(showForEpisode);
            Log.d(TAG, episode.getTitle() + " added to " + showForEpisode.getTitle());

            sortEpisodes(mEpisodes);
            sortEpisodes(showForEpisode.getEpisodes());
        }

        cleanUpOldShows();

        return newEpisodes;
    }

    public boolean addEpisodes(List<Episode> episodeList, Show show) {
        boolean newEpisodes = false;
        for (Episode episode : episodeList) {
            episode.setShow(show);
            episode.cleanTitle();

            if (mEpisodes.contains(episode)) {
                int index = mEpisodes.indexOf(episode);
                if (episodeHasAllUrls(mEpisodes.get(index))) {
                    continue;
                } else {
                    mEpisodes.remove(episode);
                    show.removeEpisode(episode);
                }
            }

            newEpisodes = true;
            mEpisodes.add(episode);
            show.addEpisode(episode);
            Log.d(TAG, episode.getTitle() + " added to " + show.getTitle());
        }

        sortEpisodes(mEpisodes);
        sortEpisodes(show.getEpisodes());

        cleanUpOldShows();

        return newEpisodes;
    }

    private void cleanUpOldShows() {
        if (mEpisodes.size() <= Constants.MAX_NUMBER_OF_EPISODES) {
            return;
        }

        while (mEpisodes.size() > Constants.MAX_NUMBER_OF_EPISODES) {
            Episode removedEpisode = mEpisodes.remove(mEpisodes.size() - 1);
            removedEpisode.getShow().getEpisodes().remove(removedEpisode);

            Log.d(TAG, "Clean up: removed " + removedEpisode.getTitle());
        }
    }

    private void sortEpisodes(List<Episode> episodes) {
        Collections.sort(episodes, new Comparator<Episode>() {
            @Override
            public int compare(Episode lhs, Episode rhs) {
                long firstDate = lhs.getPublicationDate().getTime();
                long secondDate = rhs.getPublicationDate().getTime();

                return firstDate > secondDate ? -1 : (lhs == rhs ? 0 : 1);
            }
        });
    }

    private boolean episodeHasAllUrls(Episode episode) {
        return episode.getVideoHdUrl() != null && episode.getVideoLargeUrl() != null &&
                episode.getVideoSmallUrl() != null && episode.getAudioUrl() != null;
    }

    private Show getShowFromEpisode(Episode episode) {
        for (Show show: mShows) {
            String episodeTitle = episode.getTitle();
            String showTitle = show.getTitle();

            if (episodeTitle.contains(showTitle)) {
                return show;
            }
        }

        return null;
    }

    public boolean isExcludedShow(Show show) {
        int showId = show.getId();

        for (int excludedShowId: Constants.EXCLUDED_SHOWS) {
            if (showId == excludedShowId) {
                return true;
            }
        }

        return false;
    }

    public void resetEpisodes() {
        mEpisodes = new ArrayList<>();
        for (Show show: mShows) {
            show.setLoadedAllEpisodes(false);
        }
    }

    public List<Show> loadShows() {
        List<Show> shows = new ArrayList<>();

        ShowCursorWrapper cursor = queryShows(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                shows.add(cursor.getShow());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        Log.i(TAG, "Loaded shows from database");
        return shows;
    }

    public void saveShows() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mSQLiteDatabase.delete(ShowTable.NAME, null, null);
                for (Show show : mShows) {
                    ContentValues values = getContentValues(show);

                    mSQLiteDatabase.insert(ShowTable.NAME, null, values);
                }

                Log.i(TAG, "Saved shows to database");
                return null;
            }
        }.execute();
    }

    private ShowCursorWrapper queryShows(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(
                ShowTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new ShowCursorWrapper(cursor);
    }

    private EpisodeCursorWrapper queryEpisodes(String whereClause, String[] whereArgs) {
        Cursor cursor = mSQLiteDatabase.query(
                EpisodeTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null // orderBy
        );

        return new EpisodeCursorWrapper(cursor, this);
    }

    public List<Episode> loadEpisodes() {
        List<Episode> episodes = new ArrayList<>();

        EpisodeCursorWrapper cursor = queryEpisodes(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                episodes.add(cursor.getEpisode());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        Log.i(TAG, "Loaded episodes from database");
        return episodes;
    }

    public void saveEpisodes() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                mSQLiteDatabase.delete(EpisodeTable.NAME, null, null);
                for (Episode episode : mEpisodes) {
                    ContentValues values = getContentValues(episode);

                    mSQLiteDatabase.insert(EpisodeTable.NAME, null, values);
                }

                Log.i(TAG, "Saved episodes to database");
                return null;
            }
        }.execute();
    }

    private static ContentValues getContentValues(Show show) {
        ContentValues values = new ContentValues();
        values.put(ShowTable.Cols.TITLE, show.getTitle());
        values.put(ShowTable.Cols.SHORT_CODE, show.getShortCode());
        values.put(ShowTable.Cols.COVER_ART_URL, show.getCoverArtUrl());
        values.put(ShowTable.Cols.COVER_ART_LOCAL_PATH, show.getCoverArtLocalPath());
        values.put(ShowTable.Cols.ID, show.getId());
        values.put(ShowTable.Cols.DESCRIPTION, show.getDescription());
        values.put(ShowTable.Cols.VIDEO_HD_FEED, show.getVideoHdFeed());
        values.put(ShowTable.Cols.VIDEO_LARGE_FEED, show.getVideoLargeFeed());
        values.put(ShowTable.Cols.VIDEO_SMALL_FEED, show.getVideoSmallFeed());
        values.put(ShowTable.Cols.AUDIO_FEED, show.getAudioFeed());
        values.put(ShowTable.Cols.LOADED_ALL_EPISODES, show.hasLoadedAllEpisodes() ? 1 : 0); // need to test this
        return values;
    }

    private static ContentValues getContentValues(Episode episode) {
        ContentValues values = new ContentValues();
        values.put(EpisodeTable.Cols.TITLE, episode.getTitle());
        values.put(EpisodeTable.Cols.PUBLICATION_DATE, episode.getPublicationDate().getTime()); // need to test this
        values.put(EpisodeTable.Cols.SUBTITLE, episode.getSubtitle());
        values.put(EpisodeTable.Cols.SHOW_NOTES, episode.getShowNotes());
        values.put(EpisodeTable.Cols.VIDEO_HD_URL, episode.getVideoHdUrl());
        values.put(EpisodeTable.Cols.VIDEO_LARGE_URL, episode.getVideoLargeUrl());
        values.put(EpisodeTable.Cols.VIDEO_SMALL_URL, episode.getVideoSmallUrl());
        values.put(EpisodeTable.Cols.AUDIO_URL, episode.getAudioUrl());
        values.put(EpisodeTable.Cols.RUNNING_TIME, episode.getRunningTime());
        values.put(EpisodeTable.Cols.SHOW_ID, episode.getShow().getId()); // need to test this
        return values;
    }
}
