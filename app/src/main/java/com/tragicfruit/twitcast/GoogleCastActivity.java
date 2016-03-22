package com.tragicfruit.twitcast;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.ApplicationMetadata;
import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.common.images.WebImage;
import com.google.android.libraries.cast.companionlibrary.cast.BaseCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumer;
import com.google.android.libraries.cast.companionlibrary.cast.callbacks.VideoCastConsumerImpl;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.database.TWiTLab;
import com.tragicfruit.twitcast.dialogs.LeaveFeedbackFragment;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.episode.EpisodeListFragment;
import com.tragicfruit.twitcast.episode.LatestEpisodesFragment;
import com.tragicfruit.twitcast.stream.LiveStreamFragment;
import com.tragicfruit.twitcast.stream.Stream;
import com.tragicfruit.twitcast.stream.StreamSource;
import com.tragicfruit.twitcast.utils.QueryPreferences;

import java.util.concurrent.ConcurrentMap;

/**
 * Created by Jeremy on 4/03/2016.
 */
public abstract class GoogleCastActivity extends AppCompatActivity
        implements LatestEpisodesFragment.Callbacks, LiveStreamFragment.Callbacks, EpisodeListFragment.Callbacks {
    private static final String TAG = "GoogleCastActivity";
    private static final String DIALOG_FEEDBACK = "feedback";

    private VideoCastManager mCastManager;
    private VideoCastConsumer mCastConsumer;
    private MenuItem mMediaRouteMenuItem;
    private MediaInfo mSelectedMediaInfo;
    private Episode mEpisodeToPlay;
    private int mPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BaseCastManager.checkGooglePlayServices(this);

        mCastConsumer = new VideoCastConsumerImpl() {
            @Override
            public void onApplicationConnected(ApplicationMetadata appMetadata, String sessionId, boolean wasLaunched) {
                if (mSelectedMediaInfo != null) {
                    startPlayingSelectedMedia();
                }
            }

            @Override
            public void onDeviceSelected(CastDevice device, MediaRouter.RouteInfo routeInfo) {
                if (device != null) {
                    boolean audioOnly = !device.hasCapability(CastDevice.CAPABILITY_VIDEO_OUT);
                    QueryPreferences.setCastDeviceAudioOnly(GoogleCastActivity.this, audioOnly);
                }

                if (mSelectedMediaInfo != null) {
                    showProgressBar();
                }
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_google_cast, menu);
        mMediaRouteMenuItem = mCastManager.addMediaRouterButton(menu, R.id.media_route_menu_item);
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
    public void refreshVideo() {
        try {
            if (mCastManager.isConnected() && mCastManager.isRemoteMediaLoaded()) {
                int position = (int) mCastManager.getCurrentMediaPosition();
                playVideo(mEpisodeToPlay, position);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error refreshing video", e);
        }
    }

    @Override
    public void refreshLiveStream() {
        try {
            if (mCastManager.isConnected() && mCastManager.isRemoteStreamLive()) {
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

        if (mCastManager.isConnected()) {
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

        if (mCastManager.isConnected()) {
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
            case BIT_GRAVITY_HIGH:
                return streamMap.get(getString(R.string.bitgravity_high_stream));
            case BIT_GRAVITY_LOW:
                return streamMap.get(getString(R.string.bitgravity_low_stream));
            case FLOSOFT:
                return streamMap.get(getString(R.string.flosoft_stream));
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

        showProgressBar();

        try {
            if (!mCastManager.isRemoteMediaLoaded()) { // beginning episode
                mCastManager.startVideoCastControllerActivity(this, mSelectedMediaInfo, mPosition, true);
            }
            mCastManager.loadMedia(mSelectedMediaInfo, true, mPosition);
            hideProgressBar();
            mSelectedMediaInfo = null;
            mPosition = 0;
        } catch (Exception e) {
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
