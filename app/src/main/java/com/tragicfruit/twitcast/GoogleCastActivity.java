package com.tragicfruit.twitcast;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;

/**
 * Created by Jeremy on 4/03/2016.
 */
public abstract class GoogleCastActivity extends SingleFragmentActivity implements EpisodeListFragment.Callbacks {
    private static final String TAG = "GoogleCastActivity";

    private VideoCastManager mCastManager;
    private VideoCastConsumerImpl mCastConsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseCastManager.checkGooglePlayServices(this);

        final CastConfiguration options = new CastConfiguration.Builder(SecretConstants.GOOGLE_CAST_APP_ID)
                .enableAutoReconnect()
                .enableCaptionManagement()
                .enableDebug()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .build();

        VideoCastManager.initialize(this, options);

        mCastConsumer = new VideoCastConsumerImpl();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_google_cast, menu);
        mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCastManager = VideoCastManager.getInstance();
        mCastManager.incrementUiCounter();
        mCastManager.addVideoCastConsumer(mCastConsumer);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastManager.decrementUiCounter();
        mCastManager.removeVideoCastConsumer(mCastConsumer);
    }

    @Override
    public void playVideo(String videoTitle, String videoUrl) {
        MediaMetadata mediaMetadata = new MediaMetadata( MediaMetadata.MEDIA_TYPE_MOVIE );
        mediaMetadata.putString( MediaMetadata.KEY_TITLE, videoTitle );

        MediaInfo mediaInfo = new MediaInfo.Builder( videoUrl )
                .setContentType( "video/mp4" )
                .setStreamType( MediaInfo.STREAM_TYPE_BUFFERED )
                .setMetadata( mediaMetadata )
                .build();

        if (mCastManager.isConnected()) {
            try {
                mCastManager.loadMedia(mediaInfo, true, 0);
            } catch (Exception e) {
                Log.e(TAG, "Cannot load video", e);
            }
        } else {
            // TODO: prompt user to select cast device
        }
    }
}
