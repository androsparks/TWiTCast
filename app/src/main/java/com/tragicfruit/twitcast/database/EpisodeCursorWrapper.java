package com.tragicfruit.twitcast.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.show.Show;
import com.tragicfruit.twitcast.database.TWiTDbSchema.EpisodeTable;

import java.util.Date;

/**
 * Created by Jeremy on 5/03/2016.
 */
public class EpisodeCursorWrapper extends CursorWrapper {
    private TWiTLab mDatabase;

    public EpisodeCursorWrapper(Cursor cursor, TWiTLab database) {
        super(cursor);
        mDatabase = database;
    }

    public Episode getEpisode() {
        String title = getString(getColumnIndex(EpisodeTable.Cols.TITLE));
        long publicationDate = getLong(getColumnIndex(EpisodeTable.Cols.PUBLICATION_DATE));
        String subtitle = getString(getColumnIndex(EpisodeTable.Cols.SUBTITLE));
        String showNotes = getString(getColumnIndex(EpisodeTable.Cols.SHOW_NOTES));
        String videoHdUrl = getString(getColumnIndex(EpisodeTable.Cols.VIDEO_HD_URL));
        String videoLargeUrl = getString(getColumnIndex(EpisodeTable.Cols.VIDEO_LARGE_URL));
        String videoSmallUrl = getString(getColumnIndex(EpisodeTable.Cols.VIDEO_SMALL_URL));
        String audioUrl = getString(getColumnIndex(EpisodeTable.Cols.AUDIO_URL));
        String runningTime = getString(getColumnIndex(EpisodeTable.Cols.RUNNING_TIME));
        int showId = getInt(getColumnIndex(EpisodeTable.Cols.SHOW_ID));

        Episode episode = new Episode();
        episode.setTitle(title);
        episode.setPublicationDate(new Date(publicationDate));
        episode.setSubtitle(subtitle);
        episode.setShowNotes(showNotes);
        episode.setVideoHdUrl(videoHdUrl);
        episode.setVideoLargeUrl(videoLargeUrl);
        episode.setVideoSmallUrl(videoSmallUrl);
        episode.setAudioUrl(audioUrl);
        episode.setRunningTime(runningTime);

        Show show = mDatabase.getShow(showId);
        episode.setShow(show);

        return episode;
    }
}
