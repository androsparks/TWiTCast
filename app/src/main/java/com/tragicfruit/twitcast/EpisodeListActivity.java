package com.tragicfruit.twitcast;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class EpisodeListActivity extends GoogleCastActivity {
    private static final String EXTRA_SHOW_ID = "nz.co.tragicfruit.twitcast.show_id";

    public static Intent newIntent(Context context, int showId) {
        Intent i = new Intent(context, EpisodeListActivity.class);
        i.putExtra(EXTRA_SHOW_ID, showId);
        return i;
    }

    @Override
    protected Fragment createFragment() {
        int showId = getIntent().getIntExtra(EXTRA_SHOW_ID, 0);
        return EpisodeListFragment.newInstance(showId);
    }
}
