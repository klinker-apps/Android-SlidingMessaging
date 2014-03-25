package com.klinker.android.messaging_sliding.mms;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;

import com.android.mms.MmsConfig;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.AcknowledgeInd;
import com.google.android.mms.pdu_alt.EncodedStringValue;
import com.google.android.mms.pdu_alt.PduComposer;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.MessageUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.MessageCursorAdapter;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.receivers.TextMessageReceiver;
import com.klinker.android.send_message.DisconnectWifi;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MmsReceiverService extends Service {

    private SharedPreferences sharedPrefs;
    private Settings settings;
    private String phoneNumber;
    private String name;
    private Context context;

    private String downloadLocation, threadId, msgId;

    private ConnectivityManager mConnMgr;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        settings = SendUtil.getSendSettings(this);

        try {
            phoneNumber = intent.getStringExtra("address");
            name = intent.getStringExtra("name");
        } catch (Exception e) {
            phoneNumber = null;
            name = null;
        }

        try {
            getLocation();
        } catch (Exception e) {
            // no mms to download for whatever reason
            return 0;
        }

        Log.v("mms_download", "got mms location: " + downloadLocation + " and now starting download");

        if (settings.getWifiMmsFix()) {
            startDownload();
        } else {
            startDownloadWifi();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void getLocation() throws Exception {
        Cursor locationQuery = context.getContentResolver().query(Uri.parse("content://mms/"), new String[]{"ct_l", "thread_id", "_id"}, null, null, "date desc");

        if (locationQuery != null && locationQuery.moveToFirst()) {
            do {
                downloadLocation = locationQuery.getString(locationQuery.getColumnIndex("ct_l"));
                threadId = locationQuery.getString(locationQuery.getColumnIndex("thread_id"));
                msgId = locationQuery.getString(locationQuery.getColumnIndex("_id"));
                if(!locationQuery.moveToNext())
                    break;
            } while (downloadLocation == null);
        } else {
            throw new Exception("No MMS to download");
        }

        if (downloadLocation == null) {
            throw new Exception("found mms message, but download location is null...");
        }

        locationQuery.close();
    }

    private boolean alreadyReceiving = false;

    private void startDownload() {
        revokeWifi(true);

        // enable mms connection to mobile data
        mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int result = beginMmsConnectivity();//mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

        if (result != 0) {
            // if mms feature is not already running (most likely isn't...) then register a receiver and wait for it to be active
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            final BroadcastReceiver receiver = new BroadcastReceiver() {

                @Override
                public void onReceive(Context context1, Intent intent) {
                    String action = intent.getAction();

                    if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        return;
                    }

                    @SuppressWarnings("deprecation")
                    NetworkInfo mNetworkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                    if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE)) {
                        return;
                    }

                    if (!mNetworkInfo.isConnected()) {
                        return;
                    } else {
                        // ready to send the message now
                        alreadyReceiving = true;
                        receiveData();

                        context.unregisterReceiver(this);
                    }

                }

            };

            context.registerReceiver(receiver, filter);

            try {
                Looper.prepare();
            } catch (Exception e) {
                // Already on UI thread probably
            }

            // try sending after 3 seconds anyways if for some reason the receiver doesn't work
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!alreadyReceiving) {
                        try {
                            context.unregisterReceiver(receiver);
                        } catch (Exception e) {

                        }

                        receiveData();
                    }
                }
            }, 7000);
        } else {
            // mms connection already active, so send the message
            receiveData();
        }
    }

    private void startDownloadWifi() {
        mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State state = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getState();

        if ((0 == state.compareTo(NetworkInfo.State.CONNECTED) || 0 == state.compareTo(NetworkInfo.State.CONNECTING))) {
            Log.v("mms_download", "state already ready, receiving");
            receiveData();
        } else {
            int resultInt = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

            if (resultInt == 0) {
                try {
                    Utils.ensureRouteToHost(context, settings.getMmsc(), settings.getProxy());
                    receiveData();
                } catch (Exception e) {
                    e.printStackTrace();
                    receiveData();
                }
            } else {
                // if mms feature is not already running (most likely isn't...) then register a receiver and wait for it to be active
                IntentFilter filter = new IntentFilter();
                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                final BroadcastReceiver receiver = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context1, Intent intent) {
                        String action = intent.getAction();

                        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                            return;
                        }

                        NetworkInfo mNetworkInfo = mConnMgr.getActiveNetworkInfo();
                        if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE_MMS)) {
                            return;
                        }

                        if (!mNetworkInfo.isConnected()) {
                            return;
                        } else {
                            alreadyReceiving = true;

                            try {
                                Utils.ensureRouteToHost(context, settings.getMmsc(), settings.getProxy());
                                receiveData();
                            } catch (Exception e) {
                                e.printStackTrace();
                                receiveData();
                            }

                            context.unregisterReceiver(this);
                        }

                    }

                };

                context.registerReceiver(receiver, filter);

                try {
                    Looper.prepare();
                } catch (Exception e) {
                    // Already on UI thread probably
                }

                // try sending after 3 seconds anyways if for some reason the receiver doesn't work
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!alreadyReceiving) {
                            try {
                                context.unregisterReceiver(receiver);
                            } catch (Exception e) {

                            }

                            try {
                                Utils.ensureRouteToHost(context, settings.getMmsc(), settings.getProxy());
                                receiveData();
                            } catch (Exception e) {
                                e.printStackTrace();
                                receiveData();
                            }
                        }
                    }
                }, 7000);
            }
        }
    }

    private void receiveData() {
        // be sure this is running on new thread, not UI
        new Thread(new Runnable() {

            @Override
            public void run() {
                List<APN> apns = new ArrayList<APN>();

                try {
                    APN apn = new APN(settings.getMmsc(), settings.getPort(), settings.getProxy());
                    apns.add(apn);

                    String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
                    apns.get(0).MMSCenterUrl = mmscUrl;

                    if (apns.get(0).MMSCenterUrl.equals("")) {
                        // attempt to get apns from internal databases, most likely will fail due to insignificant permissions
                        APNHelper helper = new APNHelper(context);
                        apns = helper.getMMSApns();
                    }
                } catch (Exception e) {
                    // error in the apns, none are available most likely causing an index out of bounds
                    // exception. cant send a message, so therefore mark as failed.
                    return;
                }

                try {
                    tryDownloading(apns.get(0), downloadLocation, 0, threadId, msgId);
                } catch (Exception e) {

                }

            }

        }).start();
    }

    public void tryDownloading(APN apns, String downloadLocation, int retryNumber, String threadId, String msgId) {
        try {
            Log.v("attempting_mms_download", "number: " + retryNumber);
            Utils.ensureRouteToHost(context, apns.MMSCenterUrl, apns.MMSProxy);
            byte[] resp = HttpUtils.httpConnection(
                    context, 4444L,
                    downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                    !TextUtils.isEmpty(apns.MMSProxy),
                    apns.MMSProxy,
                    Integer.parseInt(apns.MMSPort));

            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
            if (!isDuplicateMessage(this, retrieveConf)) {
                PduPersister persister = PduPersister.getPduPersister(context);
                Uri msgUri = persister.persist(retrieveConf, Telephony.Mms.Inbox.CONTENT_URI, true,
                        true, null);
                ContentValues values = new ContentValues(1);
                values.put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000L);
                SqliteWrapper.update(context, context.getContentResolver(),
                        msgUri, values, null, null);
                SqliteWrapper.delete(context, context.getContentResolver(),
                        Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[]{threadId, msgId});

                findImageAndNotify(context, phoneNumber);

                try {
                    sendAcknowledgeInd(retrieveConf, apns);
                } catch (Exception e) {
                    // if this fails, then dont fail the whole message and retry
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            if (retryNumber < Transaction.NUM_RETRIES) {
                try {
                    Thread.sleep(3000);
                } catch (Exception f) {

                }

                tryDownloading(apns, downloadLocation, retryNumber + 1, threadId, msgId);
            } else {
                Log.v("attempting_mms_download", "failed");

                if (phoneNumber != null) {
                    if (sharedPrefs.getBoolean("secure_notification", false)) {
                        makeNotification("New Picture Message", "", null, phoneNumber, "", Calendar.getInstance().getTimeInMillis() + "", context);
                    } else {
                        makeNotification("New Picture Message", phoneNumber, null, phoneNumber, "", Calendar.getInstance().getTimeInMillis() + "", context);
                    }
                }

                sendBroadcast(new Intent("com.klinker.android.messaging.SHOW_DOWNLOAD_BUTTON"));

                if (settings.getWifiMmsFix()) {
                    reinstateWifi();
                }
            }
        }
    }

    public static void findImageAndNotify(Context context, String phoneNumber) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Cursor query = context.getContentResolver().query(Uri.parse("content://mms/"), new String[]{"_id"}, null, null, null);
        query.moveToFirst();

        String selectionPart = "mid=" + query.getString(query.getColumnIndex("_id"));
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null, selectionPart, null, null);

        if (phoneNumber == null) {
            phoneNumber = MessageCursorAdapter.getFrom(Uri.parse("content://mms/" + query.getString(query.getColumnIndex("_id"))), context);
        }

        String body = "";
        String image = "";

        if (cursor.moveToFirst()) {
            do {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                String body2 = "";
                if ("text/plain".equals(type)) {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));
                    if (data != null) {
                        body2 = MessageCursorAdapter.getMmsText(partId, (Activity) context);
                        body += body2;
                    } else {
                        body2 = cursor.getString(cursor.getColumnIndex("text"));
                        body += body2;

                    }
                }

                if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                        "image/gif".equals(type) || "image/jpg".equals(type) ||
                        "image/png".equals(type)) {
                    if (image == null) {
                        image = "content://mms/part/" + partId;
                    } else {
                        image += " content://mms/part/" + partId;
                    }
                }
            } while (cursor.moveToNext());
        }

        String images[] = image.trim().split(" ");

        if (phoneNumber != null) {
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("secure_notification", false)) {
                makeNotification("New MMS Message", "", null, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
            } else {
                String name = ContactUtil.findContactName(phoneNumber, context);
                if (images[0].trim().equals("")) {
                    makeNotification(name, body, null, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
                } else {
                    Bitmap b = null;
                    try {
                        for (int i = 0; i < images.length; i++) {
                            b = SendUtil.getImage(context, Uri.parse(images[i]), 400);

                            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("auto_save_mms", false)) {
                                String imageName = UUID.randomUUID().toString();
                                IOUtil.saveImage(b, imageName, context);
                            }
                        }
                    } catch (Exception e) {
                        b = null;
                    }

                    makeNotification(name, body, b, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
                }
            }
        }

        removeOldThread(context);
    }

    public static void makeNotification(String title, String text, Bitmap image, String address, String body, String date, final Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPrefs.getBoolean("notifications", true)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.stat_notify_mms)
                            .setContentTitle(title)
                            .setContentText(text);

            String notIcon = sharedPrefs.getString("notification_icon", "white");
            if (notIcon.equals("white")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms);
            } else if (notIcon.equals("blue")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms_blue);
            } else if (notIcon.equals("green")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms_green);
            } else if (notIcon.equals("orange")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms_orange);
            } else if (notIcon.equals("purple")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms_purple);
            } else if (notIcon.equals("red")) {
                mBuilder.setSmallIcon(R.drawable.stat_notify_mms_red);
            }

            if (!sharedPrefs.getBoolean("secure_notification", false)) {
                try {
                    int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics());
                    mBuilder.setLargeIcon(Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(address, context), scale, scale, true));
                } catch (Exception e) {
                }
            }

            //TextMessageReceiver.setIcon(mBuilder, context);

            Intent resultIntent = new Intent(context, MainActivity.class);
            resultIntent.putExtra("com.klinker.android.OPEN", address);
            resultIntent.setAction(Intent.ACTION_SENDTO);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);

            mBuilder.setAutoCancel(true);

            AppSettings settings = AppSettings.init(context);
            if (settings.vibrate == AppSettings.VIBRATE_ALWAYS) {
                if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false)) {
                    String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");

                    if (vibPat.equals("short")) {
                        long[] pattern = {0L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("long")) {
                        long[] pattern = {0L, 800L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("2short")) {
                        long[] pattern = {0L, 400L, 100L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("2long")) {
                        long[] pattern = {0L, 800L, 200L, 800L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("3short")) {
                        long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("3long")) {
                        long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
                        mBuilder.setVibrate(pattern);
                    }
                } else {
                    try {
                        String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
                        long[] pattern = new long[vibPat.length];

                        for (int i = 0; i < vibPat.length; i++) {
                            pattern[i] = Long.parseLong(vibPat[i]);
                        }

                        mBuilder.setVibrate(pattern);
                    } catch (Exception e) {
                    }
                }
            } else if (settings.vibrate == AppSettings.VIBRATE_NEVER) {
                mBuilder.setVibrate(new long[] {0});
            }

            if (sharedPrefs.getBoolean("led", true)) {
                String ledColor = sharedPrefs.getString("led_color", "white");
                int ledOn = sharedPrefs.getInt("led_on_time", 1000);
                int ledOff = sharedPrefs.getInt("led_off_time", 2000);

                if (ledColor.equalsIgnoreCase("white")) {
                    mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("blue")) {
                    mBuilder.setLights(0xFF33B5E5, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("green")) {
                    mBuilder.setLights(0xFF00FF00, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("orange")) {
                    mBuilder.setLights(0xFFFF8800, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("red")) {
                    mBuilder.setLights(0xFFCC0000, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("purple")) {
                    mBuilder.setLights(0xFFAA66CC, ledOn, ledOff);
                } else {
                    mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                }
            }

            try {
                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
            } catch (Exception e) {
                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
            }

            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                mBuilder.setTicker(null);
                mBuilder.setSmallIcon(android.R.color.transparent);
                mBuilder.setPriority(Notification.PRIORITY_LOW);

                try {
                    Looper.prepare();
                } catch (Exception e) {
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNotificationManager.cancel(2);
                    }
                }, 1000);
            }

            Notification notification;

            if (image != null && !sharedPrefs.getBoolean("secure_notification", false)) {
                NotificationCompat.BigPictureStyle picBuilder = new NotificationCompat.BigPictureStyle(mBuilder);

                try {
                    picBuilder.bigPicture(image);
                } catch (Exception e) {
                }

                notification = picBuilder.build();
            } else {
                notification = mBuilder.build();
            }

            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
            mNotificationManager.notify(2, notification);

            // Light Flow Broadcast
            Intent data = new Intent("com.klinker.android.messaging.NEW_NOTIFICATION");
            data.putExtra("message", body);
            data.putExtra("contact_name", title);
            data.putExtra("number", address);
            context.sendBroadcast(data);

            Intent updateWidget = new Intent("com.klinker.android.messaging.RECEIVED_MMS");
            context.sendBroadcast(updateWidget);

            // Pebble broadcast
            if(sharedPrefs.getBoolean("pebble", false)) {
                final Intent pebble = new Intent("com.getpebble.action.SEND_NOTIFICATION");

                final Map pebbleData = new HashMap();
                pebbleData.put("title", title);
                pebbleData.put("body", body);
                final JSONObject jsonData = new JSONObject(pebbleData);
                final String notificationData = new JSONArray().put(jsonData).toString();

                pebble.putExtra("messageType", "PEBBLE_ALERT");
                pebble.putExtra("sender", context.getResources().getString(R.string.app_name));
                pebble.putExtra("notificationData", notificationData);

                context.sendBroadcast(pebble);
            }

            final Intent newMms = new Intent("com.klinker.android.messaging.NEW_MMS");
            newMms.putExtra("address", address);
            newMms.putExtra("body", body);
            newMms.putExtra("date", date);

            try {
                Looper.prepare();
            } catch (Exception e) {
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    context.sendBroadcast(newMms);
                }
            }, 1000);

            if (!sharedPrefs.getString("repeating_notification", "none").equals("none")) {
                PreferenceManager.getDefaultSharedPreferences(context).edit().putInt("repeated_times", 0).commit();
                Calendar cal = Calendar.getInstance();

                Intent repeatingIntent = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pRepeatingIntent = PendingIntent.getService(context, 0, repeatingIntent, 0);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), pRepeatingIntent);
            }
        }
    }

    private static void removeOldThread(Context context) {
        try {
            String[] projection = new String[]{"_id", "message_count"};
            Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
            Cursor query = context.getContentResolver().query(uri, projection, null, null, "date desc");

            if (query.moveToFirst()) {
                do {
                    int msgCount = Integer.parseInt(query.getString(query.getColumnIndex("message_count")));
                    String id = query.getString(query.getColumnIndex("_id"));

                    if (msgCount == 0) {
                        try {
                            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
                            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[]{id});
                        } catch (Exception e) {

                        }
                    }
                } while (query.moveToNext());
            }

            query.close();
        } catch (Exception e) {
        }
    }

    private void reinstateWifi() {
        try {
            context.unregisterReceiver(settings.discon);
        } catch (Exception f) {

        }

        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifi.setWifiEnabled(false);
        wifi.setWifiEnabled(settings.currentWifiState);
        wifi.reconnect();
        Utils.setMobileDataEnabled(context, settings.currentDataState);
    }

    private void revokeWifi(boolean saveState) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (saveState) {
            settings.currentWifi = wifi.getConnectionInfo();
            settings.currentWifiState = wifi.isWifiEnabled();
            wifi.disconnect();
            settings.discon = new DisconnectWifi();
            context.registerReceiver(settings.discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
            settings.currentDataState = Utils.isMobileDataEnabled(context);
            Utils.setMobileDataEnabled(context, true);
        } else {
            wifi.disconnect();
            wifi.disconnect();
            settings.discon = new DisconnectWifi();
            context.registerReceiver(settings.discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
            Utils.setMobileDataEnabled(context, true);
        }
    }

    private int beginMmsConnectivity() {
        Log.v("sending_mms_library", "starting mms service");
        return mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");
    }

    private void sendAcknowledgeInd(RetrieveConf rc, APN apn) throws MmsException, IOException {
        // Send M-Acknowledge.ind to MMSC if required.
        // If the Transaction-ID isn't set in the M-Retrieve.conf, it means
        // the MMS proxy-relay doesn't require an ACK.
        byte[] tranId = rc.getTransactionId();
        Log.v("receiving_mms", "checking if acknowledgment is needed");
        if (tranId != null) {
            Log.v("receiving_mms", "sending acknowledgment to mmsc that the message was downloaded");
            // Create M-Acknowledge.ind
            AcknowledgeInd acknowledgeInd = new AcknowledgeInd(
                    PduHeaders.CURRENT_MMS_VERSION, tranId);

            // insert the 'from' address per spec
            String lineNumber = Utils.getMyPhoneNumber(this);
            acknowledgeInd.setFrom(new EncodedStringValue(lineNumber));

            // Pack M-Acknowledge.ind and send it
            if(MmsConfig.getNotifyWapMMSC()) {
                Utils.ensureRouteToHost(context, downloadLocation, apn.MMSProxy);
                HttpUtils.httpConnection(
                        this, 4444L,
                        downloadLocation,
                        new PduComposer(this, acknowledgeInd).make(), HttpUtils.HTTP_POST_METHOD,
                        !TextUtils.isEmpty(apn.MMSProxy), apn.MMSProxy, Integer.parseInt(apn.MMSPort));
            } else {
                Utils.ensureRouteToHost(context, apn.MMSProxy, apn.MMSProxy);
                HttpUtils.httpConnection(
                        this, 4444L,
                        apn.MMSCenterUrl,
                        new PduComposer(this, acknowledgeInd).make(), HttpUtils.HTTP_POST_METHOD,
                        !TextUtils.isEmpty(apn.MMSProxy), apn.MMSProxy, Integer.parseInt(apn.MMSPort));
            }
        }
    }

    private static boolean isDuplicateMessage(Context context, RetrieveConf rc) {
        byte[] rawMessageId = rc.getMessageId();
        if (rawMessageId != null) {
            String messageId = new String(rawMessageId);
            String selection = "(" + Telephony.Mms.MESSAGE_ID + " = ? AND "
                    + Telephony.Mms.MESSAGE_TYPE + " = ?)";
            String[] selectionArgs = new String[] { messageId,
                    String.valueOf(PduHeaders.MESSAGE_TYPE_RETRIEVE_CONF) };

            Cursor cursor = SqliteWrapper.query(
                    context, context.getContentResolver(),
                    Telephony.Mms.CONTENT_URI, new String[] { Telephony.Mms._ID, Telephony.Mms.SUBJECT, Telephony.Mms.SUBJECT_CHARSET },
                    selection, selectionArgs, null);

            if (cursor != null) {
                try {
                    if (cursor.getCount() > 0) {
                        // A message with identical message ID and type found.
                        // Do some additional checks to be sure it's a duplicate.
                        return isDuplicateMessageExtra(cursor, rc);
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return false;
    }

    private static boolean isDuplicateMessageExtra(Cursor cursor, RetrieveConf rc) {
        EncodedStringValue encodedSubjectReceived = null;
        EncodedStringValue encodedSubjectStored = null;
        String subjectReceived = null;
        String subjectStored = null;
        String subject = null;

        encodedSubjectReceived = rc.getSubject();
        if (encodedSubjectReceived != null) {
            subjectReceived = encodedSubjectReceived.getString();
        }

        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            int subjectIdx = cursor.getColumnIndex(Telephony.Mms.SUBJECT);
            int charsetIdx = cursor.getColumnIndex(Telephony.Mms.SUBJECT_CHARSET);
            subject = cursor.getString(subjectIdx);
            int charset = cursor.getInt(charsetIdx);
            if (subject != null) {
                encodedSubjectStored = new EncodedStringValue(charset, PduPersister
                        .getBytes(subject));
            }
            if (encodedSubjectStored == null && encodedSubjectReceived == null) {
                // Both encoded subjects are null - return true
                return true;
            } else if (encodedSubjectStored != null && encodedSubjectReceived != null) {
                subjectStored = encodedSubjectStored.getString();
                if (!TextUtils.isEmpty(subjectStored) && !TextUtils.isEmpty(subjectReceived)) {
                    // Both decoded subjects are non-empty - compare them
                    return subjectStored.equals(subjectReceived);
                } else if (TextUtils.isEmpty(subjectStored) && TextUtils.isEmpty(subjectReceived)) {
                    // Both decoded subjects are "" - return true
                    return true;
                }
            }
        }

        return false;
    }
}
