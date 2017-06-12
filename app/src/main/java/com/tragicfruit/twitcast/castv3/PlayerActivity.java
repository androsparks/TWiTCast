package com.tragicfruit.twitcast.castv3;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.media.ImagePicker;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.cast.framework.media.uicontroller.UIMediaController;
import com.tragicfruit.twitcast.GoogleCastActivity;
import com.tragicfruit.twitcast.R;

/**
 * Created by Jeremy on 10/06/2017.
 */

public class PlayerActivity extends GoogleCastActivity {

    private ImageView mPageView;
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

    private UIMediaController mUIMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        mPauseDrawable = ContextCompat.getDrawable(this, R.drawable.ic_pause_circle_white_80dp);
        mPlayDrawable = ContextCompat.getDrawable(this, R.drawable.ic_play_circle_white_80dp);
        mStopDrawable = ContextCompat.getDrawable(this, R.drawable.ic_stop_circle_white_80dp);

        mPageView = (ImageView) findViewById(R.id.pageview);
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
        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }

        mUIMediaController = new UIMediaController(this);
        mUIMediaController.bindImageViewToPlayPauseToggle(mPlayPause, mPlayDrawable, mPauseDrawable, mStopDrawable, mLoading, false);
        mUIMediaController.bindSeekBar(mSeekbar);
        mUIMediaController.bindViewToRewind(mRewind, 30 * 1000);
        mUIMediaController.bindViewToForward(mForward, 30 * 1000);
        mUIMediaController.bindTextViewToStreamPosition(mStart, true);
        mUIMediaController.bindTextViewToStreamDuration(mEnd);
        mUIMediaController.bindTextViewToMetadataOfCurrentItem(mLine1, MediaMetadata.KEY_TITLE);
        mUIMediaController.bindTextViewToMetadataOfCurrentItem(mLine2, MediaMetadata.KEY_SUBTITLE);
        mUIMediaController.bindImageViewToImageOfCurrentItem(mPageView, ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND, null);
        mUIMediaController.setPostRemoteMediaClientListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                updateUIForLiveStream();
            }

            @Override
            public void onMetadataUpdated() {

            }

            @Override
            public void onQueueStatusUpdated() {

            }

            @Override
            public void onPreloadStatusUpdated() {

            }

            @Override
            public void onSendingRemoteMediaRequest() {

            }

            @Override
            public void onAdBreakStatusUpdated() {

            }
        });
    }

    private void updateUIForLiveStream() {
        if (mUIMediaController.getRemoteMediaClient() != null
                && mUIMediaController.getRemoteMediaClient().isLiveStream()) {
            mRewind.setVisibility(View.GONE);
            mForward.setVisibility(View.GONE);
            mSeekbar.setVisibility(View.GONE);
            mStart.setVisibility(View.GONE);
            mEnd.setVisibility(View.GONE);
        } else {
            mRewind.setVisibility(View.VISIBLE);
            mForward.setVisibility(View.VISIBLE);
            mSeekbar.setVisibility(View.VISIBLE);
            mStart.setVisibility(View.VISIBLE);
            mEnd.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.expanded_controller, menu);
        CastButtonFactory.setUpMediaRouteButton(this, menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUIForLiveStream();
    }

    @Override
    protected void showProgressBar() {

    }

    @Override
    protected void hideProgressBar() {

    }

    @Override
    public void onSessionEnded(CastSession castSession, int i) {
        super.onSessionEnded(castSession, i);
        finish();
    }

    @Override
    public void onSessionSuspended(CastSession castSession, int i) {
        super.onSessionSuspended(castSession, i);
        finish();
    }
}

