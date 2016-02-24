package com.tragicfruit.twitcast;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

/**
 * Created by Jeremy on 24/02/2016.
 */
public class UpdatingShowsFragment extends DialogFragment {
    public static UpdatingShowsFragment newInstance() {
        return new UpdatingShowsFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage(getString(R.string.updating_shows_dialog_text));

        return dialog;
    }
}
