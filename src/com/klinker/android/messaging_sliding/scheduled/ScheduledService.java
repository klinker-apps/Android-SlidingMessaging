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
                sendMessage(this, numbers.get(i), message);

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

    public void sendMessage(final Context context, String number, String body)
    {
        if (sharedPrefs.getBoolean("delivery_reports", false))
        {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(DELIVERED), 0);

            //---when the SMS has been sent---
            registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "2");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_alert)
                                            .setContentTitle("Error")
                                            .setContentText("Could not send message");

                            Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                            mBuilder.setContentIntent(resultPendingIntent);
                            mBuilder.setAutoCancel(true);
                            long[] pattern = {0L, 400L, 100L, 400L};
                            mBuilder.setVibrate(pattern);
                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                            try
                            {
                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                            } catch(Exception e)
                            {
                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            }

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            Notification notification = mBuilder.build();
                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                            mNotificationManager.notify(1, notification);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                    }

                    unregisterReceiver(this);
                }
            }, new IntentFilter(SENT));

            //---when the SMS has been delivered---
            registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    if (sharedPrefs.getString("delivery_options", "2").equals("1"))
                    {
                        switch (getResultCode())
                        {
                            case Activity.RESULT_OK:
                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                builder.setMessage(R.string.message_delivered)
                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });

                                builder.create().show();

                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                if (query.moveToFirst())
                                {
                                    String id = query.getString(query.getColumnIndex("_id"));
                                    ContentValues values = new ContentValues();
                                    values.put("status", "0");
                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                }
                                break;
                            case Activity.RESULT_CANCELED:
                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                builder2.setMessage(R.string.message_not_delivered)
                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });

                                builder2.create().show();

                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                if (query2.moveToFirst())
                                {
                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                    ContentValues values = new ContentValues();
                                    values.put("status", "64");
                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                }
                                break;
                        }
                    } else
                    {
                        switch (getResultCode())
                        {
                            case Activity.RESULT_OK:
                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                {

                                }

                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                if (query.moveToFirst())
                                {
                                    String id = query.getString(query.getColumnIndex("_id"));
                                    ContentValues values = new ContentValues();
                                    values.put("status", "0");
                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                }

                                break;
                            case Activity.RESULT_CANCELED:
                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                {
                                }

                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                if (query2.moveToFirst())
                                {
                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                    ContentValues values = new ContentValues();
                                    values.put("status", "64");
                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                }

                                break;
                        }
                    }

                    context.unregisterReceiver(this);
                }
            }, new IntentFilter(DELIVERED));

            ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();

            String body2 = body;

            if (sharedPrefs.getBoolean("strip_unicode", false))
            {
                body2 = StripAccents.stripAccents(body2);
            }

            if (!sharedPrefs.getString("signature", "").equals(""))
            {
                body2 += "\n" + sharedPrefs.getString("signature", "");
            }

            SmsManager smsManager = SmsManager.getDefault();

            if (sharedPrefs.getBoolean("split_sms", false))
            {
                int length = 160;

                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(body2);

                boolean counter = false;

                if (sharedPrefs.getBoolean("split_counter", false)) {
                    counter = true;
                    length -= 7;
                }

                String[] textToSend = com.klinker.android.messaging_sliding.MainActivity.splitByLength(body2, length, counter);

                for (int i = 0; i < textToSend.length; i++)
                {
                    ArrayList<String> parts = smsManager.divideMessage(textToSend[i]);

                    for (int j = 0; j < parts.size(); j++)
                    {
                        sPI.add(sentPI);
                        dPI.add(deliveredPI);
                    }

                    smsManager.sendMultipartTextMessage(number, null, parts, sPI, dPI);
                }
            } else
            {
                ArrayList<String> parts = smsManager.divideMessage(body2);

                for (int i = 0; i < parts.size(); i++)
                {
                    sPI.add(sentPI);
                    dPI.add(deliveredPI);
                }

                smsManager.sendMultipartTextMessage(number, null, parts, sPI, dPI);
            }
        } else
        {
            String SENT = "SMS_SENT";

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(SENT), 0);

            //---when the SMS has been sent---
            context.registerReceiver(new BroadcastReceiver(){
                @Override
                public void onReceive(Context arg0, Intent arg1) {
                    switch (getResultCode())
                    {
                        case Activity.RESULT_OK:
                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "2");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }

                            break;
                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }

                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(context)
                                            .setSmallIcon(R.drawable.ic_alert)
                                            .setContentTitle("Error")
                                            .setContentText("Could not send message");

                            Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );

                            mBuilder.setContentIntent(resultPendingIntent);
                            mBuilder.setAutoCancel(true);
                            long[] pattern = {0L, 400L, 100L, 400L};
                            mBuilder.setVibrate(pattern);
                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                            try
                            {
                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                            } catch(Exception e)
                            {
                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                            }

                            NotificationManager mNotificationManager =
                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                            Notification notification = mBuilder.build();
                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                            mNotificationManager.notify(1, notification);
                            break;
                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                        case SmsManager.RESULT_ERROR_NULL_PDU:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                            try
                            {
                                wait(500);
                            } catch (Exception e)
                            {

                            }

                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                            if (query.moveToFirst())
                            {
                                String id = query.getString(query.getColumnIndex("_id"));
                                ContentValues values = new ContentValues();
                                values.put("type", "5");
                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                            }


                            break;
                    }

                    unregisterReceiver(this);
                }
            }, new IntentFilter(SENT));

            ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();

            String body2 = body;

            if (sharedPrefs.getBoolean("strip_unicode", false))
            {
                body2 = StripAccents.stripAccents(body2);
            }

            if (!sharedPrefs.getString("signature", "").equals(""))
            {
                body2 += "\n" + sharedPrefs.getString("signature", "");
            }

            SmsManager smsManager = SmsManager.getDefault();

            if (sharedPrefs.getBoolean("split_sms", false))
            {
                int length = 160;

                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(body2);

                if (matcher.find())
                {
                    length = 70;
                }

                boolean counter = false;

                if (sharedPrefs.getBoolean("split_counter", false)) {
                    counter = true;
                    length -= 7;
                }

                String[] textToSend = com.klinker.android.messaging_sliding.MainActivity.splitByLength(body2, length, counter);

                for (int i = 0; i < textToSend.length; i++)
                {
                    ArrayList<String> parts = smsManager.divideMessage(textToSend[i]);

                    for (int j = 0; j < parts.size(); j++)
                    {
                        sPI.add(sentPI);
                    }

                    smsManager.sendMultipartTextMessage(number, null, parts, sPI, null);
                }
            } else
            {
                ArrayList<String> parts = smsManager.divideMessage(body2);

                for (int i = 0; i < parts.size(); i++)
                {
                    sPI.add(sentPI);
                }

                smsManager.sendMultipartTextMessage(number, null, parts, sPI, null);
            }
        }

        String address = number;
        String body2 = body;

        if (!sharedPrefs.getString("signature", "").equals(""))
        {
            body2 += "\n" + sharedPrefs.getString("signature", "");
        }

        Calendar cal = Calendar.getInstance();
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("body", StripAccents.stripAccents(body2));
        values.put("date", cal.getTimeInMillis() + "");
        values.put("read", 1);
        getContentResolver().insert(Uri.parse("content://sms/sent"), values);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead2.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", cal.getTimeInMillis() + "");
        mrIntent.putExtra("address", address);
        startService(mrIntent);

        Intent floatingNotifications = new Intent();
        floatingNotifications.setAction("robj.floating.notifications.dismiss");
        floatingNotifications.putExtra("package", getPackageName());
        sendBroadcast(floatingNotifications);

        com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
    }
}
