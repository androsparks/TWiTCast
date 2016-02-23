package com.tragicfruit.twitcast;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class SelectionActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return SelectionFragment.newInstance();
    }
}
