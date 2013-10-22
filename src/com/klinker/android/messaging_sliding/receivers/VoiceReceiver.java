package com.klinker.android.messaging_sliding.receivers;

import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.android.internal.telephony.GsmAlphabet;
import com.google.gson.annotations.SerializedName;
import com.klinker.android.send_message.Utils;
import com.koushikdutta.ion.Ion;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;

public class VoiceReceiver extends Service {

    private SharedPreferences settings;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        startRefresh();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        int ret = super.onStartCommand(intent, flags, startId);

        if (null == settings.getString("voice_account", null)) {
            stopSelf();
            return ret;
        }

        if (intent == null)
            return ret;

        startRefresh();

        return ret;
    }

    public static class Payload {
        @SerializedName("messageList")
        public ArrayList<Conversation> conversations = new ArrayList<Conversation>();
    }

    public static class Conversation {
        @SerializedName("children")
        public ArrayList<Message> messages = new ArrayList<Message>();
    }

    public static class Message {
        @SerializedName("startTime")
        public long date;

        @SerializedName("phoneNumber")
        public String phoneNumber;

        @SerializedName("message")
        public String message;

        // 10 is incoming
        // 11 is outgoing
        @SerializedName("type")
        int type;
    }

    private static final int VOICE_INCOMING_SMS = 10;
    private static final int VOICE_OUTGOING_SMS = 11;

    private static final int PROVIDER_INCOMING_SMS = 1;
    private static final int PROVIDER_OUTGOING_SMS = 2;

    // insert a message into the sms/mms provider.
    // we do this in the case of outgoing messages
    // that were not sent via this phone, and also on initial
    // message sync.
    void insertMessage(String number, String text, int type, long date) {
        ContentValues values = new ContentValues();
        values.put("address", number);
        values.put("body", text);
        values.put("type", type);
        values.put("date", date);
        values.put("read", 1);

        if (type == PROVIDER_OUTGOING_SMS) {
            values.put("status", 2);
        }

        getContentResolver().insert(Uri.parse("content://sms/sent"), values);
    }

    // refresh the messages that were on the server
    void refreshMessages() throws Exception {
        String account = settings.getString("voice_account", null);
        if (account == null)
            return;

        // tokens!
        String authToken = Utils.getAuthToken(account, this);

        Payload payload = Ion.with(this)
                .load("https://www.google.com/voice/request/messages")
                .setHeader("Authorization", "GoogleLogin auth=" + authToken)
                .as(Payload.class)
                .get();

        ArrayList<Message> all = new ArrayList<Message>();
        for (Conversation conversation : payload.conversations) {
            for (Message message : conversation.messages)
                all.add(message);
        }

        // sort by date order so the events get added in the same order
        Collections.sort(all, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                if (lhs.date == rhs.date)
                    return 0;
                if (lhs.date > rhs.date)
                    return 1;
                return -1;
            }
        });

        Log.v("refresh_voice", "at timestamp");

        long timestamp = settings.getLong("voice_refresh_timestamp", 0);
        boolean first = timestamp == 0;
        long max = timestamp;
        for (Message message : all) {
            max = Math.max(max, message.date);
            if (message.phoneNumber == null)
                continue;
            if (message.date <= timestamp)
                continue;
            if (message.message == null)
                continue;

            // on first sync, just populate the mms provider...
            // don't send any broadcasts.
            if (first) {
                int type;
                if (message.type == VOICE_INCOMING_SMS)
                    type = PROVIDER_INCOMING_SMS;
                else if (message.type == VOICE_OUTGOING_SMS)
                    type = PROVIDER_OUTGOING_SMS;
                else
                    continue;
                // just populate the content provider and go
                insertMessage(message.phoneNumber, message.message, type, message.date);
                continue;
            }

            // sync up outgoing messages (ones sent from other devices)
            if (message.type == VOICE_OUTGOING_SMS) {
                Log.v("refresh_voice", "new outgoing message we will try and save...");

                boolean exists = false;

                Uri uri = Uri.parse("content://sms/sent");
                String[] projection = new String[]{message.message};

                Cursor cursor = getContentResolver().query(uri, new String[]{"_id", "body", "date"}, "body=?", projection, null);

                if (cursor.moveToFirst()) {
                    do {
                        long date = Long.parseLong(cursor.getString(cursor.getColumnIndex("date")));
                        Log.v("refresh_voice", "saved message date: " + date + " downloaded message date: " + message.date);

                        if (date + 60000 > message.date && date - 60000 < message.date) {
                            Log.v("refresh_voice", "message already exists");
                            exists = true;
                            break;
                        }
                    } while (cursor.moveToNext());
                }

                if (!exists) {
                    insertMessage(message.phoneNumber, message.message, PROVIDER_OUTGOING_SMS, message.date);
                }

                continue;
            }

            if (message.type != VOICE_INCOMING_SMS)
                continue;

            try {
                Log.v("refresh_voice", "sending sms broadcast");
                /*Intent smsBroadcast = new Intent("com.klinker.android.messaging.VOICE_RECEIVED");
                smsBroadcast.putExtra("voice_message", true);
                smsBroadcast.putExtra("voice_body", message.message);
                smsBroadcast.putExtra("voice_address", message.phoneNumber);
                smsBroadcast.putExtra("voice_date", message.date);
                sendBroadcast(smsBroadcast);*/
                sendSms(getApplicationContext(), message.phoneNumber, message.message);
            } catch (Exception e) {
                e.printStackTrace();
                Log.v("refresh_voice", "failed to send sms broadcast");
            }
        }

        settings.edit()
                .putLong("voice_refresh_timestamp", max)
                .commit();
    }

    void startRefresh() {
        needsRefresh = true;

        Log.v("refresh_voice", "starting refresh...");

        // if a sync is in progress, dont start another
        if (refreshThread != null && refreshThread.getState() != Thread.State.TERMINATED)
            return;

        refreshThread = new Thread() {
            @Override
            public void run() {
                while (needsRefresh) {
                    try {
                        needsRefresh = false;
                        refreshMessages();
                    } catch (Exception e) {
                        Log.v("refresh_voice", "error refreshing");
                        e.printStackTrace();
                        needsRefresh = true;
                        break;
                    }
                }
            }
        };

        refreshThread.start();
    }

    boolean needsRefresh;
    Thread refreshThread;

    private void sendSms(Context context, String sender, String body) {
        byte [] pdu = null ;
        byte [] scBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD("0000000000");
        byte [] senderBytes = PhoneNumberUtils
                .networkPortionToCalledPartyBCD(sender);
        int lsmcs = scBytes.length;
        byte [] dateBytes = new byte [ 7 ];
        Calendar calendar = new GregorianCalendar();
        dateBytes[ 0 ] = reverseByte(( byte ) (calendar.get(Calendar.YEAR)));
        dateBytes[ 1 ] = reverseByte(( byte ) (calendar.get(Calendar.MONTH) + 1 ));
        dateBytes[ 2 ] = reverseByte(( byte ) (calendar.get(Calendar.DAY_OF_MONTH)));
        dateBytes[ 3 ] = reverseByte(( byte ) (calendar.get(Calendar.HOUR_OF_DAY)));
        dateBytes[ 4 ] = reverseByte(( byte ) (calendar.get(Calendar.MINUTE)));
        dateBytes[ 5 ] = reverseByte(( byte ) (calendar.get(Calendar.SECOND)));
        dateBytes[ 6 ] = reverseByte(( byte ) ((calendar.get(Calendar.ZONE_OFFSET) + calendar
                .get(Calendar.DST_OFFSET)) / ( 60 * 1000 * 15 )));
        Log.v("refresh_voice", "done with calender");
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            bo.write(lsmcs);
            bo.write(scBytes);
            bo.write( 0x04 );
            bo.write(( byte ) sender.length());
            bo.write(senderBytes);
            bo.write( 0x00 );
            bo.write( 0x00 );  // encoding: 0 for default 7bit
            bo.write(dateBytes);
            Log.v("refresh_voice", "writing bits");
            try {
                byte[] bodybytes  = GsmAlphabet.stringToGsm7BitPacked(body);
                bo.write(bodybytes);
                Log.v("refresh_voice", "more bits");
            } catch(Exception e) {}

            pdu = bo.toByteArray();
        } catch (IOException e) {
        }

        /*Log.v("refresh_voice", "broadcasting");
        Intent intent = new Intent();
        intent.setClassName( "com.android.mms" ,
                "com.android.mms.transaction.SmsReceiverService" );
        intent.setAction( "android.provider.Telephony.SMS_RECEIVED" );
        intent.putExtra( "pdus" , new Object[] { pdu });
        Log.v("refresh_voice", "last");
        context.startService(intent);*/

        Log.v("refresh_voice", "start broadcast");
        Intent smsBroadcast = new Intent("android.provider.Telephony.SMS_RECEIVED");
        smsBroadcast.putExtra( "pdus" , new Object[] { pdu });
        smsBroadcast.putExtra( "voice_message", true);
        sendBroadcast(smsBroadcast);
    }

    private static byte reverseByte( byte b) {
        return ( byte ) ((b & 0xF0 ) >> 4 | (b & 0x0F ) << 4 );
    }
}