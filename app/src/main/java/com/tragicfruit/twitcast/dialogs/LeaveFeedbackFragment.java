package com.tragicfruit.twitcast.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.tragicfruit.twitcast.R;

/**
 * Created by Jeremy on 16/03/2016.
 */
public class LeaveFeedbackFragment extends DialogFragment {
    public static LeaveFeedbackFragment newInstance() {
        return new LeaveFeedbackFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_leave_feedback, null);

        View email = view.findViewById(R.id.email_feedback);
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get version number
                String version;
                try {
                    version = getActivity().getPackageManager()
                            .getPackageInfo(getActivity().getPackageName(), 0).versionName;
                } catch (Exception e) {
                    version = "no version";
                }

                // Open email app with email address and subject
                try {
                    String uriString = "mailto:"
                            + getString(R.string.feedback_email_address)
                            + "?subject=" + getString(R.string.feedback_email_subject, version);
                    Intent i = new Intent(Intent.ACTION_SENDTO);
                    i.setData(Uri.parse(uriString));
                    startActivity(i);
                } catch (android.content.ActivityNotFoundException e) { // Email app not found
                    Toast.makeText(getActivity(), R.string.no_email_app_error, Toast.LENGTH_SHORT)
                            .show();
                }

                dismiss();
            }
        });

        View playStore = view.findViewById(R.id.play_store_feedback);
        playStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String appPackageName = getActivity().getPackageName();
                try {
                    Uri playStoreLink = Uri.parse("market://details?id=" + appPackageName);
                    Intent i = new Intent(Intent.ACTION_VIEW, playStoreLink);
                    startActivity(i);
                } catch (android.content.ActivityNotFoundException e) {
                    // Devices without Play Store
                    Uri playStoreLink = Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName);
                    Intent i = new Intent(Intent.ACTION_VIEW, playStoreLink);
                    startActivity(i);
                }

                dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.leave_feedback_menu_item)
                .setView(view)
                .create();
    }
}
