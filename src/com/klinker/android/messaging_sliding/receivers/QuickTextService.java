package com.klinker.android.messaging_sliding.receivers;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.klinker.android.messaging_donate.R;
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

        boolean useSlideOver = sharedPrefs.getBoolean("quick_text_slideover", false);
        
        Intent notifyIntent;
                
        if (useSlideOver) {
                notifyIntent = new Intent(this, MainActivityPopup.class);
                notifyIntent.putExtra("secAction", true);
                intent.putExtra("secondaryType", "newMessage");
        } else {
                notifyIntent = new Intent(this, SendMessage.class);
        }

        notifyIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent intent2 = PendingIntent.getActivity(this, 2,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(intent2);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(3, mBuilder.build());

        stopSelf();
    }
}