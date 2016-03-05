package com.tragicfruit.twitcast;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
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
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 23/02/2016.
 */
public class ShowListFragment extends Fragment {
    private static final String TAG = "ShowListFragment";
    private static final String DIALOG_UPDATING_SHOWS = "updating_shows";

    private AutofitRecyclerView mRecyclerView;
    private UpdatingShowsFragment mLoadingDialog;
    private TWiTDatabase mDatabase;
    private FetchShowsTask mFetchShowsTask;
    private FetchCoverArtTask mFetchCoverArtTask;
    private FetchEpisodesTask mFetchEpisodesTask;

    public static ShowListFragment newInstance() {
        return new ShowListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mDatabase = TWiTDatabase.get();
        mFetchCoverArtTask = new FetchCoverArtTask();
        mFetchShowsTask = new FetchShowsTask();
        mFetchEpisodesTask = new FetchEpisodesTask();

        if (mDatabase.getShows() == null) {
            updateShows();
        } else if (!isCoverArtDownloaded()) {
            updateCoverArt();
        } else {
            updateEpisodes();
        }
    }

    private void updateShows() {
        mFetchShowsTask.execute();
        // TODO: handle no internet connection
    }

    private void updateCoverArt() {
        mFetchCoverArtTask.execute();
    }

    private void updateEpisodes() {
        // TODO: if oldest server episode is newer than newest local episode then wipe episodes & reset
        mFetchEpisodesTask.execute(); // TODO: handle no internet connection
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

        mRecyclerView = (AutofitRecyclerView) v.findViewById(R.id.fragment_selection_recycler_view);
        setupAdapter();

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_selection, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_button:
                updateShows();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setupAdapter() {
        if (isAdded()) {
            if (mDatabase.getShows() != null) {
                mRecyclerView.setAdapter(new ShowAdapter(mDatabase.getShows()));
            } else {
                mRecyclerView.setAdapter(new ShowAdapter(new ArrayList<Show>()));
            }
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

    private void showLoadingDialog(String message) {
        mLoadingDialog = UpdatingShowsFragment.newInstance(message);
        FragmentManager fm = getFragmentManager();
        mLoadingDialog.show(fm, DIALOG_UPDATING_SHOWS);
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

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
        }

        public void bindShow(Show show) {
            mShow = show;
        }

        @Override
        public void onClick(View v) {
            Intent i = EpisodeListActivity.newIntent(getActivity(), mShow.getId());

            ActivityOptionsCompat options = ActivityOptionsCompat
                    .makeSceneTransitionAnimation(getActivity(), v, "cover_art");
            startActivity(i, options.toBundle());
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

            if (show.getCoverArt() == null) {
                Drawable placeholder = getResources().getDrawable(R.drawable.cover_art_placeholder);
                holder.bindDrawable(placeholder);
            } else {
                holder.bindDrawable(show.getCoverArt());
            }
        }

        @Override
        public int getItemCount() {
            return mShowList.size();
        }
    }

    private class FetchShowsTask extends AsyncTask<Void, Void, List<Show>> {
        @Override
        protected void onPreExecute() {
            showLoadingDialog(getString(R.string.updating_shows_text));
        }

        @Override
        protected List<Show> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchShows();
        }

        @Override
        protected void onPostExecute(List<Show> showList) {
            if (showList != null && !isCancelled()) {
                Log.d(TAG, "Fetched shows");
                mDatabase.setShows(showList);
                setupAdapter();
                mFetchCoverArtTask.execute();
            } else {
                if (mLoadingDialog != null && mLoadingDialog.isAdded()) {
                    mLoadingDialog.dismiss();
                }
                Toast.makeText(getActivity(), "Cannot fetch episodes. Please try again later.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchCoverArtTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            if (mLoadingDialog == null || !mLoadingDialog.isAdded()) {
                showLoadingDialog(getString(R.string.downloading_cover_art_placeholder_text));
            } else {
                mLoadingDialog.setDialogMessage(getString(R.string.downloading_cover_art_placeholder_text));
            }
            mLoadingDialog.setMaxProgress(mDatabase.getShows().size());
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mDatabase.getShows().size(); i++) {
                Show show = mDatabase.getShows().get(i);
                try {
                    byte[] bitmapBytes = new TWiTFetcher(getActivity()).getUrlBytes(show.getCoverArtUrl());

                    if (isCancelled()) {
                        break;
                    }

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.length);
                    show.setCoverArt(new BitmapDrawable(getResources(), bitmap));
                } catch (IOException e) {
                    Log.e(TAG, "Cannot download cover art for " + show.getTitle(), e);
                }
                publishProgress(i + 1);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mRecyclerView.getAdapter().notifyDataSetChanged();

            if (mLoadingDialog != null && mLoadingDialog.isAdded()) {
                mLoadingDialog.setProgress(values[0]);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (isCancelled()) {
                return;
            }

            updateEpisodes();
            Log.d(TAG, "Fetched cover art");
        }
    }

    private class FetchEpisodesTask extends AsyncTask<Void, Void, List<Episode>> {
        @Override
        protected void onPreExecute() {
            if (mLoadingDialog == null || !mLoadingDialog.isAdded()) {
                showLoadingDialog(getString(R.string.updating_episodes_text));
            } else {
                mLoadingDialog.setDialogMessage(getString(R.string.updating_episodes_text));
            }
        }

        @Override
        protected List<Episode> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchAllEpisodes();
        }

        @Override
        protected void onPostExecute(List<Episode> episodeList) {
            if (isCancelled()) {
                return;
            }

            mDatabase.addEpisodes(episodeList);
//            mDatabase.setTimeLastUpdated(new Date().getTime());
            Log.d(TAG, "Episode count: " + mDatabase.getEpisodeCount());

            // dismiss loading dialog
            try {
                mLoadingDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Cannot dismiss dialog", e);
            }
        }
    }
}
