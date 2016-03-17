package com.tragicfruit.twitcast.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.tragicfruit.twitcast.database.TWiTDbSchema.ShowTable;
import com.tragicfruit.twitcast.show.Show;

/**
 * Created by Jeremy on 5/03/2016.
 */
public class ShowCursorWrapper extends CursorWrapper {

    public ShowCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Show getShow() {
        String title = getString(getColumnIndex(ShowTable.Cols.TITLE));
        String shortCode = getString(getColumnIndex(ShowTable.Cols.SHORT_CODE));
        String coverArtUrl = getString(getColumnIndex(ShowTable.Cols.COVER_ART_URL));
        String coverArtLocalPath = getString(getColumnIndex(ShowTable.Cols.COVER_ART_LOCAL_PATH));
        int id = getInt(getColumnIndex(ShowTable.Cols.ID));
        String description = getString(getColumnIndex(ShowTable.Cols.DESCRIPTION));
        String videoHdFeed = getString(getColumnIndex(ShowTable.Cols.VIDEO_HD_FEED));
        String videoLargeFeed = getString(getColumnIndex(ShowTable.Cols.VIDEO_LARGE_FEED));
        String videoSmallFeed = getString(getColumnIndex(ShowTable.Cols.VIDEO_SMALL_FEED));
        String audioFeed = getString(getColumnIndex(ShowTable.Cols.AUDIO_FEED));
        int loadedAllEpisodes = getInt(getColumnIndex(ShowTable.Cols.LOADED_ALL_EPISODES));

        Show show = new Show();
        show.setTitle(title);
        show.setShortCode(shortCode);
        show.setCoverArtUrl(coverArtUrl);
        show.setCoverArtLocalPath(coverArtLocalPath);
        show.setId(id);
        show.setDescription(description);
        show.setVideoHdFeed(videoHdFeed);
        show.setVideoLargeFeed(videoLargeFeed);
        show.setVideoSmallFeed(videoSmallFeed);
        show.setAudioFeed(audioFeed);
        show.setLoadedAllEpisodes(loadedAllEpisodes != 0);

        return show;
    }
}
