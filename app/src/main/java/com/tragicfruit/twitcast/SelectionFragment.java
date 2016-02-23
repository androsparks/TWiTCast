package com.tragicfruit.twitcast;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Jeremy on 23/02/2016.
 */
public class SelectionFragment extends Fragment {
    private RecyclerView mRecyclerView;

    public static SelectionFragment newInstance() {
        return new SelectionFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_selection, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_selection_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        return v;
    }
}
