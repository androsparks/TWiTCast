package com.tragicfruit.twitcast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jeremy on 23/02/2016.
 */
public class SelectionFragment extends Fragment {
    private static final String TAG = "SelectionFragment";

    private RecyclerView mRecyclerView;
    private List<Show> mShows;

    public static SelectionFragment newInstance() {
        return new SelectionFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        new FetchShowsTask().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_selection_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        setupAdapter();

        return v;
    }

    private void setupAdapter() {
        if (isAdded()) {
            if (mShows != null) {
                mRecyclerView.setAdapter(new ShowAdapter(mShows));
            } else {
                mRecyclerView.setAdapter(new ShowAdapter(new ArrayList<Show>()));
            }
        }
    }

    private class ShowHolder extends RecyclerView.ViewHolder {
        private TextView mTitleTextView;

        public ShowHolder(View itemView) {
            super(itemView);

            mTitleTextView = (TextView) itemView;
        }

        public void bindShow(Show show) {
            String title = show.getTitle();
            mTitleTextView.setText(title);
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
            View view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
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
        protected List<Show> doInBackground(Void... params) {
            return new TWiTFetcher().fetchShows();
        }

        @Override
        protected void onPostExecute(List<Show> shows) {
            mShows = shows;
            setupAdapter();
        }
    }
}
