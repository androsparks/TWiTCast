package com.tragicfruit.twitcast.misc;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;

import com.tragicfruit.twitcast.stream.LiveStreamFragment;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.episode.LatestEpisodesFragment;
import com.tragicfruit.twitcast.show.ShowListFragment;

public class MenuPagerActivity extends GoogleCastActivity
        implements ShowListFragment.Callbacks, LatestEpisodesFragment.Callbacks {
    private static final String KEY_PROGRESS_SHOWN = "progress_shown";

    private static final Fragment[] mFragments = {
            ShowListFragment.newInstance(),
            LatestEpisodesFragment.newInstance(),
            LiveStreamFragment.newInstance()
    };

    private static final int[] mFragmentTitles = {
            R.string.show_list_fragment_tab_title,
            R.string.latest_episodes_tab_title,
            R.string.live_stream_tab_title
    };

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private ProgressBar mConnectingProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_pager);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mViewPager = (ViewPager) findViewById(R.id.activity_menu_pager_view_pager);
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments[position];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(mFragmentTitles[position]);
            }
        });

        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(mViewPager);
        }

        mConnectingProgressBar = (ProgressBar) findViewById(R.id.cast_device_connecting_progress_bar);
        if (savedInstanceState != null) {
            boolean progressShown = savedInstanceState.getBoolean(KEY_PROGRESS_SHOWN);
            if (progressShown) {
                mConnectingProgressBar.setVisibility(View.VISIBLE);
            } else {
                mConnectingProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PROGRESS_SHOWN, mConnectingProgressBar.isShown());
    }

    @Override
    public void refreshLatestEpisodes() {
        LatestEpisodesFragment latestEpisodesFragment = null;
        for (Fragment fragment : mFragments) {
            if (fragment instanceof LatestEpisodesFragment) {
                latestEpisodesFragment = (LatestEpisodesFragment) fragment;
                break;
            }
        }

        if (latestEpisodesFragment != null) {
            latestEpisodesFragment.updateList();
        }
    }

    @Override
    public void showNoConnectionSnackbar() {
        View view = findViewById(R.id.activity_menu_pager_layout);
        if (view != null) {
            Snackbar.make(view,
                    R.string.no_connection_snackbar,
                    Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void showProgressBar() {
        mConnectingProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        mConnectingProgressBar.setVisibility(View.INVISIBLE);
    }
}
