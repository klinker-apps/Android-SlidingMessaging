package com.klinker.android.messaging_sliding.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Telephony.Mms.Inbox;
import android.util.Log;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.GenericPdu;
import com.google.android.mms.pdu_alt.PduHeaders;
import com.google.android.mms.pdu_alt.PduParser;
import com.google.android.mms.pdu_alt.PduPersister;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.Util;
import com.klinker.android.messaging_sliding.mms.MmsReceiverService;

import java.util.Calendar;

public class MMSMessageReceiver extends BroadcastReceiver {
    public static String lastReceivedNumber = "";
    public static long lastReceivedTime = Calendar.getInstance().getTimeInMillis();

    public SharedPreferences sharedPrefs;
    public Context context;
    public String phoneNumber;
    public String picNumber;
    public String mmsFrom;

    public void onReceive(final Context context, Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;
        String incomingNumber = null;

        if (intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            try {
                if (bundle != null) {
                    String type = intent.getType();
                    if (type.trim().equalsIgnoreCase("application/vnd.wap.mms-message")) {
                        byte[] buffer = bundle.getByteArray("data");
                        incomingNumber = new String(buffer);
                        int indx = incomingNumber.indexOf("/TYPE");
                        if (indx > 0 && (indx - 15) > 0) {
                            int newIndx = indx - 15;
                            incomingNumber = incomingNumber.substring(newIndx, indx);
                            indx = incomingNumber.indexOf("+");
                            if (indx > 0) {
                                incomingNumber = incomingNumber.substring(indx);
                            }
                        }
                    }
                }
            } catch (Exception e) {
            }
        }

        if (incomingNumber != null) {
            MainActivity.messageRecieved = true;

            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "download MMS");
            wakeLock.acquire(5000);

            boolean error = false;

            // TODO always attempt to run this on android 4.4+
            if (sharedPrefs.getBoolean("override", false) && !sharedPrefs.getBoolean("receive_with_stock", false)) {
                byte[] pushData = intent.getByteArrayExtra("data");
                PduParser parser = new PduParser(pushData);
                GenericPdu pdu = parser.parse();

                if (null == pdu) {
                    return;
                }

                int type = pdu.getMessageType();
                PduPersister p = PduPersister.getPduPersister(context);

                try {
                    boolean groupMMS = false;

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false)) {
                        groupMMS = true;
                    }

                    switch (type) {
                        case PduHeaders.MESSAGE_TYPE_DELIVERY_IND:
                        case PduHeaders.MESSAGE_TYPE_READ_ORIG_IND:
                        case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND: {
                            p.persist(pdu, Inbox.CONTENT_URI, true, groupMMS, null);
                            break;
                        }
                        default:
                            Log.v("MMS Error", "Non recognized pdu_alt header - " + type);
                    }
                } catch (MmsException e1) {
                    e1.printStackTrace();
                    error = true;
                }
            }

            if (lastReceivedNumber.equals(picNumber) && Calendar.getInstance().getTimeInMillis() < lastReceivedTime + (1000 * 10)) {
                Log.v("downloading_mms", "Already saved this message, moving on...");
                return;
            }

            lastReceivedNumber = incomingNumber;
            lastReceivedTime = Calendar.getInstance().getTimeInMillis();
            phoneNumber = incomingNumber.replace("+1", "").replace("+", "").replace("-", "").replace(" ", "").replace("(", "").replace(")", "");
            mmsFrom = ContactUtil.findContactName(phoneNumber, context);

            if (!sharedPrefs.getBoolean("auto_download_mms", false) || sharedPrefs.getBoolean("receive_with_stock", false)) {
                try {
                    Looper.prepare();
                } catch (Throwable e) {
                }

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (sharedPrefs.getBoolean("secure_notification", false)) {
                            MmsReceiverService.makeNotification("New Picture Message", "", null, phoneNumber, "", Calendar.getInstance().getTimeInMillis() + "", context);
                        } else {
                            MmsReceiverService.makeNotification("New Picture Message", mmsFrom, null, phoneNumber, "", Calendar.getInstance().getTimeInMillis() + "", context);
                        }
                    }
                }, 3000);
            } else {
                Intent downloadMessage = new Intent(context, MmsReceiverService.class);
                downloadMessage.putExtra("address", incomingNumber.replace("+1", "").replace("+", "").replace("-", "").replace(" ", "").replace("(", "").replace(")", ""));
                downloadMessage.putExtra("name", mmsFrom);
                context.startService(downloadMessage);
            }

            if (!sharedPrefs.getBoolean("receive_with_stock", false)) {
                error = true;
            }

            if (!Util.isRunning(context)) {
                Intent updateHalo = new Intent("com.klinker.android.messaging.UPDATE_HALO");
                updateHalo.putExtra("name", mmsFrom);
                updateHalo.putExtra("message", "New Picture Message");
                context.sendBroadcast(updateHalo);
            }

            if (!Util.isRunning(context)) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (sharedPrefs.getBoolean("popup_reply", false) && !sharedPrefs.getBoolean("secure_notification", false)) {
                            Intent intent3;

                            if (sharedPrefs.getBoolean("halo_popup", false) || sharedPrefs.getBoolean("full_app_popup", true)) {
                                boolean halo = sharedPrefs.getBoolean("halo_popup", false);

                                if (halo) {
                                    intent3 = new Intent(context, MainActivity.class);
                                } else {
                                    intent3 = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);
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
                                    if (!sharedPrefs.getBoolean("full_app_popup", true) || (sharedPrefs.getBoolean("full_app_popup", true) && !sharedPrefs.getBoolean("slideover_popup_lockscreen_only", false)) || sharedPrefs.getBoolean("unlock_screen", false)) {
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
                            }
                        }

                    }

                }, sharedPrefs.getBoolean("receive_with_stock", false) ? 1000 : 200);
            }

            if (sharedPrefs.getBoolean("override", false) && !error) {
                abortBroadcast();
            } else {
                clearAbortBroadcast();
            }
        }
    }
}