package com.tragicfruit.twitcast;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.player.VideoCastControllerActivity;
import com.google.android.libraries.cast.companionlibrary.utils.Utils;

/**
 * Created by Jeremy on 4/03/2016.
 */
public abstract class GoogleCastActivity extends SingleFragmentActivity implements EpisodeListFragment.Callbacks {
    private static final String TAG = "GoogleCastActivity";

    private VideoCastManager mCastManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseCastManager.checkGooglePlayServices(this);

        CastConfiguration options = new CastConfiguration.Builder(SecretConstants.GOOGLE_CAST_APP_ID)
                .enableAutoReconnect()
                .enableDebug()
                .enableLockScreen()
                .enableWifiReconnection()
                .enableNotification()
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_SKIP_PREVIOUS, false)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_SKIP_NEXT, false)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_REWIND, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true)
                .build();

        VideoCastManager.initialize(this, options);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCastManager.decrementUiCounter();
    }

    @Override
    public void playVideo(Episode episode) {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, episode.getShow().getTitle());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, episode.getDisplayTitle());
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, getString(R.string.studio_name));
        mediaMetadata.addImage(new WebImage(Uri.parse(episode.getShow().getCoverArtSmallUrl())));
        mediaMetadata.addImage(new WebImage(Uri.parse(episode.getShow().getCoverArtLargeUrl())));

        // TODO: change for different stream qualities
        MediaInfo mediaInfo = new MediaInfo.Builder(episode.getVideoHdUrl())
                .setContentType("video/mp4")
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        if (mCastManager.isConnected()) {
            try {
                mCastManager.loadMedia(mediaInfo, true, 0);
                mCastManager.startVideoCastControllerActivity(this, mediaInfo, 0, true);
            } catch (Exception e) {
                Log.e(TAG, "Cannot load video", e);
            }
        } else {
            // TODO: prompt user to select cast device
        }
    }
}
