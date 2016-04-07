package com.tragicfruit.twitcast.stream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.dialogs.ChooseSourceFragment;
import com.tragicfruit.twitcast.utils.QueryPreferences;
import com.tragicfruit.twitcast.database.TWiTFetcher;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Jeremy on 9/03/2016.
 */
public class LiveStreamFragment extends Fragment {
    private static final String TAG = "LiveStreamFragment";
    private static final int REQUEST_SOURCE = 0;
    private static final String DIALOG_CHOOSE_SOURCE = "dialog_choose_source";

    private ImageView mPlayButton;
    private RecyclerView mTodayRecyclerView;
    private RecyclerView mTomorrowRecyclerView;
    private TextView mTodayLoading;
    private TextView mTomorrowLoading;

    private boolean mFetchedUpcomingEpisodes;
    private FetchUpcomingEpisodesTask mUpcomingEpisodesTask;
    private List<UpcomingEpisode> mTodayList;
    private List<UpcomingEpisode> mTomorrowList;
    private Callbacks mCallbacks;

    public interface Callbacks {
        void playLiveStream();
        void refreshLiveStream();
        void showNoConnectionSnackbar();
    }

    public static LiveStreamFragment newInstance() {
        return new LiveStreamFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && !mFetchedUpcomingEpisodes) {
            if (isNetworkAvailableAndConnected()) {
                mUpcomingEpisodesTask = new FetchUpcomingEpisodesTask();
                mUpcomingEpisodesTask.execute();
            } else {
                mCallbacks.showNoConnectionSnackbar();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_live_stream, container, false);

        mPlayButton = (ImageView) v.findViewById(R.id.twit_live_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.playLiveStream();
            }
        });

        mTodayRecyclerView = (RecyclerView) v.findViewById(R.id.today_upcoming_shows_recycler_view);
        mTodayRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTodayRecyclerView.setNestedScrollingEnabled(false);

        mTomorrowRecyclerView = (RecyclerView) v.findViewById(R.id.tomorrow_upcoming_shows_recycler_view);
        mTomorrowRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mTomorrowRecyclerView.setNestedScrollingEnabled(false);

        mTodayLoading = (TextView) v.findViewById(R.id.today_loading_text);
        mTomorrowLoading = (TextView) v.findViewById(R.id.tomorrow_loading_text);

        setupAdapters();

        return v;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mUpcomingEpisodesTask != null) {
            mUpcomingEpisodesTask.cancel(false);
        }
    }

    private void setupAdapters() {
        if (isAdded()) {
            if (mTodayList != null) {
                mTodayLoading.setVisibility(View.GONE);
                mTodayRecyclerView.setAdapter(new UpcomingEpisodesAdapter(mTodayList));
            } else {
                if (mFetchedUpcomingEpisodes) {
                    mTodayLoading.setText(R.string.upcoming_shows_error);
                }
            }

            if (mTomorrowList != null) {
                mTomorrowLoading.setVisibility(View.GONE);
                mTomorrowRecyclerView.setAdapter(new UpcomingEpisodesAdapter(mTomorrowList));
            } else {
                if (mFetchedUpcomingEpisodes) {
                    mTomorrowLoading.setText(R.string.upcoming_shows_error);
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_live_stream, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.choose_source:
                FragmentManager fm = getFragmentManager();
                ChooseSourceFragment dialog = ChooseSourceFragment.newInstance();
                dialog.setTargetFragment(LiveStreamFragment.this, REQUEST_SOURCE);
                dialog.show(fm, DIALOG_CHOOSE_SOURCE);
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

        if (requestCode == REQUEST_SOURCE) {
            StreamSource source = ChooseSourceFragment.getStreamSource(data);
            QueryPreferences.setStreamSource(getActivity(), source);
            mCallbacks.refreshLiveStream();
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

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();

        return isNetworkConnected;
    }

    private class UpcomingEpisodeHolder extends RecyclerView.ViewHolder {
        private TextView mTimeTextView;
        private TextView mTitleTextView;

        public UpcomingEpisodeHolder(View itemView) {
            super(itemView);

            mTimeTextView = (TextView) itemView.findViewById(R.id.upcoming_episode_time);
            mTitleTextView = (TextView) itemView.findViewById(R.id.upcoming_episode_title);
        }

        public void bindUpcomingEpisode(UpcomingEpisode episode) {
            mTimeTextView.setText(episode.getDisplayTime());
            mTitleTextView.setText(episode.getTitle());
        }
    }

    private class UpcomingEpisodesAdapter extends RecyclerView.Adapter<UpcomingEpisodeHolder> {
        private List<UpcomingEpisode> mUpcomingEpisodes;

        public UpcomingEpisodesAdapter(List<UpcomingEpisode> upcomingEpisodeList) {
            mUpcomingEpisodes = upcomingEpisodeList;
        }

        @Override
        public UpcomingEpisodeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View view = inflater.inflate(R.layout.list_item_upcoming_episode, parent, false);
            return new UpcomingEpisodeHolder(view);
        }

        @Override
        public void onBindViewHolder(UpcomingEpisodeHolder holder, int position) {
            UpcomingEpisode episode = mUpcomingEpisodes.get(position);
            holder.bindUpcomingEpisode(episode);
        }

        @Override
        public int getItemCount() {
            return mUpcomingEpisodes.size();
        }
    }

    private class FetchUpcomingEpisodesTask extends AsyncTask<Void, Void, List<UpcomingEpisode>> {

        @Override
        protected List<UpcomingEpisode> doInBackground(Void... params) {
            return new TWiTFetcher(getActivity()).fetchUpcomingEpisodes();
        }

        @Override
        protected void onPostExecute(List<UpcomingEpisode> upcomingEpisodes) {
            if (isCancelled()) {
                return;
            }

            mFetchedUpcomingEpisodes = true;
            if (upcomingEpisodes != null) {
                mTodayList = new ArrayList<>();
                mTomorrowList = new ArrayList<>();

                // allocated to different lists
                for (UpcomingEpisode episode : upcomingEpisodes) {
                    if (isToday(episode.getAiringDate())) {
                        mTodayList.add(episode);
                    } else if (isTomorrow(episode.getAiringDate())) {
                        mTomorrowList.add(episode);
                    }
                }
            }

            setupAdapters();
        }

        private boolean isToday(Date date) {
            // today
            Calendar calendar = new GregorianCalendar();

            // midnight today
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // midnight tomorrow
            calendar.add(Calendar.DAY_OF_MONTH, 1);

            return date.before(calendar.getTime());
        }

        private boolean isTomorrow(Date date) {
            // today
            Calendar calendar = new GregorianCalendar();

            // midnight today
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // second midnight from now
            calendar.add(Calendar.DAY_OF_MONTH, 2);

            return date.before(calendar.getTime());
        }
    }
}
