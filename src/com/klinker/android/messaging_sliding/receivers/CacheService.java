package com.klinker.android.messaging_sliding.receivers;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

import java.util.ArrayList;

/**
 * Service for caching conversation list and conversations
 */

public class CacheService extends IntentService {

    public static boolean cached = false;
    public static Cursor conversationList;
    public static ArrayList<Cursor> conversations;
    public SharedPreferences sharedPrefs;

    public CacheService() {
        super("Cache Service");
    }

    @Override
    public void onHandleIntent(Intent intent) {
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        cached = false;
        String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
        Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");

        conversationList = getContentResolver().query(uri, projection, null, null, "date desc");
        conversations = new ArrayList<Cursor>();

        if (conversationList.moveToFirst()) {
            int numToCache = sharedPrefs.getInt("num_cache_conversations", 5);

            if (conversationList.getCount() < numToCache) {
                numToCache = conversationList.getCount();
            }

            int reps = 0;

            do {
                Uri uri2 = Uri.parse("content://mms-sms/conversations/" + conversationList.getString(conversationList.getColumnIndex("_id")) + "/");
                String[] projection2;
                String proj = "_id body date type read msg_box";

                if (sharedPrefs.getBoolean("show_original_timestamp", false))
                {
                    proj += " date_sent";
                }

                if (sharedPrefs.getBoolean("delivery_reports", false)) {
                    proj += " status";
                }

                projection2 = proj.split(" ");

                String sortOrder = "normalized_date desc";

                if (sharedPrefs.getBoolean("limit_messages", true))
                {
                    sortOrder += " limit 20";
                }

                conversations.add(getContentResolver().query(uri2, projection2, null, null, sortOrder));

                reps++;
                conversationList.moveToNext();
            } while (reps < numToCache);
        }

        cached = true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            onCreate();
        } else {
            super.onDestroy();
        }

    }
}