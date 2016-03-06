package com.tragicfruit.twitcast.misc;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.dialog.video.VideoMediaRouteDialogFactory;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.constants.SecretConstants;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.episode.EpisodeListFragment;
import com.tragicfruit.twitcast.utils.QueryPreferences;

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
                .setMediaRouteDialogFactory(new VideoMediaRouteDialogFactory())
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

        String url = getMediaUrl(episode);
        String contentType = getContentType(url);

        if (url == null || contentType == null) {
            Toast.makeText(this, R.string.error_playing_video_toast, Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "Playing from url: " + url);
        MediaInfo mediaInfo = new MediaInfo.Builder(url)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        if (mCastManager.isConnected()) {
            try {
                mCastManager.loadMedia(mediaInfo, true, 0);
                mCastManager.startVideoCastControllerActivity(this, mediaInfo, 0, true);
            } catch (Exception e) {
                Log.e(TAG, "Cannot load video", e);
                Toast.makeText(this, R.string.cast_not_ready_toast, Toast.LENGTH_SHORT).show();
            }
        } else {
            // TODO: prompt user to select cast device
            Toast.makeText(this, "No cast device detected.", Toast.LENGTH_SHORT).show();
        }
    }

    private String getMediaUrl(Episode episode) {
        String url = null;
        switch (QueryPreferences.getStreamQuality(this)) {
            case VIDEO_HD:
                url = episode.getVideoHdUrl();
                if (url != null) {
                    break;
                }
            case VIDEO_LARGE:
                url = episode.getVideoLargeUrl();
                if (url != null) {
                    break;
                }
            case VIDEO_SMALL:
                url = episode.getVideoSmallUrl();
                if (url != null) {
                    break;
                }
            case AUDIO:
                url = episode.getAudioUrl();
        }
        return url;
    }

    private String getContentType(String url) {
        if (url.endsWith(".mp4")) {
            return Constants.VIDEO_CONTENT_TYPE;
        } else if (url.endsWith(".mp3")) {
            return Constants.AUDIO_CONTENT_TYPE;
        } else {
            return null;
        }
    }
}
