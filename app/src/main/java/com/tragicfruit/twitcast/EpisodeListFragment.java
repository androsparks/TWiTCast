package com.tragicfruit.twitcast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Jeremy on 29/02/2016.
 */
public class EpisodeListFragment extends Fragment {
    private static final String ARG_SHOW_ID = "show_id";

    private RecyclerView mRecyclerView;
    private ImageView mCoverArtImageView;
    private TextView mTitleTextView;
    private TextView mDescriptionTextView;

    private Show mShow;
    private List<Episode> mEpisodeList;

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

        int showId = getArguments().getInt(ARG_SHOW_ID);
        mShow = TWiTDatabase.get().getShow(showId);
        mEpisodeList = mShow.getEpisodes();

        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setTitle(mShow.getTitle());
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

        return v;
    }

    private class EpisodeHolder extends RecyclerView.ViewHolder {
        private TextView mNumberTitleTextView;
        private TextView mDateTextView;
        private TextView mRunningTimeTextView;

        public EpisodeHolder(View itemView) {
            super(itemView);

            mNumberTitleTextView = (TextView) itemView.findViewById(R.id.episode_number_title);
            mDateTextView = (TextView) itemView.findViewById(R.id.episode_date);
            mRunningTimeTextView = (TextView) itemView.findViewById(R.id.episode_running_time);
        }

        public void bindEpisode(Episode episode) {
            mNumberTitleTextView.setText(getString(R.string.episode_number_title, episode.getEpisodeNumber(), episode.getTitle()));
            mRunningTimeTextView.setText(getString(R.string.episode_running_time, episode.getRunningTimeInMinutes()));

            String dateString = DateFormat.format("EEEE, MMMM d", episode.getAiringDate()).toString();
            mDateTextView.setText(dateString);
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
