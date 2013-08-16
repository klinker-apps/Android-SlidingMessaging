package com.klinker.android.messaging_donate.receivers;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;

public class NotificationListener extends NotificationListenerService {

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if (sbn.getPackageName().equals(this.getPackageName())) {
            startService(new Intent(this, QmMarkRead2.class));
        }
    }
}
