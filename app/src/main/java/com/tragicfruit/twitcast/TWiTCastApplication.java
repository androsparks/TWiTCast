package com.tragicfruit.twitcast;

import android.app.Application;

/**
 * Created by Jeremy on 17/03/2016.
 */
public class TWiTCastApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

//        CastConfiguration options = new CastConfiguration.Builder(Constants.GOOGLE_CAST_APP_ID)
//                .enableAutoReconnect()
//                .enableDebug()
//                .enableLockScreen()
//                .enableWifiReconnection()
//                .enableNotification()
////                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_SKIP_PREVIOUS, false)
////                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_SKIP_NEXT, false)
//                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_REWIND, true)
//                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
//                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_FORWARD, true)
//                .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, false)
//                .setMediaRouteDialogFactory(new VideoMediaRouteDialogFactory())
//                .setNextPrevVisibilityPolicy(CastConfiguration.NEXT_PREV_VISIBILITY_POLICY_ALWAYS)
//                .build();
//
//        VideoCastManager.initialize(this, options);
    }
}
