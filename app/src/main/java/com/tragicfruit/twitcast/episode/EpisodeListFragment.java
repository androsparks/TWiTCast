package com.tragicfruit.twitcast.episode;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.database.TWiTLab;
import com.tragicfruit.twitcast.dialogs.ChooseQualityFragment;
import com.tragicfruit.twitcast.show.Show;
import com.tragicfruit.twitcast.utils.QueryPreferences;
import com.tragicfruit.twitcast.utils.TWiTFetcher;

import java.util.List;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class EpisodeListFragment extends Fragment {
    private static final String ARG_SHOW_ID = "show_id";
    private static final String DIALOG_CHOOSE_QUALITY = "choose_quality";
    private static final int REQUEST_QUALITY = 0;

    private RecyclerView mRecyclerView;
    private ImageView mCoverArtImageView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;
    private ProgressBar mLoadingProgressBar;
    private SwipeRefreshLayout mSwipeRefresh;

    private Show mShow;
    private List<Episode> mEpisodeList;
    private TWiTLab mTWiTLab;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void playVideo(Episode episode);
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

    public static EpisodeListFragment newInstance(int showId) {
        Bundle args = new Bundle();
        args.putInt(ARG_SHOW_ID, showId);

        EpisodeListFragment fragment = new EpisodeListFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);

        mTWiTLab = TWiTLab.get(getActivity());
        int showId = getArguments().getInt(ARG_SHOW_ID);
        mShow = mTWiTLab.getShow(showId);
        mEpisodeList = mShow.getEpisodes();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setTitle(mShow.getTitle());
        }

        if (!mShow.hasLoadedAllEpisodes()) {
            new FetchMoreEpisodesTask().execute();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_episode_list, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // maintain transition animation
                getActivity().onBackPressed();
                return true;
            case R.id.choose_quality:
                FragmentManager fm = getFragmentManager();
                ChooseQualityFragment dialog = ChooseQualityFragment.newInstance();
                dialog.setTargetFragment(EpisodeListFragment.this, REQUEST_QUALITY);
                dialog.show(fm, DIALOG_CHOOSE_QUALITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_episode_list, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_episode_list_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        mRecyclerView.setAdapter(new EpisodeAdapter());
        mRecyclerView.setFocusable(false); // to allow scroll view to start at the top

        mCoverArtImageView = (ImageView) v.findViewById(R.id.show_cover_art);
        mCoverArtImageView.setImageDrawable(mShow.getCoverArt());

        mTitleTextView = (TextView) v.findViewById(R.id.show_title);
        mTitleTextView.setText(mShow.getTitle());

        mDescriptionTextView = (TextView) v.findViewById(R.id.show_description);
        mDescriptionTextView.setText(mShow.getDescription());

        mLoadingProgressBar = (ProgressBar) v.findViewById(R.id.loading_more_episodes_progress_bar);
        if (mShow.hasLoadedAllEpisodes()) {
            mLoadingProgressBar.setVisibility(View.GONE);
        }

        mSwipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.fragment_episode_list_swipe_refresh);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_QUALITY) {
            StreamQuality quality = ChooseQualityFragment.getStreamQuality(data);
            QueryPreferences.setStreamQuality(getActivity(), quality);
        }
    }

    private class FetchMoreEpisodesTask extends AsyncTask<Void, Void, List<Episode>> {

        @Override
        protected List<Episode> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchEpisodes(mShow);
        }

        @Override
        protected void onPostExecute(List<Episode> episodeList) {
            boolean newShows = mTWiTLab.addEpisodes(episodeList, mShow);

            mRecyclerView.getAdapter().notifyDataSetChanged();
            mLoadingProgressBar.setVisibility(View.GONE);
            mSwipeRefresh.setRefreshing(false);

            if (newShows) {
                getActivity().setResult(Activity.RESULT_OK);
                mTWiTLab.saveShows();
                mTWiTLab.saveEpisodes();
            }
        }
    }

    private class EpisodeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Episode mEpisode;
        private TextView mNumberTitleTextView;
        private TextView mDateTextView;
        private TextView mRunningTimeTextView;
        private TextView mDescriptionTextView;

        public EpisodeHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);

            mNumberTitleTextView = (TextView) itemView.findViewById(R.id.episode_number_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.episode_date);
            mRunningTimeTextView = (TextView) itemView.findViewById(R.id.episode_running_time);
            mDescriptionTextView = (TextView) itemView.findViewById(R.id.episode_description);
        }

        public void bindEpisode(Episode episode) {
            mEpisode = episode;
            mNumberTitleTextView.setText(episode.getDisplayTitle());
            mRunningTimeTextView.setText(episode.getRunningTime());
            mDescriptionTextView.setText(episode.getSubtitle());

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
            View view = inflater.inflate(R.layout.list_item_episode, parent, false);
            return new EpisodeHolder(view);
        }

        @Override
        public void onBindViewHolder(EpisodeHolder holder, int position) {
            Episode episode = mEpisodeList.get(position);
            holder.bindEpisode(episode);
        }

        @Override
        public int getItemCount() {
            return mEpisodeList.size();
        }
    }
}
