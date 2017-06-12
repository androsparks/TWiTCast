package com.tragicfruit.twitcast.castv3;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;
import com.google.android.gms.cast.framework.media.widget.ExpandedControllerActivity;
import com.tragicfruit.twitcast.GoogleCastActivity;
import com.tragicfruit.twitcast.R;

/**
 * Created by Jeremy on 10/06/2017.
 */

public class PlayerActivity extends GoogleCastActivity {
    private View mPageView;
    private ImageButton mPlayPause;
    private TextView mStart;
    private TextView mEnd;
    private SeekBar mSeekbar;
    private TextView mLine1;
    private TextView mLine2;
    private ProgressBar mLoading;
    private double mVolumeIncrement;
    private View mControllers;
    private Drawable mPauseDrawable;
    private Drawable mPlayDrawable;
    private Drawable mStopDrawable;
    private int mStreamType;
    private ImageButton mClosedCaptionIcon;
    private ImageButton mForward;
    private ImageButton mRewind;
    private View mPlaybackControls;
    private Toolbar mToolbar;
    private boolean mImmersive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPauseDrawable = ContextCompat.getDrawable(this, R.drawable.ic_pause_circle_white_80dp);
        mPlayDrawable = ContextCompat.getDrawable(this, R.drawable.ic_play_circle_white_80dp);
        mStopDrawable = ContextCompat.getDrawable(this, R.drawable.ic_stop_circle_white_80dp);
        mPageView = findViewById(R.id.pageview);
        mPlayPause = (ImageButton) findViewById(R.id.play_pause_toggle);
        mStart = (TextView) findViewById(R.id.start_text);
        mEnd = (TextView) findViewById(R.id.end_text);
        mSeekbar = (SeekBar) findViewById(R.id.seekbar);
        mLine1 = (TextView) findViewById(R.id.textview1);
        mLine2 = (TextView) findViewById(R.id.textview2);
        mLoading = (ProgressBar) findViewById(R.id.progressbar1);
        mControllers = findViewById(R.id.controllers);
        mForward = (ImageButton) findViewById(R.id.forward);
        mRewind = (ImageButton) findViewById(R.id.rewind);
        mPlaybackControls = findViewById(R.id.playback_controls);

        UIMediaController uiMediaController = new UIMediaController(this);
        uiMediaController.bindImageViewToPlayPauseToggle(mPlayPause, mPlayDrawable, mPauseDrawable, mStopDrawable, );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.expanded_controller, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void showProgressBar() {

    }

    @Override
    protected void hideProgressBar() {

    }
}
