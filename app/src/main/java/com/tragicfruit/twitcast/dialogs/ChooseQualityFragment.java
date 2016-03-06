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

import com.tragicfruit.twitcast.utils.QueryPreferences;
import com.tragicfruit.twitcast.R;
import com.tragicfruit.twitcast.episode.StreamQuality;

/**
 * Created by Jeremy on 6/03/2016.
 */
public class ChooseQualityFragment extends DialogFragment {
    private static final String EXTRA_QUALITY = "com.tragicfruit.twitcast.quality";

    private RadioGroup mRadioGroup;
    private RadioButton mVideoHdRadioButton;
    private RadioButton mVideoLargeRadioButton;
    private RadioButton mVideoSmallRadioButton;
    private RadioButton mAudioRadioButton;

    public static ChooseQualityFragment newInstance() {
        return new ChooseQualityFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_choose_quality, null);
        mRadioGroup = (RadioGroup) v.findViewById(R.id.dialog_choose_quality_radio_group);
        mVideoHdRadioButton = (RadioButton) v.findViewById(R.id.quality_video_hd);
        mVideoLargeRadioButton = (RadioButton) v.findViewById(R.id.quality_video_large);
        mVideoSmallRadioButton = (RadioButton) v.findViewById(R.id.quality_video_small);
        mAudioRadioButton = (RadioButton) v.findViewById(R.id.quality_audio);

        selectQualityButton(QueryPreferences.getStreamQuality(getActivity()));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.choose_quality_menu_item)
                .setView(v)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int selectedItem = mRadioGroup.getCheckedRadioButtonId();
                        switch (selectedItem) {
                            case R.id.quality_video_hd:
                                sendResult(Activity.RESULT_OK, StreamQuality.VIDEO_HD);
                                break;
                            case R.id.quality_video_large:
                                sendResult(Activity.RESULT_OK, StreamQuality.VIDEO_LARGE);
                                break;
                            case R.id.quality_video_small:
                                sendResult(Activity.RESULT_OK, StreamQuality.VIDEO_SMALL);
                                break;
                            case R.id.quality_audio:
                                sendResult(Activity.RESULT_OK, StreamQuality.AUDIO);
                                break;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .create();
    }

    private void selectQualityButton(StreamQuality quality) {
        switch (quality) {
            case VIDEO_HD:
                mVideoHdRadioButton.setChecked(true);
                break;
            case VIDEO_LARGE:
                mVideoLargeRadioButton.setChecked(true);
                break;
            case VIDEO_SMALL:
                mVideoSmallRadioButton.setChecked(true);
                break;
            case AUDIO:
                mAudioRadioButton.setChecked(true);
                break;
        }
    }

    private void sendResult(int resultCode, StreamQuality quality) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent data = new Intent();
        data.putExtra(EXTRA_QUALITY, quality.toString());

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
    }

    public static StreamQuality getStreamQuality(Intent data) {
        String qualityString = data.getStringExtra(EXTRA_QUALITY);
        return StreamQuality.valueOf(qualityString);
    }
}
