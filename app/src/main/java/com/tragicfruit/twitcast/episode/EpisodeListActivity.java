package com.tragicfruit.twitcast.episode;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.misc.GoogleCastActivity;
import com.tragicfruit.twitcast.misc.SingleFragmentActivity;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class EpisodeListActivity extends SingleFragmentActivity implements EpisodeListFragment.Callbacks {
    private static final String EXTRA_SHOW_ID = "nz.co.tragicfruit.twitcast.show_id";

    private Toolbar mToolbar;

    public static Intent newIntent(Context context, int showId) {
        Intent i = new Intent(context, EpisodeListActivity.class);
        i.putExtra(EXTRA_SHOW_ID, showId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected Fragment createFragment() {
        int showId = getIntent().getIntExtra(EXTRA_SHOW_ID, 0);
        return EpisodeListFragment.newInstance(showId);
    }

    @Override
    public void showNoConnectionSnackbar() {
        Snackbar.make(findViewById(R.id.fragment_container),
                R.string.no_connection_snackbar,
                Snackbar.LENGTH_LONG)
                .show();
    }

    @Override
    public void setToolbarColour(int toolbarColour, int statusBarColour) {
        mToolbar.setBackgroundColor(toolbarColour);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(statusBarColour);
        }
    }
}
