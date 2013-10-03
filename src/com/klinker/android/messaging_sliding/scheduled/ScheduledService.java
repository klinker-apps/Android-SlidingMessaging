package com.klinker.android.messaging_sliding.scheduled;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.SendUtil;

import java.util.ArrayList;

public class ScheduledService extends IntentService {

    SharedPreferences sharedPrefs;

    public ScheduledService() {
        super("ScheduledService");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        ArrayList<String> numbers = new ArrayList<String>();

        String number = intent.getStringExtra(ScheduledSms.EXTRA_NUMBER);
        String message = intent.getStringExtra(ScheduledSms.EXTRA_MESSAGE);

        number.replaceAll(" ", "");

        while (number.contains(";")) {
            numbers.add(number.substring(0, number.indexOf(';')));
            number = number.substring(number.indexOf(';') + 1, number.length());
        }


        try {
            for (int i = 0; i < numbers.size(); i++) {
                SendUtil.sendMessage(this, numbers.get(i), message);

                NotificationCompat.Builder mBuilder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.drawable.stat_notify_sms)
                                .setContentTitle("Sending Successful")
                                .setContentText("Scheduled SMS sent successfully.");

                Intent resultIntent = new Intent(this, com.klinker.android.messaging_sliding.scheduled.ScheduledSms.class);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                mBuilder.setContentIntent(resultPendingIntent);

                int mNotificationId = 5;

                NotificationManager mNotifyMgr =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

                mNotifyMgr.notify(mNotificationId, mBuilder.build());
            }

        } catch (Exception e) {

        }
    }
}
