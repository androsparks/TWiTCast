package com.tragicfruit.twitcast;

import android.support.v4.app.Fragment;

public class ShowListActivity extends GoogleCastActivity {

    @Override
    protected Fragment createFragment() {
        return ShowListFragment.newInstance();
    }
}
