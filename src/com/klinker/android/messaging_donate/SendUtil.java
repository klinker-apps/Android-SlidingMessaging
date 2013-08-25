package com.klinker.android.messaging_donate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.util.Calendar;

public class SendUtil {

    public static void sendMessage(Context context, String number, String body)
    {
        Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);

        sendTransaction.sendNewMessage(message, null);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead2.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
    }

    public static void sendMessage(Context context, String[] number, String body)
    {
        Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);

        sendTransaction.sendNewMessage(message, null);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead2.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
    }

    public static void sendMessage(Context context, String[] number, String body, Bitmap[] images)
    {
        Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);
        message.setImages(images);

        sendTransaction.sendNewMessage(message, null);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead2.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
    }

    public static Settings getSendSettings(Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Settings sendSettings = new Settings();

        sendSettings.setMmsc(sharedPrefs.getString("mmsc_url", ""));
        sendSettings.setProxy(sharedPrefs.getString("mms_proxy", ""));
        sendSettings.setPort(sharedPrefs.getString("mms_port", ""));
        sendSettings.setGroup(sharedPrefs.getBoolean("group_message", false));
        sendSettings.setWifiMmsFix(sharedPrefs.getBoolean("wifi_mms_fix", true));
        sendSettings.setPreferVoice(sharedPrefs.getBoolean("prefer_voice", false));
        sendSettings.setDeliveryReports(sharedPrefs.getBoolean("delivery_reports", false));
        sendSettings.setSplit(sharedPrefs.getBoolean("split_sms", false));
        sendSettings.setSplitCounter(sharedPrefs.getBoolean("split_counter", false));
        sendSettings.setStripUnicode(sharedPrefs.getBoolean("strip_unicode", false));
        sendSettings.setSignature(sharedPrefs.getString("signature", ""));
        sendSettings.setSendLongAsMms(sharedPrefs.getBoolean("send_as_mms", false));
        sendSettings.setSendLongAsMmsAfter(sharedPrefs.getInt("mms_after", 4));
        return sendSettings;
    }
}
