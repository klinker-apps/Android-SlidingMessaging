package com.klinker.android.messaging_sliding.backup;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.scheduled.ScheduledSms;

import java.util.ArrayList;

/**
 * Created by luke on 7/1/13.
 */
public class BackupService extends IntentService {

    SharedPreferences sharedPrefs;
    Context context;

    public BackupService() {
        super("BackupService");
    }

    @Override
    public void onHandleIntent(Intent intent)
    {
        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = getApplicationContext();

        ArrayList<String> numbers = new ArrayList<String>();

        backupToDrive();
        backupToSD();

        deleteAll();
    }

    public void backupToDrive()
    {

    }

    public void backupToSD()
    {

    }

    public void deleteAll()
    {
        ArrayList<String> threadIds = new ArrayList<String>();
        String[] projection = new String[]{"_id"};
        Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
        Cursor query = context.getContentResolver().query(uri, projection, null, null, null);

        if (query.moveToFirst())
        {
            do
            {
                threadIds.add(query.getString(query.getColumnIndex("_id")));
            } while (query.moveToNext());
        }

        try {
            for (int i = 0; i < threadIds.size(); i++)
            {
                context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/"), null, null);
            }
        } catch (Exception e) {
        }
    }
}
