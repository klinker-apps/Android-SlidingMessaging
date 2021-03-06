package com.klinker.android.messaging_sliding.receivers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivityPopup;
import com.klinker.android.messaging_sliding.quick_reply.SendMessage;

public class QuickTextService extends IntentService {

    public QuickTextService(String name) {
        super(name);
    }

    public QuickTextService() {
        super("Quick Text");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("quick_text", false)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(android.R.color.transparent)
                            .setContentTitle(getResources().getString(R.string.quick_text_notification))
                            .setContentText(getResources().getString(R.string.quick_text_notification_summary))
                            .setOngoing(true)
                            .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.drawable.stat_notify_sms));

            if (android.os.Build.VERSION.SDK_INT >= 16) {
                mBuilder.setPriority(Notification.PRIORITY_MIN);
            }

            String[] favorites = PreferenceManager.getDefaultSharedPreferences(this).getString("quick_text_favorites", "").split("--");

            if (favorites.length == 1 && favorites[0].equals("")) {
                // dont do anything here because there are no favorites
            } else {
                int index = 0;
                for (String favorite : favorites) {
                    String[] person = favorite.split(", ");
                    Intent open = new Intent(Intent.ACTION_SENDTO);
                    open.setClass(this, MainActivity.class);
                    open.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    open.setData(Uri.parse("sms:" + person[1].replace("(", "").replace(")", "").replace("+", "").replace(" ", "").replace("-", "")));
                    PendingIntent pending = PendingIntent.getActivity(this, index++, open, PendingIntent.FLAG_UPDATE_CURRENT);
                    mBuilder.addAction(0, person[0], pending);
                }
            }

            boolean useSlideOver = sharedPrefs.getBoolean("quick_text_slideover", false);

            Intent notifyIntent;

            if (useSlideOver) {
                notifyIntent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                notifyIntent.putExtra("fromHalo", true);
                notifyIntent.putExtra("secAction", true);
                notifyIntent.putExtra("secondaryType", "newMessage");
            } else {
                notifyIntent = new Intent(this, SendMessage.class);
                notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            PendingIntent intent2 = PendingIntent.getActivity(this, 2,
                    notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(intent2);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(3, mBuilder.build());
        } else {
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(3);
        }

        stopSelf();
    }
}