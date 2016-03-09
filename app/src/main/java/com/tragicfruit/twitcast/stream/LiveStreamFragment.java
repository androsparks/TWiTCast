package com.tragicfruit.twitcast.stream;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.database.TWiTLab;

import java.util.List;

/**
 * Created by Jeremy on 9/03/2016.
 */
public class LiveStreamFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<Stream> mStreamList;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void playVideo(Stream stream);
    }

    public static LiveStreamFragment newInstance() {
        return new LiveStreamFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_live_stream, container, false);

        mStreamList = TWiTLab.get(getActivity()).getStreams();

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_live_stream_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(new StreamAdapter());

        return v;
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

    private class StreamHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Stream mStream;
        private TextView mStreamTitleTextView;

        public StreamHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            mStreamTitleTextView = (TextView) itemView;
        }

        private void bindStream(Stream stream) {
            mStream = stream;
            mStreamTitleTextView.setText(stream.getTitle());
        }

        @Override
        public void onClick(View v) {
            mCallbacks.playVideo(mStream);
        }
    }

    private class StreamAdapter extends RecyclerView.Adapter<StreamHolder> {

        @Override
        public StreamHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            View v = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            return new StreamHolder(v);
        }

        @Override
        public void onBindViewHolder(StreamHolder holder, int position) {
            Stream stream = mStreamList.get(position);
            holder.bindStream(stream);
        }

        @Override
        public int getItemCount() {
            return mStreamList.size();
        }
    }
}
