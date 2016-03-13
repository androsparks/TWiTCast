package com.tragicfruit.twitcast.misc;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.tragicfruit.twitcast.R;

/**
 * Created by Jeremy on 23/11/2015.
 */
public abstract class SingleFragmentActivity extends GoogleCastActivity {
    private static final String KEY_PROGRESS_SHOWN = "progress_shown";

    private ProgressBar mConnectingProgressBar;

    protected abstract Fragment createFragment();

    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.activity_fragment;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        mConnectingProgressBar = (ProgressBar) findViewById(R.id.cast_device_connecting_progress_bar);
        if (savedInstanceState != null) {
            boolean progressShown = savedInstanceState.getBoolean(KEY_PROGRESS_SHOWN);
            if (progressShown) {
                mConnectingProgressBar.setVisibility(View.VISIBLE);
            } else {
                mConnectingProgressBar.setVisibility(View.INVISIBLE);
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction()
                    .add(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_PROGRESS_SHOWN, mConnectingProgressBar.isShown());
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
