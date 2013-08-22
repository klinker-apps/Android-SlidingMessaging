package com.klinker.android.messaging_sliding.scheduled;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.SendUtil;
import com.klinker.android.send_message.StripAccents;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScheduledService extends IntentService {

    SharedPreferences sharedPrefs;

    public ScheduledService() {
        super("ScheduledService");
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        ArrayList<String> numbers = new ArrayList<String>();

        String number = intent.getStringExtra(ScheduledSms.EXTRA_NUMBER);
        String message = intent.getStringExtra(ScheduledSms.EXTRA_MESSAGE);

        number.replaceAll(" ", "");

        while (number.contains(";"))
        {
            numbers.add(number.substring(0, number.indexOf(';')));
            number = number.substring(number.indexOf(';') + 1, number.length());
        }


        try
        {
            for (int i = 0; i < numbers.size(); i++)
            {
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

        } catch (Exception e)
        {

        }
    }
}
