package com.tragicfruit.twitcast.show;

import android.support.v4.app.Fragment;

import com.tragicfruit.twitcast.misc.GoogleCastActivity;

public class ShowListActivity extends GoogleCastActivity {

    @Override
    protected Fragment createFragment() {
        return ShowListFragment.newInstance();
    }
}
