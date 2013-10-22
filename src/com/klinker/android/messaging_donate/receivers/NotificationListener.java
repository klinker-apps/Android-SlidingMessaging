package com.klinker.android.messaging_donate.receivers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.messaging_sliding.receivers.VoiceReceiver;

public class NotificationListener extends NotificationListenerService {
    private SharedPreferences sharedPrefs;

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sbn.getPackageName().equals("com.google.android.apps.googlevoice") && sharedPrefs.getString("voice_account", null) != null) {
            cancelNotification("com.google.android.apps.googlevoice", sbn.getTag(), sbn.getId());
            startService(new Intent(this, VoiceReceiver.class));
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("swipe_read", true)) {
            if (sbn.getPackageName().equals(this.getPackageName()) && !(sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false))) {
                startService(new Intent(this, QmMarkRead.class));
            }
        }
    }

    @Override
    public IBinder onBind (Intent intent) {
        return null;
    }
}
