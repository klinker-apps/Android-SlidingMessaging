package com.klinker.android.messaging_sliding.receivers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.*;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsMessage;
import android.util.Log;
import android.util.TypedValue;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.Util;
import com.klinker.android.messaging_sliding.MainActivityPopup;
import com.klinker.android.messaging_sliding.blacklist.BlacklistContact;
import com.klinker.android.messaging_sliding.notifications.IndividualSetting;
import com.klinker.android.messaging_sliding.quick_reply.CardQuickReply;
import com.klinker.android.messaging_sliding.quick_reply.QmDelete;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.messaging_sliding.quick_reply.QuickReply;

import java.io.InputStream;
import java.util.*;

@SuppressWarnings("deprecation")
public class TextMessageReceiver extends BroadcastReceiver {
    public static final String SMS_EXTRA_NAME = "pdus";
    public SharedPreferences sharedPrefs;

    private boolean alert = true;

    @SuppressLint("Wakelock")
    public void onReceive(final Context context, Intent intent) {
        try {
            Bundle extras = intent.getExtras();

            String body = "";
            String address = "";
            String name = "";
            String id;
            String date = "";
            String dateReceived;

            boolean voiceMessage = intent.getBooleanExtra("voice_message", false);
            boolean fromSmsBroadcast = intent.getBooleanExtra("sms_broadcast", false);

            Log.v("refresh_voice", "sms receiver " + voiceMessage);
            Log.v("sms_notification", "just started");

            // gets the message details depending on voice or sms
            if (!voiceMessage || (voiceMessage && fromSmsBroadcast)) {
                if (extras != null) {
                    Object[] smsExtra = (Object[]) extras.get(SMS_EXTRA_NAME);

                    for (int i = 0; i < smsExtra.length; ++i) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

                        body += sms.getMessageBody().toString();
                        address = sms.getOriginatingAddress();
                        date = sms.getTimestampMillis() + "";
                    }
                } else {
                    return;
                }
            } else {
                body = intent.getStringExtra("voice_body");
                address = intent.getStringExtra("voice_address");
                date = intent.getLongExtra("voice_date", Calendar.getInstance().getTimeInMillis()) + "";
            }

            Log.v("sms_notification", "got details");

            // gets actual date received
            Calendar cal = Calendar.getInstance();
            dateReceived = cal.getTimeInMillis() + "";

            final String origBody = body;
            final String origDate = dateReceived;
            final String origAddress = address;

            // floating notifications
            Intent fnReceiver = new Intent("com.klinker.android.messaging_donate.FNRECEIVED");
            fnReceiver.putExtra("address", origAddress);
            fnReceiver.putExtra("body", origBody);
            context.sendBroadcast(fnReceiver);
            Log.v("sms_notification", "after floating notification");

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            if (!sharedPrefs.getBoolean("override", false)) {
                Util.checkOverride(context);
            }

            // dont alert during a call if not desired
            if (!sharedPrefs.getBoolean("alert_in_call", true) && isCallActive(context)) {
                alert = false;
            }

            // checks against the saved blacklist list
            ArrayList<BlacklistContact> blacklist = IOUtil.readBlacklist(context);
            int blacklistType = 0;

            for (int i = 0; i < blacklist.size(); i++) {
                if (blacklist.get(i).name.equals(address.replace("-", "").replace("(", "").replace(")", "").replace(" ", "").replace("+1", ""))) {
                    blacklistType = blacklist.get(i).type;
                }
            }

            final ArrayList<String> prevNotifications = IOUtil.readNotifications(context);

            if (blacklistType == 2) {
                // don't do anything with message if severely blacklisted
                abortBroadcast();
            } else {
                // if overriding stock or its a voice message, save the messages
                if (sharedPrefs.getBoolean("override", false) || voiceMessage || Build.VERSION.SDK_INT > 18) {
                    ContentValues values = new ContentValues();
                    values.put("address", address);
                    values.put("body", body);
                    values.put("date", dateReceived);
                    values.put("read", "0");
                    values.put("date_sent", date);

                    if (voiceMessage) {
                        values.put("status", 2);
                    }

                    context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
                }

                // if notification is to be given for the message because it is not blacklisted
                if (blacklistType != 1) {
                    Bundle bundle = new Bundle();
                    bundle.putString("body", body);
                    bundle.putString("date", date);
                    bundle.putString("address", address);

                    String origin = address.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");

                    // find the name and id of the contact
                    try {
                        name = ContactUtil.findContactName(origin, context);

                        ArrayList<String> newMessages = IOUtil.readNewMessages(context);
                        boolean flag = false;
                        for (int i = 0; i < newMessages.size(); i++) {
                            if (name.equals(newMessages.get(i))) {
                                flag = true;
                            }
                        }
                        if (!flag) {
                            newMessages.add(name);
                        }

                        IOUtil.writeNewMessages(newMessages, context);

                        Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
                        Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts._ID}, null, null, null);

                        if (phonesCursor != null && phonesCursor.moveToFirst()) {
                            id = phonesCursor.getString(0);
                        } else {
                            id = "0";
                        }

                        phonesCursor.close();
                    } catch (IllegalArgumentException e) {
                        name = address;
                        id = "0";
                    }

                    // get the display picture for the contact and size it according to how high the notification is, 64dp
                    InputStream input = ContactUtil.openDisplayPhoto(Long.parseLong(id), context);
                    Bitmap contactImage;
                    try {
                        int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics());
                        contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), scale, scale, true);
                    } catch (Exception e) {
                        contactImage = null;
                    }

                    // begin setting up the reply button pending intent
                    Intent intent2 = new Intent(context, CardQuickReply.class);
                    intent2.putExtra("address", origAddress);
                    intent2.putExtra("body", origBody);
                    intent2.putExtra("date", origDate);
                    int pIntentExtra = PendingIntent.FLAG_UPDATE_CURRENT;

                    // if you should use the oldest popup
                    if (sharedPrefs.getBoolean("use_old_popup", false)) {
                        intent2 = new Intent(context, QuickReply.class);
                        pIntentExtra = 0;
                    }

                    // if you are using slideover
                    if (sharedPrefs.getBoolean("full_app_popup", true)) {
                        intent2 = new Intent(context, MainActivityPopup.class);
                        intent2.putExtra("fromWidget", false);
                        intent2.putExtra("fromNotification", true);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pIntentExtra = 0;
                    }

                    // if you should use halo
                    if (sharedPrefs.getBoolean("halo_popup", false)) {
                        intent2 = new Intent(context, MainActivity.class);
                        pIntentExtra = 0;

                        try {
                            // set the flags to open in halo
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                            intent2.putExtra("halo_popup", true);
                        } catch (Exception e) {
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                    }

                    // create the pending intent
                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent2.putExtra("notification", "true");
                    PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent2, pIntentExtra);

                    // if notifiations for the app are enabled
                    if (sharedPrefs.getBoolean("notifications", true)) {
                        // if we want to wake the screen on a new message
                        if (sharedPrefs.getBoolean("wake_screen", false)) {
                            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                            final WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
                            wakeLock.acquire(Long.parseLong(sharedPrefs.getString("screen_timeout", "5")) * 1000);
                        }

                        // pending intent for calling the user
                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                        callIntent.setData(Uri.parse("tel:" + address));
                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        PendingIntent callPendingIntent = PendingIntent.getActivity(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                        // pending intent for marking all messages as read
                        Intent mrIntent = new Intent();
                        mrIntent.setClass(context, QmMarkRead.class);
                        PendingIntent mrPendingIntent = PendingIntent.getService(context, 0, mrIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        // pending intent for deleting the specified message
                        Intent deleterIntent = new Intent();
                        deleterIntent.setClass(context, QmDelete.class);
                        PendingIntent deletePendingIntent = PendingIntent.getService(context, 0, deleterIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                        // get the buttons that the user has enabled
                        HashSet<String> set = new HashSet<String>();
                        set.add("1");
                        set.add("2");
                        set.add("3");
                        set.add("4");
                        Set<String> buttons = sharedPrefs.getStringSet("button_options", set);
                        int[] buttonArray = new int[buttons.size()];
                        for (int i = 0; i < buttons.size(); i++) {
                            buttonArray[i] = Integer.parseInt((String) buttons.toArray()[i]);
                        }
                        Arrays.sort(buttonArray);

                        if (!sharedPrefs.getBoolean("secure_notification", false)) {
                            if (prevNotifications.size() == 0) {
                                // single new notification
                                makeNotification(name, body, name + ": " + body, origAddress, body, dateReceived, contactImage, buttonArray, pIntent, mrPendingIntent, callPendingIntent, deletePendingIntent, 1, prevNotifications, alert, context);
                            } else if (prevNotifications.size() == 1 && prevNotifications.get(0).startsWith(name)) {
                                // add onto previous notification because it is the same person again and no others
                                String body2 = prevNotifications.get(0);

                                for (int i = 0; i < body2.length() - 1; i++) {
                                    if (body2.substring(i, i + 1).equals(":")) {
                                        body2 = body2.substring(i + 1);
                                        break;
                                    }
                                }

                                if (sharedPrefs.getBoolean("stack_notifications", true)) {
                                    body = body2 + " | " + body;
                                }

                                makeNotification(name, body, name + ": " + body, origAddress, body, dateReceived, contactImage, buttonArray, pIntent, mrPendingIntent, callPendingIntent, deletePendingIntent, 2, prevNotifications, alert, context);
                            } else {
                                makeNotification(prevNotifications.size() + 1 + " New Messages", body, prevNotifications.size() + 1 + " New Messages", origAddress, body, dateReceived, null, buttonArray, pIntent, mrPendingIntent, callPendingIntent, deletePendingIntent, 3, prevNotifications, alert, context);
                            }
                        } else {
                            if (prevNotifications.size() == 0) {
                                makeNotification("New Message", "", "New Message", origAddress, "New Message", dateReceived, contactImage, buttonArray, pIntent, mrPendingIntent, callPendingIntent, deletePendingIntent, 1, prevNotifications, alert, context);
                            } else {
                                makeNotification("New Messages", "", "New Messages", origAddress, "New Messages", dateReceived, contactImage, buttonArray, pIntent, mrPendingIntent, callPendingIntent, deletePendingIntent, 1, prevNotifications, alert, context);
                            }
                        }
                    }
                }

                MainActivity.messageRecieved = true;

                if (!Util.isRunning(context)) {
                    Intent updateHalo = new Intent("com.klinker.android.messaging.UPDATE_HALO");
                    updateHalo.putExtra("name", name);
                    updateHalo.putExtra("message", body);
                    updateHalo.putExtra("number", address);
                    context.sendBroadcast(updateHalo);
                }

                if (!Util.isRunning(context) && blacklistType != 1) {
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {

                        @Override
                        public void run() {
                            if (sharedPrefs.getBoolean("popup_reply", false) && !sharedPrefs.getBoolean("secure_notification", false)) {
                                Intent intent3 = new Intent(context, CardQuickReply.class);
                                intent3.putExtra("address", origAddress);
                                intent3.putExtra("body", origBody);
                                intent3.putExtra("date", origDate);

                                if (sharedPrefs.getBoolean("use_old_popup", false)) {
                                    intent3 = new Intent(context, QuickReply.class);
                                }

                                if (sharedPrefs.getBoolean("halo_popup", false) || sharedPrefs.getBoolean("full_app_popup", true)) {
                                    boolean halo = sharedPrefs.getBoolean("halo_popup", false);

                                    if (halo) {
                                        intent3 = new Intent(context, MainActivity.class);
                                    } else {
                                        intent3 = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);

                                        if (prevNotifications.size() > 0) {
                                            intent3.putExtra("multipleNew", true);
                                        }
                                    }

                                    try {
                                        if (halo) {
                                            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                                            intent3.putExtra("halo_popup", true);
                                        } else {
                                            intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        }
                                    } catch (Exception e) {
                                        intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    }

                                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                                    if (pm.isScreenOn() || sharedPrefs.getBoolean("unlock_screen", false)) {
                                        if (!sharedPrefs.getBoolean("full_app_popup", true) || (sharedPrefs.getBoolean("full_app_popup", true) && !sharedPrefs.getBoolean("slideover_popup_lockscreen_only", false)) || (sharedPrefs.getBoolean("unlock_screen", false) && !sharedPrefs.getBoolean("full_app_popup", true))) {
                                            final Intent popup = intent3;

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    context.startActivity(popup);
                                                }
                                            }, 250);
                                        }
                                    } else {
                                        UnlockReceiver.openApp = true;
                                    }
                                } else {
                                    intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent3);
                                }
                            }

                        }

                    }, 200);
                }

                if (voiceMessage) {
                    Intent voice = new Intent("com.klinker.android.messaging.NEW_MMS");
                    voice.putExtra("address", address);
                    voice.putExtra("body", body);
                    voice.putExtra("date", date);
                    context.sendBroadcast(voice);
                }

                if (sharedPrefs.getBoolean("override", false) && Build.VERSION.SDK_INT <= 18) {
                    try {
                        this.abortBroadcast();
                    } catch (Exception e) {
                    }
                }
            }
        } catch (Exception e) {
        }
    }

    public static void setIcon(NotificationCompat.Builder mBuilder, Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (!sharedPrefs.getBoolean("breath", false)) {
            String notIcon = sharedPrefs.getString("notification_icon", "white");
            int notImage = Integer.parseInt(sharedPrefs.getString("notification_image", "1"));

            switch (notImage) {
                case 1:
                    if (notIcon.equals("white")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms);
                    } else if (notIcon.equals("blue")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_blue);
                    } else if (notIcon.equals("green")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_green);
                    } else if (notIcon.equals("orange")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_orange);
                    } else if (notIcon.equals("purple")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_purple);
                    } else if (notIcon.equals("red")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_red);
                    } else if (notIcon.equals("icon")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 2:
                    if (notIcon.equals("white")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble);
                    } else if (notIcon.equals("blue")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_blue);
                    } else if (notIcon.equals("green")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_green);
                    } else if (notIcon.equals("orange")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_orange);
                    } else if (notIcon.equals("purple")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_purple);
                    } else if (notIcon.equals("red")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_red);
                    } else if (notIcon.equals("icon")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 3:
                    if (notIcon.equals("white")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point);
                    } else if (notIcon.equals("blue")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_blue);
                    } else if (notIcon.equals("green")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_green);
                    } else if (notIcon.equals("orange")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_orange);
                    } else if (notIcon.equals("purple")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_purple);
                    } else if (notIcon.equals("red")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_red);
                    } else if (notIcon.equals("icon")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 4:
                    if (notIcon.equals("white")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane);
                    } else if (notIcon.equals("blue")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_blue);
                    } else if (notIcon.equals("green")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_green);
                    } else if (notIcon.equals("orange")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_orange);
                    } else if (notIcon.equals("purple")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_purple);
                    } else if (notIcon.equals("red")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_red);
                    } else if (notIcon.equals("icon")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 5:
                    if (notIcon.equals("white")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud);
                    } else if (notIcon.equals("blue")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_blue);
                    } else if (notIcon.equals("green")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_green);
                    } else if (notIcon.equals("orange")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_orange);
                    } else if (notIcon.equals("purple")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_purple);
                    } else if (notIcon.equals("red")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_red);
                    } else if (notIcon.equals("icon")) {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }
                    break;
            }
        } else {
            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_breath);
        }
    }

    public static boolean individualNotification(NotificationCompat.Builder mBuilder, String name, Context context, boolean alert) {
        ArrayList<IndividualSetting> individuals = IOUtil.readIndividualNotifications(context);

        for (int i = 0; i < individuals.size(); i++) {
            if (individuals.get(i).name.equals(name)) {
                if (alert)
                    mBuilder.setSound(Uri.parse(individuals.get(i).ringtone));

                try {
                    String[] vibPat = individuals.get(i).vibratePattern.replace("L", "").split(", ");
                    long[] pattern = new long[vibPat.length];

                    for (int j = 0; j < vibPat.length; j++) {
                        pattern[j] = Long.parseLong(vibPat[j]);
                    }

                    if (alert)
                        mBuilder.setVibrate(pattern);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mBuilder.setLights(individuals.get(i).color, PreferenceManager.getDefaultSharedPreferences(context).getInt("led_on_time", 1000), PreferenceManager.getDefaultSharedPreferences(context).getInt("led_off_time", 2000));

                return true;
            }
        }

        return false;
    }

    private void makeNotification(String title, String text, String ticker, String address, String body, String date, Bitmap contactImage,
                                  int[] buttonArray, PendingIntent pIntent, PendingIntent mrPendingIntent, PendingIntent callPendingIntent,
                                  PendingIntent deletePendingIntent, int notificationType, ArrayList<String> prevNotifications, boolean alert, final Context context) {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (sharedPrefs.getBoolean("secure_notification", false)) {
            text = "New Message";
        }

        if (sharedPrefs.getBoolean("notifications", true)) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText(text);

            if (notificationType == 1 || notificationType == 2) {
                mBuilder.setTicker(ticker);
            }

            if (!sharedPrefs.getBoolean("secure_notification", false) && contactImage != null) {
                try {
                    mBuilder.setLargeIcon(contactImage);
                } catch (Exception e) {
                }
            }

            TextMessageReceiver.setIcon(mBuilder, context);

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

            if (!sharedPrefs.getBoolean("secure_notification", false)) {
                for (int i = 0; i < buttonArray.length; i++) {
                    String[] labels = context.getResources().getStringArray(R.array.quickReplyOptions);

                    int option = buttonArray[i];

                    if (option == 1) {
                        mBuilder.addAction(R.drawable.ic_menu_msg_compose_holo_dark, labels[0], pIntent);
                    } else if (option == 2) {
                        mBuilder.addAction(R.drawable.ic_menu_done_holo_dark, labels[1], mrPendingIntent);
                    } else if (option == 3 && notificationType != 3) {
                        mBuilder.addAction(R.drawable.ic_menu_call, labels[2], callPendingIntent);
                    } else if (option == 4 && notificationType == 1) {
                        mBuilder.addAction(R.drawable.ic_menu_delete, labels[3], deletePendingIntent);
                    }
                }
            }

            if (!individualNotification(mBuilder, ContactUtil.findContactName(address, context), context, alert) || sharedPrefs.getBoolean("secure_notification", false)) {
                if (sharedPrefs.getBoolean("vibrate", true) && alert) {
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

                if (alert) {
                    try {
                        mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                    } catch (Exception e) {
                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    }
                }
            }

            final NotificationManager mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            Notification notification;

            if (notificationType == 1 || notificationType == 2) {
                prevNotifications = new ArrayList<String>();
                prevNotifications.add(ContactUtil.findContactName(address, context) + ": " + body);

                notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
            } else {
                prevNotifications.add(ContactUtil.findContactName(address, context) + ": " + body);
                NotificationCompat.InboxStyle not = new NotificationCompat.InboxStyle(mBuilder);

                for (int i = 0; i < prevNotifications.size(); i++) {
                    not.addLine(prevNotifications.get(i));
                }

                not.setSummaryText(prevNotifications.size() + " New Messages");
                notification = not.build();
            }

            Log.v("sms_notification", "posted notification");

            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
            mNotificationManager.notify(1, notification);

            Log.v("sms_notification", "posted notification");

            Intent updateWidget = new Intent("com.klinker.android.messaging.RECEIVED_MMS");
            context.sendBroadcast(updateWidget);

            final Intent newMms = new Intent("com.klinker.android.messaging.NEW_MMS");
            newMms.putExtra("address", address);
            newMms.putExtra("body", body);
            newMms.putExtra("date", date);

            try {
                Looper.prepare();
            } catch (Exception e) {
            }

            if (sharedPrefs.getBoolean("override_stock", false)) {
                context.sendBroadcast(newMms);
            } else {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        context.sendBroadcast(newMms);
                    }
                }, 1000);
            }

            if (!sharedPrefs.getString("repeating_notification", "none").equals("none")) {
                Calendar cal = Calendar.getInstance();

                Intent repeatingIntent = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pRepeatingIntent = PendingIntent.getService(context, 0, repeatingIntent, 0);
                AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), pRepeatingIntent);
            }

            IOUtil.writeNotifications(prevNotifications, context);
        }
    }

    public static boolean isCallActive(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (manager.getMode() == AudioManager.MODE_IN_CALL) {
            return true;
        } else {
            return false;
        }
    }
}
