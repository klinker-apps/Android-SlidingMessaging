package com.klinker.android.messaging_sliding.scheduled;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledDataSource;
import com.klinker.android.messaging_sliding.scheduled.scheduled.ScheduledMessage;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

public class ScheduledService extends IntentService {

    private static final String TAG = "ScheduledService";
    private static final int SCHEDULED_ALARM_REQUEST_CODE = 5423;
    private static boolean running = false;

    public ScheduledService() {
        super(TAG);
    }

    @Override
    public void onHandleIntent(Intent intent) {
        Log.v(TAG, "started service");
        if (running) {
            Log.v(TAG, "service already running, exiting");
            stopSelf();
            return;
        }

        running = true;

        Log.v(TAG, "opening datasource");
        ScheduledDataSource dataSource = new ScheduledDataSource(this);
        dataSource.open();

        Log.v(TAG, "getting message");
        ScheduledMessage message = dataSource.getFirstMessage();
        String body = message.body;
        String number = message.address;
        String attachment = message.attachment;
        Log.v(TAG, "number: " + number + ", body: " + body + ", attachment: " + attachment +
                ", date: " + message.date + ", repetition: " + message.repetition);

        long currentTime = System.currentTimeMillis();
        if (!(message.date > currentTime - 1000*60*5 && message.date < currentTime + 1000*60*5)) {
            Log.v(TAG, "message is outside time range, dont send");
            stopSelf();
            return;
        } else if (checkExisting(this, message.body, message.address, message.date, 2 * 60 * 60 * 1000)) {
            Log.v(TAG, "message already exists, don't send again");
            stopSelf();
            return;
        }

        Log.v(TAG, "sending message");

        SendUtil.sendMessage(this, number.replace(";", "").split(" "), body);


        dataSource.deleteMessage(message);
        Log.v(TAG, "deleted message");

        if (message.repetition != ScheduledMessage.REPEAT_NEVER) {
            Log.v(TAG, "message needs readded because of its repetition - " + message.repetition);
            if (message.repetition == ScheduledMessage.REPEAT_MONTHLY) {
                Date d = new Date(message.date);
                if (d.getMonth() == 11) {
                    d.setMonth(0);
                    d.setYear(d.getYear() + 1);
                } else {
                    if ((d.getMonth() == 0 && d.getDay() > 28) || d.getDay() == 31) {
                        d.setMonth(d.getMonth() + 2);
                    } else {
                        d.setMonth(d.getMonth() + 1);
                    }
                }

                message.date = d.getTime();
            } else if (message.repetition == ScheduledMessage.REPEAT_YEARLY) {
                Date d = new Date(message.date);
                d.setYear(d.getYear() + 1);
                message.date = d.getTime();
            } else {
                message.date += message.repetition;
            }
            dataSource.addMessage(message);
            Log.v(TAG, "added message at new time: " + message.date);
        } else {
            if (message.attachment != null && !message.attachment.equals("")) {
                File f = new File(Uri.parse(message.attachment).getPath());
                boolean result = f.delete();
                Log.v(TAG, "deleting result = " + result);
            }
        }

        Log.v(TAG, "closing datasource");
        dataSource.close();
        giveNotification(body);

        Log.v(TAG, "scheduling next alarm");
        scheduleNextAlarm(this);

        stopSelf();
        running = false;
    }

    private void giveNotification(String body) {
        Log.v(TAG, "giving notification with body: " + body);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.stat_notify_sms)
                        .setContentTitle(getString(R.string.scheduled_success))
                        .setContentText(body);

        Intent resultIntent = new Intent(this, ScheduledSms.class);

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

    public static void scheduleNextAlarm(Context context) {
        Intent intent = new Intent(context, ScheduledService.class);
        PendingIntent pIntent = PendingIntent.getService(context, SCHEDULED_ALARM_REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);

        // cancel the current request if it exists, we'll just make a completely new one
        alarmManager.cancel(pIntent);

        ScheduledDataSource dataSource = new ScheduledDataSource(context);
        dataSource.open();
        ScheduledMessage message = dataSource.getFirstMessage();

        if (message == null) {
            Log.v(TAG, "no scheduled messages");
            return;
        }

        long time = message.date;
        dataSource.close();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, time, pIntent);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);
        }

        Log.v(TAG, "Set alarm for " + time + " (current time: " + System.currentTimeMillis() + ")");
    }

    public static boolean checkExisting(Context context, String body, String address, long currentDate, long dateRangeMillis) {
        try {
            Cursor query = context.getContentResolver().query(Telephony.Sms.Outbox.CONTENT_URI,
                    new String[]{Telephony.Sms.ADDRESS, Telephony.Sms.BODY, Telephony.Sms.DATE},
                    Telephony.Sms.ADDRESS + "=? AND " + Telephony.Sms.BODY + "=?",
                    new String[]{address, body},
                    Telephony.Sms.DATE + " DESC");

            if (query != null && query.moveToFirst()) {
                long d = query.getLong(query.getColumnIndex(Telephony.Sms.DATE));

                // if a message with the same body to the same sender was sent in the last 2 hours
                // then don't send this scheduled one
                if (currentDate - dateRangeMillis < d) return true;
            }
        } catch (Exception e) {
        }

        return false;
    }
}