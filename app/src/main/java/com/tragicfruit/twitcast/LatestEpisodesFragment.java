package com.tragicfruit.twitcast;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tragicfruit.twitcast.database.TWiTLab;
import com.tragicfruit.twitcast.dialogs.ChooseQualityFragment;
import com.tragicfruit.twitcast.episode.Episode;
import com.tragicfruit.twitcast.episode.StreamQuality;
import com.tragicfruit.twitcast.utils.QueryPreferences;
import com.tragicfruit.twitcast.utils.TWiTFetcher;

import java.util.List;

/**
 * Created by Jeremy on 6/03/2016.
 */
public class LatestEpisodesFragment extends Fragment {
    private static final String DIALOG_CHOOSE_QUALITY = "choose_quality";
    private static final int REQUEST_QUALITY = 0;

    private TWiTLab mTWiTLab;
    private List<Episode> mEpisodes;

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefresh;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void playVideo(Episode episode);
    }

    public static LatestEpisodesFragment newInstance() {
        return new LatestEpisodesFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mTWiTLab = TWiTLab.get(getActivity());
        mEpisodes = mTWiTLab.getEpisodes();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_latest_episodes, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_latest_episodes_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new EpisodeAdapter());

        mSwipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.fragment_latest_episodes_swipe_refresh);
        mSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchMoreEpisodesTask().execute();
            }
        });

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_latest_episodes, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_quality:
                FragmentManager fm = getFragmentManager();
                ChooseQualityFragment dialog = ChooseQualityFragment.newInstance();
                dialog.setTargetFragment(LatestEpisodesFragment.this, REQUEST_QUALITY);
                dialog.show(fm, DIALOG_CHOOSE_QUALITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private class EpisodeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Episode mEpisode;
        private TextView mNumberTitleTextView;
        private TextView mDateTextView;
        private TextView mRunningTimeTextView;
        private ImageView mCoverArtImageView;

        public EpisodeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNumberTitleTextView = (TextView) itemView.findViewById(R.id.episode_number_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.episode_date);
            mRunningTimeTextView = (TextView) itemView.findViewById(R.id.episode_running_time);
            mCoverArtImageView = (ImageView) itemView.findViewById(R.id.list_item_cover_art);
        }

        public void bindEpisode(Episode episode) {
            mEpisode = episode;
            mCoverArtImageView.setImageDrawable(episode.getShow().getCoverArt());
            mNumberTitleTextView.setText(episode.getDisplayTitle());
            mRunningTimeTextView.setText(episode.getRunningTime());

            String dateString = DateFormat.format("MMM d", episode.getPublicationDate()).toString();
            mDateTextView.setText(dateString);
        }

        @Override
        public void onClick(View v) {
            mCallbacks.playVideo(mEpisode);
        }
    }

    private class EpisodeAdapter extends RecyclerView.Adapter<EpisodeHolder> {

        @Override
        public EpisodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_episode_small, parent, false);
            return new EpisodeHolder(view);
        }

        @Override
        public void onBindViewHolder(EpisodeHolder holder, int position) {
            Episode episode = mEpisodes.get(position);
            holder.bindEpisode(episode);
        }

        @Override
        public int getItemCount() {
            return mEpisodes.size();
        }
    }

    private class FetchMoreEpisodesTask extends AsyncTask<Void, Void, List<Episode>> {

        @Override
        protected List<Episode> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchAllEpisodes();
        }

        @Override
        protected void onPostExecute(List<Episode> episodeList) {
            boolean newShows = mTWiTLab.addEpisodes(episodeList);

            mRecyclerView.getAdapter().notifyDataSetChanged();
            mSwipeRefresh.setRefreshing(false);

            if (newShows) {
                mTWiTLab.saveShows();
                mTWiTLab.saveEpisodes();
            }
        }
    }
}
