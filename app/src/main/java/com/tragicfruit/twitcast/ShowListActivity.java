package com.tragicfruit.twitcast;

import android.support.v4.app.Fragment;

public class ShowListActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return ShowListFragment.newInstance();
    }
}
