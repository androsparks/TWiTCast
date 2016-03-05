package com.tragicfruit.twitcast;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class UpdatingShowsFragment extends DialogFragment {
    private ProgressDialog mProgressDialog;
    private int mMaxProgress;

    private static final String ARG_MESSAGE = "message";

    public static UpdatingShowsFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE, message);

        UpdatingShowsFragment fragment = new UpdatingShowsFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getArguments().getString(ARG_MESSAGE));

        return mProgressDialog;
    }

    public void setDialogMessage(String message) {
        mProgressDialog.setMessage(message);
    }

    public void setMaxProgress(int max) {
        mMaxProgress = max;
    }

    public void setProgress(int progress) {
        setDialogMessage(getString(R.string.downloading_cover_art_text, progress, mMaxProgress));
    }
}
