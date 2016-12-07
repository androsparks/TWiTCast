package com.tragicfruit.twitcast.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.stream.StreamSource;
import com.tragicfruit.twitcast.utils.QueryPreferences;

/**
 * Created by Jeremy on 10/03/2016.
 */
public class ChooseSourceFragment extends DialogFragment {
    private static final String EXTRA_SOURCE = "com.tragicfruit.twitcast.source";

    private RadioGroup mRadioGroup;
    private RadioButton mFlosoftHighRadioButton;
    private RadioButton mFlosoftLowRadioButton;
    private RadioButton mTwitchRadioButton;
    private RadioButton mUstreamRadioButton;
    private RadioButton mAudioRadioButton;

    public static ChooseSourceFragment newInstance() {
        return new ChooseSourceFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_source, null);
        mRadioGroup = (RadioGroup) v.findViewById(R.id.dialog_choose_source_radio_group);
        mFlosoftHighRadioButton = (RadioButton) v.findViewById(R.id.source_flosoft_high);
        mFlosoftLowRadioButton = (RadioButton) v.findViewById(R.id.source_flosoft_low);
        mTwitchRadioButton = (RadioButton) v.findViewById(R.id.source_twitch);
        mUstreamRadioButton = (RadioButton) v.findViewById(R.id.source_ustream);
        mAudioRadioButton = (RadioButton) v.findViewById(R.id.source_audio);

        selectSourceButton(QueryPreferences.getStreamSource(getActivity()));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_source_menu_item)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedItem = mRadioGroup.getCheckedRadioButtonId();
                        switch (selectedItem) {
                            case R.id.source_flosoft_high:
                                sendResult(Activity.RESULT_OK, StreamSource.FLOSOFT_HIGH);
                                break;
                            case R.id.source_flosoft_low:
                                sendResult(Activity.RESULT_OK, StreamSource.FLOSOFT_LOW);
                                break;
                            case R.id.source_twitch:
                                sendResult(Activity.RESULT_OK, StreamSource.TWITCH);
                                break;
                            case R.id.source_ustream:
                                sendResult(Activity.RESULT_OK, StreamSource.USTREAM);
                                break;
                            case R.id.source_audio:
                                sendResult(Activity.RESULT_OK, StreamSource.AUDIO);
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void selectSourceButton(StreamSource source) {
        switch (source) {
            case FLOSOFT_HIGH:
                mFlosoftHighRadioButton.setChecked(true);
                break;
            case FLOSOFT_LOW:
                mFlosoftLowRadioButton.setChecked(true);
                break;
            case TWITCH:
                mTwitchRadioButton.setChecked(true);
                break;
            case USTREAM:
                mUstreamRadioButton.setChecked(true);
                break;
            case AUDIO:
                mAudioRadioButton.setChecked(true);
                break;
        }
    }

    private void sendResult(int resultCode, StreamSource source) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_SOURCE, source.toString());

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
    }

    public static StreamSource getStreamSource(Intent data) {
        String sourceString = data.getStringExtra(EXTRA_SOURCE);
        return StreamSource.valueOf(sourceString);
    }
}
