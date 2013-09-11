package com.klinker.android.messaging_sliding.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class SendErrorReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(com.klinker.android.send_message.R.drawable.ic_alert)
                        .setContentTitle("Error")
                        .setContentText("Could not send message");

        mBuilder.setAutoCancel(true);
        long[] pattern = {0L, 400L, 100L, 400L};
        mBuilder.setVibrate(pattern);
        mBuilder.setLights(0xFFffffff, 1000, 2000);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = mBuilder.build();
        mNotificationManager.notify(4, notification);
    }
}
