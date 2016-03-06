package com.tragicfruit.twitcast.show;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import com.tragicfruit.twitcast.LatestEpisodesFragment;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.misc.GoogleCastActivity;

public class MenuPagerActivity extends GoogleCastActivity implements LatestEpisodesFragment.Callbacks {
    private static final Fragment[] mFragments = {
            ShowListFragment.newInstance(),
            LatestEpisodesFragment.newInstance()
    };

    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_pager);

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
        });
    }
}
