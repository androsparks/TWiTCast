package com.tragicfruit.twitcast;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.SessionManager;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.images.WebImage;
import com.tragicfruit.twitcast.castv3.PlayerActivity;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.database.TWiTLab;
import com.tragicfruit.twitcast.dialogs.LeaveFeedbackFragment;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.episode.EpisodeListFragment;
import com.tragicfruit.twitcast.episode.LatestFragment;
import com.tragicfruit.twitcast.stream.LiveFragment;
import com.tragicfruit.twitcast.stream.Stream;
import com.tragicfruit.twitcast.stream.StreamSource;
import com.tragicfruit.twitcast.utils.QueryPreferences;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by Jeremy on 4/03/2016.
 */
public abstract class GoogleCastActivity extends AppCompatActivity
        implements LatestFragment.Callbacks, LiveFragment.Callbacks, EpisodeListFragment.Callbacks {
    private static final String TAG = "GoogleCastActivity";
    private static final String DIALOG_FEEDBACK = "feedback";

    private CastContext mCastContext;
    private SessionManager mCastSessionManager;
    private CastSessionManagerListener mCastSessionManagerListener;
    private RemoteMediaClient mRemoteMediaClient;
    private CastMediaClientListener mRemoteMediaClientListener;

    private MenuItem mMediaRouteMenuItem;
    private MediaInfo mSelectedMediaInfo;
    private Episode mEpisodeToPlay;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCastContext = CastContext.getSharedInstance(this);
    }

    private class CastSessionManagerListener implements SessionManagerListener<CastSession> {

        @Override
        public void onSessionStarting(CastSession castSession) {

        }

        @Override
        public void onSessionStarted(CastSession castSession, String s) {
            Log.d(TAG, "onSessionStarted");

//            if (mSelectedMediaInfo != null) {
//                startPlayingSelectedMedia();
//            }

            // onDeviceSelected =========================

            if (castSession.getCastDevice() != null) {
                boolean audioOnly = !castSession.getCastDevice().hasCapability(CastDevice.CAPABILITY_VIDEO_OUT);
                QueryPreferences.setCastDeviceAudioOnly(GoogleCastActivity.this, audioOnly);
            }

//            if (mSelectedMediaInfo != null) {
//                showProgressBar();
//            }

            mRemoteMediaClient = castSession.getRemoteMediaClient();
            mRemoteMediaClientListener = new CastMediaClientListener();
            mRemoteMediaClient.addListener(mRemoteMediaClientListener);
        }

        @Override
        public void onSessionStartFailed(CastSession castSession, int i) {

        }

        @Override
        public void onSessionEnding(CastSession castSession) {

        }

        @Override
        public void onSessionEnded(CastSession castSession, int i) {
            if (mRemoteMediaClient != null) {
                mRemoteMediaClient.removeListener(mRemoteMediaClientListener);
            }
        }

        @Override
        public void onSessionResuming(CastSession castSession, String s) {

        }

        @Override
        public void onSessionResumed(CastSession castSession, boolean b) {
            Log.d(TAG, "onSessionResumed");

            mRemoteMediaClient = castSession.getRemoteMediaClient();
            mRemoteMediaClientListener = new CastMediaClientListener();
            mRemoteMediaClient.addListener(mRemoteMediaClientListener);
        }

        @Override
        public void onSessionResumeFailed(CastSession castSession, int i) {

        }

        @Override
        public void onSessionSuspended(CastSession castSession, int i) {
            if (mRemoteMediaClient != null) {
                mRemoteMediaClient.removeListener(mRemoteMediaClientListener);
            }
        }
    }

    private class CastMediaClientListener implements RemoteMediaClient.Listener {

        @Override
        public void onMetadataUpdated() {
            Log.e(TAG, "onMetadataUpdated()");
        }

        @Override
        public void onStatusUpdated() {
            Log.e(TAG, "onStatusUpdated()");

            if (mSelectedMediaInfo != null) {
                startPlayingSelectedMedia();
//                showProgressBar();
            }

            mRemoteMediaClient.removeListener(this);
        }

        @Override
        public void onSendingRemoteMediaRequest() {
        }

        @Override
        public void onQueueStatusUpdated() {
        }

        @Override
        public void onPreloadStatusUpdated() {
        }
    }

    public boolean checkGooglePlayServices() {
        final int googlePlayServicesCheck = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        switch (googlePlayServicesCheck) {
            case ConnectionResult.SUCCESS:
                return true;
            default:
                Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, googlePlayServicesCheck, 0);
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                });
                dialog.show();
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_google_cast, menu);
        mMediaRouteMenuItem = CastButtonFactory
                .setUpMediaRouteButton(getApplicationContext(),menu, R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.leave_feedback_menu_item:
                FragmentManager fm = getSupportFragmentManager();
                LeaveFeedbackFragment dialog = LeaveFeedbackFragment.newInstance();
                dialog.show(fm, DIALOG_FEEDBACK);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGooglePlayServices();

        mCastSessionManager =
                CastContext.getSharedInstance(this).getSessionManager();
        mCastSessionManagerListener = new CastSessionManagerListener();
        mCastSessionManager.addSessionManagerListener(mCastSessionManagerListener,
                CastSession.class);
    }

    @Override
    protected void onPause() {
        super.onPause();

        mCastSessionManager.removeSessionManagerListener(mCastSessionManagerListener,
                CastSession.class);
    }

    private boolean isConnected() {
        CastSession castSession = CastContext.getSharedInstance(this)
                .getSessionManager()
                .getCurrentCastSession();
        return (castSession != null && castSession.isConnected());
    }

    @Override
    public void refreshVideo() {
        try {
            if (isConnected() && mRemoteMediaClient.hasMediaSession()) {
                int position = (int) mRemoteMediaClient.getApproximateStreamPosition();
                playVideo(mEpisodeToPlay, position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing video", e);
        }
    }

    @Override
    public void refreshLiveStream() {
        try {
            if (isConnected() && mRemoteMediaClient.isLiveStream()) {
                playLiveStream();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing video", e);
        }
    }

    @Override
    public void playVideo(Episode episode, int position) {
        mEpisodeToPlay = episode;
        mPosition = position;

        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, mEpisodeToPlay.getShow().getTitle());
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, mEpisodeToPlay.getShortTitle());
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, getString(R.string.studio_name));
        mediaMetadata.addImage(new WebImage(Uri.parse(mEpisodeToPlay.getShow().getCoverArtUrl())));
        mediaMetadata.addImage(new WebImage(Uri.parse(mEpisodeToPlay.getShow().getCoverArtUrl())));

        String url = getMediaUrl(mEpisodeToPlay);
        String contentType = getContentType(url);

        if (url == null || contentType == null) {
            Toast.makeText(this,
                    R.string.error_playing_episode_toast,
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mSelectedMediaInfo = new MediaInfo.Builder(url)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setMetadata(mediaMetadata)
                .build();

        if (isConnected()) {
            startPlayingSelectedMedia();
        } else {
            // cast device detected but not connected
            if (mMediaRouteMenuItem.isVisible()) {
                showMediaRouteDialog(mMediaRouteMenuItem);
            } else { // no cast device detected
                Toast.makeText(this,
                        R.string.no_chromecast_toast,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }


    @Override
    public void playLiveStream() {
        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        mediaMetadata.putString(MediaMetadata.KEY_TITLE, getString(R.string.twit_live_stream_title));
        mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, getString(R.string.twit_live_stream_title));
        mediaMetadata.putString(MediaMetadata.KEY_STUDIO, getString(R.string.studio_name));
        mediaMetadata.addImage(new WebImage(Uri.parse(Constants.LOGO_URL)));
        mediaMetadata.addImage(new WebImage(Uri.parse(Constants.LOGO_URL)));

        Stream stream = getStream(QueryPreferences.getStreamSource(this));
        String url = null, contentType = null;
        if (stream != null) {
            url = stream.getSource();
            contentType = stream.getType();
        }

        if (stream == null || url == null || contentType == null) {
            Toast.makeText(this,
                    R.string.error_playing_stream_toast,
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        mSelectedMediaInfo = new MediaInfo.Builder(url)
                .setContentType(contentType)
                .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                .setMetadata(mediaMetadata)
                .build();

        if (isConnected()) {
            startPlayingSelectedMedia();
        } else {
            // cast device detected but not connected
            if (mMediaRouteMenuItem.isVisible()) {
                showMediaRouteDialog(mMediaRouteMenuItem);
            } else { // no cast device detected
                Toast.makeText(this,
                        R.string.no_chromecast_toast,
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    private Stream getStream(StreamSource source) {
        ConcurrentMap<String, Stream> streamMap = TWiTLab.get(this).getStreams();

        switch (source) {
            case FLOSOFT_HIGH:
                return streamMap.get(getString(R.string.flosoft_high_stream));
            case FLOSOFT_LOW:
                return streamMap.get(getString(R.string.flosoft_low_stream));
            case FLOSOFT_ABR:
                return streamMap.get(getString(R.string.flosoft_abr_stream));
            case TWITCH_HD:
                return streamMap.get(getString(R.string.twitch_hd_stream));
            case TWITCH_HIGH:
                return streamMap.get(getString(R.string.twitch_high_stream));
            case TWITCH_LOW:
                return streamMap.get(getString(R.string.twitch_low_stream));
            case USTREAM:
                return streamMap.get(getString(R.string.ustream_stream));
            case AUDIO:
                return streamMap.get(getString(R.string.audio_stream));
        }

        return null;
    }

    private void startPlayingSelectedMedia() {
        if (QueryPreferences.isCastDeviceAudioOnly(this)) {
            mSelectedMediaInfo = getAudioMediaInfo();
        }

        if (mSelectedMediaInfo != null)
            Log.d(TAG, "Playing from url: " + mSelectedMediaInfo.getContentId());

//        showProgressBar();

        try {
            // if nothing playing
            if (!(mRemoteMediaClient.hasMediaSession() || mRemoteMediaClient.isLiveStream())) {
                Intent intent = new Intent(this, PlayerActivity.class);
                startActivity(intent);
            }
            mRemoteMediaClient.load(mSelectedMediaInfo, true, mPosition);
            hideProgressBar();
            mSelectedMediaInfo = null;
            mPosition = 0;
        } catch (Exception e) {
            e.printStackTrace();
            // Cast device not ready - will play automatically once connected
        }
    }

    private MediaInfo getAudioMediaInfo() {
        if (mSelectedMediaInfo.getStreamType() != MediaInfo.STREAM_TYPE_LIVE) {
            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, mEpisodeToPlay.getShow().getTitle());
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, mEpisodeToPlay.getShortTitle());
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, getString(R.string.studio_name));
            mediaMetadata.putString(MediaMetadata.KEY_ARTIST, getString(R.string.studio_name));
            mediaMetadata.addImage(new WebImage(Uri.parse(mEpisodeToPlay.getShow().getCoverArtUrl())));
            mediaMetadata.addImage(new WebImage(Uri.parse(mEpisodeToPlay.getShow().getCoverArtUrl())));

            String url = mEpisodeToPlay.getAudioUrl();

            if (url == null) {
                Toast.makeText(this,
                        R.string.error_playing_episode_toast,
                        Toast.LENGTH_SHORT)
                        .show();
                return null;
            }

            return new MediaInfo.Builder(url)
                    .setContentType(Constants.AUDIO_CONTENT_TYPE)
                    .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                    .setMetadata(mediaMetadata)
                    .build();
        } else {
            MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MUSIC_TRACK);
            mediaMetadata.putString(MediaMetadata.KEY_TITLE, getString(R.string.studio_name));
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_TITLE, getString(R.string.twit_live_stream_title));
            mediaMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, getString(R.string.studio_name));
            mediaMetadata.putString(MediaMetadata.KEY_ARTIST, getString(R.string.studio_name));
            mediaMetadata.addImage(new WebImage(Uri.parse(Constants.LOGO_URL)));
            mediaMetadata.addImage(new WebImage(Uri.parse(Constants.LOGO_URL)));

            Stream stream = getStream(StreamSource.AUDIO);
            String url = null, contentType = null;
            if (stream != null) {
                url = stream.getSource();
                contentType = stream.getType();
            }

            if (url == null) {
                Toast.makeText(this,
                        R.string.error_playing_episode_toast,
                        Toast.LENGTH_SHORT)
                        .show();
                return null;
            }

            return new MediaInfo.Builder(url)
                    .setContentType(contentType)
                    .setStreamType(MediaInfo.STREAM_TYPE_LIVE)
                    .setMetadata(mediaMetadata)
                    .build();
        }
    }

    private void showMediaRouteDialog(MenuItem menuItem) {
        MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)
                MenuItemCompat.getActionProvider(menuItem);
        mediaRouteActionProvider.onPerformDefaultAction();
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

    protected abstract void showProgressBar();
    protected abstract void hideProgressBar();

    @Override
    public void showNoConnectionSnackbar() {}

    @Override
    public void setToolbarColour(int toolbarColour, int statusBarColour) {}
}
