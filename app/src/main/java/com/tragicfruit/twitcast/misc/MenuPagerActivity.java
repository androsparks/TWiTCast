package com.tragicfruit.twitcast.misc;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.TabLayout;

import com.tragicfruit.twitcast.episode.LatestEpisodesFragment;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.show.ShowListFragment;

public class MenuPagerActivity extends GoogleCastActivity
        implements LatestEpisodesFragment.Callbacks, ShowListFragment.Callbacks {
    private static final Fragment[] mFragments = {
            ShowListFragment.newInstance(),
            LatestEpisodesFragment.newInstance()
    };

    private static final int[] mFragmentTitles = {
            R.string.show_list_fragment_tab_title,
            R.string.latest_episodes_tab_title
    };

    private ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;

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
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public void refreshShows() {
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
}
