package com.tragicfruit.twitcast;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 23/02/2016.
 */
public class SelectionFragment extends Fragment {
    private static final String TAG = "SelectionFragment";
    private static final String DIALOG_UPDATING_SHOWS = "updating_shows";

    private AutofitRecyclerView mRecyclerView;
    private List<Show> mShows;
    private CoverArtDownloader<Show> mCoverArtDownloader;

    public static SelectionFragment newInstance() {
        return new SelectionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        updateShows();

        Handler responseHandler = new Handler();
        mCoverArtDownloader = new CoverArtDownloader<>(responseHandler);
        mCoverArtDownloader.setCoverArtDownloadListener(new CoverArtDownloader.CoverArtDownloadListener<Show>() {
            @Override
            public void onCoverArtDownloaded(Show show, Bitmap coverArt) {
                if (isAdded()) {
                    show.setCoverArt(new BitmapDrawable(getResources(), coverArt));
                    mRecyclerView.getAdapter().notifyDataSetChanged();
                }
            }
        });
        mCoverArtDownloader.start();
        mCoverArtDownloader.getLooper();
        Log.i(TAG, "CoverArtDownloader thread started");
    }

    private UpdatingShowsFragment mLoadingDialog;

    private void updateShows() {
        mLoadingDialog = UpdatingShowsFragment.newInstance();
        FragmentManager fm = getFragmentManager();
        mLoadingDialog.show(fm, DIALOG_UPDATING_SHOWS);

        new FetchShowsTask().execute(); // TODO: handle no internet connection
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection, container, false);

        mRecyclerView = (AutofitRecyclerView) v.findViewById(R.id.fragment_selection_recycler_view);

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            mLoadingDialog.dismiss();
            if (mShows != null) {
                mRecyclerView.setAdapter(new ShowAdapter(mShows));
            } else {
                mRecyclerView.setAdapter(new ShowAdapter(new ArrayList<Show>()));
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCoverArtDownloader.quit();
        Log.i(TAG, "CoverArtDownloader thread destroyed");
    }

    private class ShowHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        public ShowHolder(View itemView) {
            super(itemView);

            mImageView = (ImageView) itemView.findViewById(R.id.fragment_selection_image_view);

            // stretch image if not wide enough
            int size = mRecyclerView.getStretchedSize();
            mImageView.setLayoutParams(new FrameLayout.LayoutParams(size, size));
        }

        public void bindDrawable(Drawable drawable) {
            mImageView.setImageDrawable(drawable);
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
        protected List<Show> doInBackground(Void... params) {
            return new TWiTFetcher().fetchShows();
        }

        @Override
        protected void onPostExecute(List<Show> shows) {
            mShows = shows;
            setupAdapter();

            if (shows == null) {
                return;
            }

            for (Show show: mShows) {
                mCoverArtDownloader.queueDownload(show, show.getCoverArtUrl());
            }
        }
    }
}
