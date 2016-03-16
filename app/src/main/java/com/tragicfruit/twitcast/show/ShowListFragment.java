package com.tragicfruit.twitcast.show;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.constants.Constants;
import com.tragicfruit.twitcast.database.TWiTLab;
import com.tragicfruit.twitcast.dialogs.ChooseQualityFragment;
import com.tragicfruit.twitcast.dialogs.UpdatingShowsFragment;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.episode.EpisodeListActivity;
import com.tragicfruit.twitcast.episode.StreamQuality;
import com.tragicfruit.twitcast.utils.QueryPreferences;
import com.tragicfruit.twitcast.utils.TWiTFetcher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Jeremy on 23/02/2016.
 */
public class ShowListFragment extends Fragment {
    private static final String TAG = "ShowListFragment";
    private static final String DIALOG_UPDATING_SHOWS = "updating_shows";
    private static final String DIALOG_CHOOSE_QUALITY = "choose_quality";
    private static final int REQUEST_QUALITY = 0;
    private static final int REQUEST_SHOW_EPISODES = 1;

    private AutofitRecyclerView mRecyclerView;
    private TWiTLab mDatabase;
    private FetchShowsTask mFetchShowsTask;
    private FetchCoverArtTask mFetchCoverArtTask;
    private FetchEpisodesTask mFetchEpisodesTask;

    private boolean mRefreshingShows;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void refreshLatestEpisodes();
        void showNoConnectionSnackbar();
    }

    public static ShowListFragment newInstance() {
        return new ShowListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mDatabase = TWiTLab.get(getActivity());

        if (mDatabase.getShows().size() == 0) {
            updateShows();
        } else if (!isCoverArtDownloaded()) {
            updateCoverArt();
        } else {
            updateEpisodes();
        }
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    private void updateShows() {
        if (isNetworkAvailableAndConnected()) {
            mRefreshingShows = true;
            getActivity().invalidateOptionsMenu();

            mDatabase.resetEpisodes();
            mFetchShowsTask = new FetchShowsTask();
            mFetchShowsTask.execute();
        } else {
            mRefreshingShows = false;
            getActivity().invalidateOptionsMenu();

            dismissLoadingDialog();
            mCallbacks.showNoConnectionSnackbar();
        }
    }

    private void updateCoverArt() {
        if (isNetworkAvailableAndConnected()) {
            mRefreshingShows = true;
            getActivity().invalidateOptionsMenu();

            mFetchCoverArtTask = new FetchCoverArtTask();
            mFetchCoverArtTask.execute();
        } else {
            mRefreshingShows = false;
            getActivity().invalidateOptionsMenu();

            dismissLoadingDialog();
            mCallbacks.showNoConnectionSnackbar();
        }
    }

    private void updateEpisodes() {
        if (isNetworkAvailableAndConnected()) {
            mFetchEpisodesTask = new FetchEpisodesTask();
            mFetchEpisodesTask.execute();
        } else {
            dismissLoadingDialog();
            mCallbacks.showNoConnectionSnackbar();
        }
    }

    private boolean isCoverArtDownloaded() {
        for (Show show : mDatabase.getShows()) {
            if (show.getCoverArt() == null) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_show_list, container, false);

        mRecyclerView = (AutofitRecyclerView) v.findViewById(R.id.fragment_show_list_recycler_view);
        setupAdapter();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_show_list, menu);
        MenuItem refreshButton = menu.findItem(R.id.refresh_button);
        refreshButton.setEnabled(!mRefreshingShows);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:
                updateShows();
                return true;
            case R.id.choose_quality:
                FragmentManager fm = getFragmentManager();
                ChooseQualityFragment dialog = ChooseQualityFragment.newInstance();
                dialog.setTargetFragment(ShowListFragment.this, REQUEST_QUALITY);
                dialog.show(fm, DIALOG_CHOOSE_QUALITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupAdapter() {
        if (isAdded() && !mRefreshingShows) {
            if (mDatabase.getShows().size() > 0) {
                mRecyclerView.setAdapter(new ShowAdapter(mDatabase.getShows()));
            } else {
                mRecyclerView.setAdapter(new ShowAdapter(new ArrayList<Show>()));
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_QUALITY) {
            StreamQuality quality = ChooseQualityFragment.getStreamQuality(data);
            QueryPreferences.setStreamQuality(getActivity(), quality);
        } else if (requestCode == REQUEST_SHOW_EPISODES) {
            mCallbacks.refreshLatestEpisodes();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mRefreshingShows) {
            showLoadingDialog();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mFetchShowsTask != null) {
            mFetchShowsTask.cancel(false);
        }

        if (mFetchCoverArtTask != null) {
            mFetchCoverArtTask.cancel(false);
        }

        if (mFetchEpisodesTask != null) {
            mFetchEpisodesTask.cancel(false);
        }
    }

    private void showLoadingDialog() {
        try {
            FragmentManager fm = getFragmentManager();
            Fragment fragment = fm.findFragmentByTag(DIALOG_UPDATING_SHOWS);

            if (fragment == null) {
                UpdatingShowsFragment dialog = UpdatingShowsFragment.newInstance();
                dialog.show(fm, DIALOG_UPDATING_SHOWS);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error showing loading dialog");
        }
    }

    private void dismissLoadingDialog() {
        try {
            FragmentManager fm = getFragmentManager();
            DialogFragment dialog = (DialogFragment) fm.findFragmentByTag(DIALOG_UPDATING_SHOWS);
            dialog.dismiss();
        } catch (Exception e) {
            Log.e(TAG, "Error dismissing loading dialog");
        }
    }

    private class ShowHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Show mShow;
        private ImageView mImageView;

        public ShowHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.fragment_selection_image_view);
            mImageView.setOnClickListener(this);

            // stretch image if not wide enough
            int size = mRecyclerView.getStretchedSize();
            mImageView.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        }

        public void bindShow(Show show) {
            mShow = show;
            mImageView.setImageDrawable(show.getCoverArt());
        }

        @Override
        public void onClick(View v) {
            Intent i = EpisodeListActivity.newIntent(getActivity(), mShow.getId());

            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), v, "cover_art");
            startActivityForResult(i, REQUEST_SHOW_EPISODES, options.toBundle());
        }
    }

    private class ShowAdapter extends RecyclerView.Adapter<ShowHolder> {
        private List<Show> mShowList;

        public ShowAdapter(List<Show> showList) {
            mShowList = showList;
        }

        @Override
        public ShowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.cover_art_item, parent, false);
            return new ShowHolder(view);
        }

        @Override
        public void onBindViewHolder(ShowHolder holder, int position) {
            Show show = mShowList.get(position);
            holder.bindShow(show);
        }

        @Override
        public int getItemCount() {
            return mShowList.size();
        }
    }

    private class FetchShowsTask extends AsyncTask<Void, Void, List<Show>> {
        @Override
        protected void onPreExecute() {
            showLoadingDialog();
        }

        @Override
        protected List<Show> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchShows();
        }

        @Override
        protected void onPostExecute(List<Show> showList) {
            if (isCancelled()) {
                return;
            }

            if (showList != null) {
                Log.d(TAG, "Fetched shows");
                mDatabase.setShows(showList);
            }

            updateCoverArt();
        }
    }

    private class FetchCoverArtTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            // clean up old cover art
            cleanUp(Constants.COVER_ART_FOLDER);
            cleanUp(Constants.COVER_ART_LARGE_FOLDER);
            cleanUp(Constants.LOGO_FOLDER);

            // download new cover art
            for (Show show : mDatabase.getShows()) {
                try {
                    File coverArtFile = new TWiTFetcher(getActivity()).getCoverArt(show);

                    if (isCancelled()) {
                        break;
                    }

                    show.setCoverArt(Drawable.createFromPath(coverArtFile.getAbsolutePath()));
                    show.setCoverArtLocalPath(coverArtFile.getAbsolutePath());
                } catch (IOException e) {
                    Log.e(TAG, "Cannot download cover art for " + show.getTitle(), e);
                }
            }

            // pull logo from assets
            try {
                saveFromAssets(Constants.LOGO_FILE, Constants.LOGO_FOLDER);
                saveFromAssets(Constants.LOGO_LARGE_FILE, Constants.LOGO_FOLDER);
            } catch (IOException e) {
                Log.e(TAG, "Cannot fetch logos from assets", e);
            }
            return null;
        }

        private void saveFromAssets(String filename, String folderName) throws IOException {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                inputStream = getActivity().getResources().getAssets().open(filename);

                File folder = new File(getActivity().getFilesDir() + "/" + folderName);
                if (!folder.exists()) {
                    folder.mkdir();
                }

                File file = new File(getActivity().getFilesDir() + "/" + folderName, filename);

                // write the inputStream to a FileOutputStream
                outputStream =
                        new FileOutputStream(file);

                int read = 0;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) != -1) {
                    outputStream.write(bytes, 0, read);
                }

                Log.i(TAG, "File saved to: " + file.getAbsolutePath());

            } catch (IOException e) {
                Log.e(TAG, "Error fetching " + filename + " from assets");
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        }

        private void cleanUp(String folder) {
            File coverArtFolder = new File(getActivity().getFilesDir() + "/" + folder);
            if (coverArtFolder.exists()) {
                File[] files = coverArtFolder.listFiles();
                for (File file : files) {
                    file.delete();
                    Log.d(TAG, "Deleted " + file.getAbsolutePath());
                }
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isCancelled()) {
                return;
            }
            Log.d(TAG, "Fetched cover art");

            mRefreshingShows = false;
            getActivity().invalidateOptionsMenu();
            setupAdapter();
            updateEpisodes();
        }
    }

    private class FetchEpisodesTask extends AsyncTask<Void, Void, List<Episode>> {

        @Override
        protected List<Episode> doInBackground(Void... params) {
            try {
                return new TWiTFetcher(getActivity()).fetchAllEpisodes();
            } catch (IOException e) {
                Log.e(TAG, "Error fetching episodes", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Episode> episodeList) {
            if (isCancelled()) {
                return;
            }

            if (episodeList == null) {
                dismissLoadingDialog();
                mCallbacks.showNoConnectionSnackbar();
                return;
            }

            // reset episodes if local episodes obsolete
            if (mDatabase.getEpisodes().size() > 0) {
                Episode newestLocal = mDatabase.getEpisodes().get(0);
                Episode oldestServer = episodeList.get(episodeList.size() - 1);

                Date newestLocalDate = newestLocal.getPublicationDate();
                Date oldestServerDate = oldestServer.getPublicationDate();

                if (oldestServerDate.after(newestLocalDate)) {
                    mDatabase.resetEpisodes();
                    Log.d(TAG, "Cleaning up obsolete episodes");
                }
            }

            boolean newShows = mDatabase.addEpisodes(episodeList);
            dismissLoadingDialog();

            if (newShows) {
                mCallbacks.refreshLatestEpisodes();

                TWiTLab.get(getActivity()).saveShows();
                TWiTLab.get(getActivity()).saveEpisodes();
            }
        }
    }
}
