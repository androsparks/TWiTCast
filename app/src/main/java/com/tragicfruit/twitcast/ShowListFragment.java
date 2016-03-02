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

    private boolean mCoverArtDownloaded;
    private boolean mEpisodesUpdated;

    public static ShowListFragment newInstance() {
        return new ShowListFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);

        mDatabase = TWiTDatabase.get();

        if (mDatabase.getShows() == null) {
            updateShows();
        } else {
            updateEpisodes();
        }
    }

    private void updateShows() {
        new FetchShowsTask().execute(); // TODO: handle no internet connection
    }

    private void updateEpisodes() {
        new FetchEpisodesTask().execute(); // TODO: handle no internet connection
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
    public void onResume() {
        super.onResume();
        if (mLoadingDialog != null && mDatabase.getShows() != null) {
            if (mEpisodesUpdated) {
                mLoadingDialog.dismiss();
            } else if (mCoverArtDownloaded) {
                mLoadingDialog.setDialogMessage(getString(R.string.updating_episodes_text));
            } else {
                mLoadingDialog.setDialogMessage(getString(R.string.downloading_cover_art_placeholder_text));
            }
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
            // show loading dialog
            mLoadingDialog = UpdatingShowsFragment.newInstance();
            FragmentManager fm = getFragmentManager();
            mLoadingDialog.show(fm, DIALOG_UPDATING_SHOWS);
        }

        @Override
        protected List<Show> doInBackground(Void... params) {
            return new TWiTFetcher().fetchShows();
        }

        @Override
        protected void onPostExecute(List<Show> shows) {
            mDatabase.setShows(shows);
            setupAdapter();

            if (mDatabase.getShows() != null) {
                new FetchCoverArtTask().execute();
            }
        }
    }

    private class FetchCoverArtTask extends AsyncTask<Void, Integer, Void> {
        @Override
        protected void onPreExecute() {
            if (mLoadingDialog != null && mLoadingDialog.isAdded()) {
                mLoadingDialog.setDialogMessage(getString(R.string.downloading_cover_art_placeholder_text));
                mLoadingDialog.setMaxProgress(mDatabase.getShows().size());
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            for (int i = 0; i < mDatabase.getShows().size(); i++) {
                Show show = mDatabase.getShows().get(i);
                try {
                    byte[] bitmapBytes = new TWiTFetcher().getUrlBytes(show.getCoverArtUrl());
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
            mCoverArtDownloaded = true;

            if (mDatabase.getShows() != null) {
                updateEpisodes();
            }
        }
    }

    private class FetchEpisodesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            if (mLoadingDialog != null && mLoadingDialog.isAdded()) {
                mLoadingDialog.setDialogMessage(getString(R.string.updating_episodes_text));
            }
        }
        @Override
        protected Void doInBackground(Void... params) {
            new TWiTFetcher().fetchEpisodes(null);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mEpisodesUpdated = true;

            // dismiss loading dialog
            try {
                mLoadingDialog.dismiss();
            } catch (Exception e) {
                Log.e(TAG, "Cannot dismiss dialog", e);
            }
        }
    }
}
