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
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.google.android.mms.pdu_alt.RetrieveConf;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.SendUtil;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.receivers.TextMessageReceiver;
import com.klinker.android.send_message.DisconnectWifi;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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
        Cursor locationQuery = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"ct_l", "thread_id", "_id"}, null, null, "date desc");

        if (locationQuery != null && locationQuery.moveToFirst()) {
            downloadLocation = locationQuery.getString(locationQuery.getColumnIndex("ct_l"));
            threadId = locationQuery.getString(locationQuery.getColumnIndex("thread_id"));
            msgId = locationQuery.getString(locationQuery.getColumnIndex("_id"));
        } else {
            throw new Exception("No MMS to download");
        }

        locationQuery.close();
    }

    private boolean alreadyReceiving = false;
    private void startDownload() {
        revokeWifi(true);

        // enable mms connection to mobile data
        mConnMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

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
            }, 3500);
        } else {
            // mms connection already active, so send the message
            receiveData();
        }
    }

    private void startDownloadWifi() {
        mConnMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo.State state = mConnMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS).getState();

        if ((0 == state.compareTo(NetworkInfo.State.CONNECTED) || 0 == state.compareTo(NetworkInfo.State.CONNECTING))) {
            Log.v("mms_download", "state already ready, receiving");
            receiveData();
        } else {
            int resultInt = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

            if (resultInt == 0) {
                Log.v("mms_download", "state already ready 2, receiving");
                receiveData();
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
                            Log.v("mms_download", "downloading through receiver");
                            alreadyReceiving = true;
                            Utils.forceMobileConnectionForAddress(mConnMgr, settings.getMmsc());
                            receiveData();

                            context.unregisterReceiver(this);
                        }

                    }

                };

                context.registerReceiver(receiver, filter);

                // try sending after 3 seconds anyways if for some reason the receiver doesn't work
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!alreadyReceiving) {
                            try {
                                context.unregisterReceiver(receiver);
                            } catch (Exception e) {

                            }

                            Log.v("mms_download", "receiving data through handler");
                            receiveData();
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

                APN apn = new APN(settings.getMmsc(), settings.getPort(), settings.getProxy());
                apns.add(apn);

                String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
                apns.get(0).MMSCenterUrl = mmscUrl;

                if (apns.get(0).MMSCenterUrl.equals("")) {
                    // attempt to get apns from internal databases, most likely will fail due to insignificant permissions
                    APNHelper helper = new APNHelper(context);
                    apns = helper.getMMSApns();
                }

                Log.v("mms_download", apns.get(0).MMSCenterUrl + " " + apns.get(0).MMSPort + " " + apns.get(0).MMSProxy);

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
                    context, SendingProgressTokenManager.NO_TOKEN,
                    downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                    !TextUtils.isEmpty(apns.MMSProxy),
                    apns.MMSProxy,
                    Integer.parseInt(apns.MMSPort));

            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
            PduPersister persister = PduPersister.getPduPersister(context);
            Uri msgUri = persister.persist(retrieveConf, Telephony.Mms.Inbox.CONTENT_URI, true,
                    true, null);
            ContentValues values = new ContentValues(1);
            values.put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000L);
            SqliteWrapper.update(context, context.getContentResolver(),
                    msgUri, values, null, null);
            SqliteWrapper.delete(context, context.getContentResolver(),
                    Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadId, msgId});

            findImageAndNotify();
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
                    if (sharedPrefs.getBoolean("secure_notification", false))
                    {
                        makeNotification("New Picture Message", "", null, phoneNumber, "", Calendar.getInstance().getTimeInMillis() + "", context);
                    } else
                    {
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

    private void findImageAndNotify() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Cursor query = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"_id"}, null, null, null);
        query.moveToFirst();

        String selectionPart = "mid=" + query.getString(query.getColumnIndex("_id"));
        Uri uri = Uri.parse("content://mms/part");
        Cursor cursor = context.getContentResolver().query(uri, null, selectionPart, null, null);

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
                        body2 = getMmsText(partId, (Activity) context);
                        body += body2;
                    } else {
                        body2 = cursor.getString(cursor.getColumnIndex("text"));
                        body += body2;

                    }
                }

                if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                        "image/gif".equals(type) || "image/jpg".equals(type) ||
                        "image/png".equals(type)) {
                    if (image == null)
                    {
                        image = "content://mms/part/" + partId;
                    } else
                    {
                        image += " content://mms/part/" + partId;
                    }
                }
            } while (cursor.moveToNext());
        }

        String images[] = image.trim().split(" ");

        if (phoneNumber != null) {
            if (sharedPrefs.getBoolean("secure_notification", false))
            {
                makeNotification("New MMS Message", "", null, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
            } else
            {
                if (images[0].trim().equals(""))
                {
                    makeNotification(phoneNumber, body, null, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
                } else
                {
                    Bitmap b = decodeFile(new File(getRealPathFromURI(Uri.parse(images[0].trim()))));
                    makeNotification(phoneNumber, body, b, phoneNumber, body, Calendar.getInstance().getTimeInMillis() + "", context);
                }
            }
        }

        removeOldThread();
    }

    public static void makeNotification(String title, String text, Bitmap image, String address, String body, String date, Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPrefs.getBoolean("notifications", true))
        {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setSmallIcon(R.drawable.stat_notify_mms)
                            .setContentTitle(title)
                            .setContentText(text);

            if (!sharedPrefs.getBoolean("secure_notification", false))
            {
                try
                {
                    mBuilder.setLargeIcon(MainActivity.getFacebookPhoto(address, context));
                } catch (Exception e)
                {

                }
            }

            TextMessageReceiver.setIcon(mBuilder, context);

            Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            mBuilder.setContentIntent(resultPendingIntent);

            mBuilder.setAutoCancel(true);

            if (sharedPrefs.getBoolean("vibrate", true))
            {
                if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
                {
                    String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");

                    if (vibPat.equals("short"))
                    {
                        long[] pattern = {0L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("long"))
                    {
                        long[] pattern = {0L, 800L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("2short"))
                    {
                        long[] pattern = {0L, 400L, 100L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("2long"))
                    {
                        long[] pattern = {0L, 800L, 200L, 800L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("3short"))
                    {
                        long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
                        mBuilder.setVibrate(pattern);
                    } else if (vibPat.equals("3long"))
                    {
                        long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
                        mBuilder.setVibrate(pattern);
                    }
                } else
                {
                    try
                    {
                        String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
                        long[] pattern = new long[vibPat.length];

                        for (int i = 0; i < vibPat.length; i++)
                        {
                            pattern[i] = Long.parseLong(vibPat[i]);
                        }

                        mBuilder.setVibrate(pattern);
                    } catch (Exception e)
                    {

                    }
                }
            }

            if (sharedPrefs.getBoolean("led", true))
            {
                String ledColor = sharedPrefs.getString("led_color", "white");
                int ledOn = sharedPrefs.getInt("led_on_time", 1000);
                int ledOff = sharedPrefs.getInt("led_off_time", 2000);

                if (ledColor.equalsIgnoreCase("white"))
                {
                    mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("blue"))
                {
                    mBuilder.setLights(0xFF33B5E5, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("green"))
                {
                    mBuilder.setLights(0xFF00FF00, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("orange"))
                {
                    mBuilder.setLights(0xFFFF8800, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("red"))
                {
                    mBuilder.setLights(0xFFCC0000, ledOn, ledOff);
                } else if (ledColor.equalsIgnoreCase("purple"))
                {
                    mBuilder.setLights(0xFFAA66CC, ledOn, ledOff);
                } else
                {
                    mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
                }
            }

            try
            {
                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
            } catch(Exception e)
            {
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

            if (image != null && !sharedPrefs.getBoolean("secure_notification", false))
            {
                NotificationCompat.BigPictureStyle picBuilder = new NotificationCompat.BigPictureStyle(mBuilder);

                try
                {
                    picBuilder.bigPicture(image);
                } catch (Exception e)
                {

                }

                notification = picBuilder.build();
            } else
            {
                notification = mBuilder.build();
            }

            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
            mNotificationManager.notify(2, notification);

            Intent updateWidget = new Intent("com.klinker.android.messaging.RECEIVED_MMS");
            context.sendBroadcast(updateWidget);

            Intent newMms = new Intent("com.klinker.android.messaging.NEW_MMS");
            newMms.putExtra("address", address);
            newMms.putExtra("body", body);
            newMms.putExtra("date", date);
            context.sendBroadcast(newMms);

            if (!sharedPrefs.getString("repeating_notification", "none").equals("none"))
            {
                Calendar cal = Calendar.getInstance();

                Intent repeatingIntent = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pRepeatingIntent = PendingIntent.getService(context, 0, repeatingIntent, 0);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), pRepeatingIntent);
            }

            if (sharedPrefs.getBoolean("cache_conversations", false)) {
                Intent cacheService = new Intent(context, CacheService.class);
                context.startService(cacheService);
            }
        }
    }

    private void removeOldThread() {
        try
        {
            String[] projection = new String[]{"_id", "message_count"};
            Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
            Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");

            if (query.moveToFirst()) {
                do {
                    int msgCount = Integer.parseInt(query.getString(query.getColumnIndex("message_count")));
                    String id = query.getString(query.getColumnIndex("_id"));

                    if (msgCount == 0) {
                        try {
                            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + id + "/"), null, null);
                            context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/"), "_id=?", new String[] {id});
                        } catch (Exception e) {

                        }
                    }
                } while (query.moveToNext());
            }

            query.close();
        } catch (Exception e)
        {

        }
    }

    private String getMmsText(String id, Activity context) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private Bitmap decodeFile(File f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=200;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {e.printStackTrace();}
        return null;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    // FIXME again with the wifi problems... should not have to do this at all
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

    // FIXME it should not be required to disable wifi and enable mobile data manually, but I have found no way to use the two at the same time
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
}
