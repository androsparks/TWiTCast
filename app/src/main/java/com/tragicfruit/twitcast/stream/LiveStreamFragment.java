package com.tragicfruit.twitcast.stream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.dialogs.ChooseSourceFragment;
import com.tragicfruit.twitcast.utils.QueryPreferences;

/**
 * Created by Jeremy on 9/03/2016.
 */
public class LiveStreamFragment extends Fragment {
    private static final int REQUEST_SOURCE = 0;
    private static final String DIALOG_CHOOSE_SOURCE = "dialog_choose_source";

    private Button mPlayButton;

    private Callbacks mCallbacks;

    public interface Callbacks {
        void playLiveStream();
        void refreshLiveStream();
    }

    public static LiveStreamFragment newInstance() {
        return new LiveStreamFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_live_stream, container, false);

        mPlayButton = (Button) v.findViewById(R.id.twit_live_play_button);
        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallbacks.playLiveStream();
            }
        });

        return v;
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
}
