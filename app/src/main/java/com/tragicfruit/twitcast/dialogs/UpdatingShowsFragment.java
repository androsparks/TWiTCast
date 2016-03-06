package com.tragicfruit.twitcast.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.tragicfruit.twitcast.R;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class UpdatingShowsFragment extends DialogFragment {
    private ProgressDialog mProgressDialog;

    public static UpdatingShowsFragment newInstance() {
        return new UpdatingShowsFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mProgressDialog.setMessage(getString(R.string.updating_shows_text));

        return mProgressDialog;
    }
}
