package com.klinker.android.messaging_sliding.quick_reply;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;

import java.util.ArrayList;

public class QmMarkRead2 extends IntentService {

    public QmMarkRead2() {
        super("service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        readSMS(this);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

        IOUtil.writeNotifications(new ArrayList<String>(), this);
        IOUtil.writeNewMessages(new ArrayList<String>(), this);

        Intent intent2 = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
        this.sendBroadcast(intent2);

        Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
        PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pStopRepeating);

        Intent floatingNotifications = new Intent();
        floatingNotifications.setAction("robj.floating.notifications.dismiss");
        floatingNotifications.putExtra("package", getPackageName());
        sendBroadcast(floatingNotifications);

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        Intent clearMessages = new Intent("com.klinker.android.messaging.CLEAR_MESSAGES");
        getApplicationContext().sendBroadcast(clearMessages);

        stopSelf();

    }

    public void readSMS(Context context) {
        try {
            Uri uriSms = Uri.parse("content://sms/inbox");
            Cursor c = context.getContentResolver().query(uriSms,
                    new String[]{"_id", "thread_id", "address",
                            "person", "date", "body", "read"}, null, null, "date DESC LIMIT 10");

            if (c != null && c.moveToFirst()) {
                do {
                    String id = c.getString(0);

                    ContentValues values = new ContentValues();
                    values.put("read", true);
                    getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + id, null);
                } while (c.moveToNext());
            }
            c.close();
        } catch (Exception e) {

        }
    }
}
