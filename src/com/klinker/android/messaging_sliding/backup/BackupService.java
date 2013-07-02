package com.klinker.android.messaging_sliding.backup;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.telephony.SmsManager;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.scheduled.ScheduledSms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

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



        if (sharedPrefs.getBoolean("sd_backup", true))
            backupToSD();

        if (sharedPrefs.getBoolean("drive_backup", false))
            backupToDrive();

        if (sharedPrefs.getBoolean("delete_after_backup", true))
            deleteAll();
    }

    public void backupToDrive()
    {
        Toast.makeText(getApplicationContext(), "Backing up to Drive", Toast.LENGTH_SHORT).show();
    }

    public void backupToSD()
    {
        Toast.makeText(getApplicationContext(), "Backing up to SD card", Toast.LENGTH_SHORT).show();

        try {

            File backedup = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backed_up_messages/");
// have the object build the directory structure, if needed.
            backedup.mkdirs();

            OutputStreamWriter outputStreamWriter;
            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backed_up_messages/ids.txt"));

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

            for (int i = 0; i < threadIds.size(); i++)
                outputStreamWriter.write(threadIds.get(i) + "\n");

            outputStreamWriter.close();

            /*
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

            for (int i = 0; i < threadIds.size(); i++)
                //fos.write(threadIds.get(i).getBytes());*/

        }
        catch (IOException e) {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage("5159911493", null, "in service", null, null);
        }
    }

    public void deleteAll()
    {
        Toast.makeText(getApplicationContext(), "Deleting messages", Toast.LENGTH_SHORT).show();

        /*
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
        }*/
    }

}
