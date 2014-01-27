package com.klinker.android.messaging_sliding.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.widget.Toast;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;

public class DeliveryReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean result = intent.getBooleanExtra("result", true);
        int reportType = Integer.parseInt(sharedPrefs.getString("delivery_reports_type", "2"));
        Uri uri = Uri.parse(intent.getStringExtra("message_uri"));

        String deliveredTo = "";

        Cursor query = context.getContentResolver().query(
                uri,
                new String[] {"_id", "address"},
                null,
                null,
                null
        );

        if (query != null && query.moveToFirst()) {
            deliveredTo = " - " + ContactUtil.findContactName(query.getString(query.getColumnIndex("address")), context);
        }

        switch (reportType) {
            case 1:
                // do nothing, just save and show a checkmark.
                break;
            case 2:
                // give a toast
                Toast.makeText(context, (result ? context.getString(R.string.message_delivered) : context.getString(R.string.message_not_delivered)) + deliveredTo, Toast.LENGTH_SHORT).show();
                break;
            case 3:
                // ugh. give a notification
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

                if (deliveredTo.startsWith(" - ")) {
                    deliveredTo = deliveredTo.substring(3);
                } else {
                    deliveredTo = null;
                }

                if (result) {
                    TextMessageReceiver.setIcon(builder, context);
                    builder.setPriority(Notification.PRIORITY_LOW);
                    builder.setContentTitle(context.getString(R.string.message_delivered));

                    if (deliveredTo != null) {
                        builder.setContentText(deliveredTo);
                    }

                    builder.setTicker(context.getString(R.string.message_delivered));
                } else {
                    builder.setSmallIcon(R.drawable.ic_alert);
                    builder.setPriority(Notification.PRIORITY_HIGH);
                    builder.setContentTitle(context.getString(R.string.message_not_delivered));

                    if (deliveredTo != null) {
                        builder.setContentText(deliveredTo);
                    }

                    builder.setTicker(context.getString(R.string.message_not_delivered));
                }

                builder.setAutoCancel(true);

                Intent resultIntent = new Intent(context, MainActivity.class);

                TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                stackBuilder.addParentStack(MainActivity.class);
                stackBuilder.addNextIntent(resultIntent);
                PendingIntent resultPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_CANCEL_CURRENT
                        );

                builder.setContentIntent(resultPendingIntent);

                long[] pattern = {0L, 400L, 100L, 400L};
                builder.setVibrate(pattern);
                builder.setLights(0xFFffffff, 1000, 2000);

                NotificationManager mNotificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                Notification notification = builder.build();
                mNotificationManager.notify(4, notification);
                break;
        }
    }
}
