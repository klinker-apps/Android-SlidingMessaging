package com.klinker.android.messaging_sliding.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intent2 = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
        context.sendBroadcast(intent2);

        // clear custom light flow broadcast
        Intent lightFlow = new Intent("com.klinker.android.messaging.CLEAR_NOTIFICATION");
        context.sendBroadcast(lightFlow);

        Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
        PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pStopRepeating);

        Intent floatingNotifications = new Intent();
        floatingNotifications.setAction("robj.floating.notifications.dismiss");
        floatingNotifications.putExtra("package", context.getPackageName());
        context.sendBroadcast(floatingNotifications);
    }
}
