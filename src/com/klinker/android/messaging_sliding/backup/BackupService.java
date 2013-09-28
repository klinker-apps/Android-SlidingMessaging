package com.klinker.android.messaging_sliding.backup;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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

            File backedup = new File(Environment.getExternalStorageDirectory() + "/Messages/SMS/");
            backedup.mkdirs();

            //backedup = new File(Environment.getExternalStorageDirectory() + "/Messages/MMS/");
            //backedup.mkdirs();

            ArrayList<String> threadIds = new ArrayList<String>();
            String[] projection = new String[]{"_id"};
            Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
            Cursor query = context.getContentResolver().query(uri, projection, null, null, null);

            // gets the ids for each conversation
            if (query.moveToFirst())
            {
                do
                {
                    threadIds.add(query.getString(query.getColumnIndex("_id")));
                } while (query.moveToNext());
            }

            query.close();

            NotificationManager mNotifyManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle("Backup to SD Card")
                    .setContentText("Backup in progress")
                    .setSmallIcon(R.drawable.stat_notify_sms)
                    .setOngoing(true);



            // 1. set up the output file with contact names
            // 2. check if it already exists or not
            // 3. start by writing header or appending to the end
            // 4. write the rest of the data to the file

            for (int i = 0; i < threadIds.size(); i++)
            {
                mNotifyManager.notify(0, mBuilder.build());

                ContentResolver contentResolver = getContentResolver();
                final String[] project = new String[]{"_id", "ct_t"};
                uri = Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/");
                query = contentResolver.query(uri, project, null, null, null);

                if (query.moveToFirst()) {
                    do {
                        String string = query.getString(query.getColumnIndex("ct_t"));
                        if ("application/vnd.wap.multipart.related".equals(string)) {
                            // it's MMS

                        } else {
                            // it's SMS

                            String id = query.getString(query.getColumnIndex("_id"));

                            String selection = "_id = " + id;
                            uri = Uri.parse("content://sms");
                            Cursor cursor = contentResolver.query(uri, null, selection, null, null);
                            cursor.moveToFirst();
                            String phone = cursor.getString(cursor.getColumnIndex("address"));
                            int type = cursor.getInt(cursor.getColumnIndex("type"));// 2 = sent, etc.
                            String date = cursor.getString(cursor.getColumnIndex("date"));
                            String body = cursor.getString(cursor.getColumnIndex("body"));
                            cursor.close();

                            // formats the phone number
                            phone = phone.replace(" ", "");

                            if(phone.substring(0,2).equals("+1"))
                                phone = phone.substring(2, phone.length());

                            phone = phone.replace("+", "");
                            phone = phone.replace("-", "");
                            phone = phone.replace("(", "");
                            phone = phone.replace(")", "");

                            if (phone.length() > 10)
                                phone = phone.substring(phone.length() - 10, phone.length());

                            // formats the body to width of 30
                            ArrayList<String> myBody = new ArrayList<String>();

                            while(body.length() > 30)
                            {
                                int index = 30;
                                while(body.charAt(index) != ' ' && index > 0)
                                    index--;
                                if (type == 1)
                                    myBody.add(body.substring(0,index) + "\n");
                                else if (type == 2)
                                    myBody.add(body.substring(0,index) + "\n\t\t\t\t");

                                body = body.substring(index + 1, body.length());
                            }

                            myBody.add(body);

                            body = "";

                            for (int x = 0; x < myBody.size(); x++)
                                body += myBody.get(x);

                            // format the date
                            Date myDate = new Date(Long.parseLong(date));

                            if (sharedPrefs.getBoolean("hour_format", false))
                            {
                                date = (DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(myDate));
                            } else
                            {
                                date = (DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(myDate));
                            }

                            if (sharedPrefs.getBoolean("hour_format", false))
                            {
                                date += " " + (DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(myDate));
                            } else
                            {
                                date += " " + (DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(myDate));
                            }

                            String name = ContactUtil.findContactName(phone, this);

                            name = name.replace(" ", "_");

                            OutputStreamWriter outputStreamWriter;
                            outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/Messages/SMS/" + name + ".txt", true));

                            if (type == 1)
                                outputStreamWriter.append(body + "\n" + date + "\n\n");
                            else if (type == 2)
                                outputStreamWriter.append("\t\t\t\t" + body + "\n\t\t\t\t" + date + "\n\n");

                            outputStreamWriter.close();
                        }
                    } while (query.moveToNext());
                }

                query.close();

                mBuilder.setProgress(threadIds.size(), i, false)
                        .setContentText("Backed up " + i + "/" +threadIds.size() + " conversations");


            }

            mBuilder.setContentText("Backup complete")
                    .setProgress(0,0,false)
                    .setOngoing(false);
            mNotifyManager.notify(0, mBuilder.build());

        }
        catch (IOException e) {

        }
    }

    public void deleteAll()
    {
        NotificationManager mNotifyManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setContentTitle("Deleting Messages")
                .setContentText("Delete in progress")
                .setSmallIcon(R.drawable.ic_action_discard)
                .setOngoing(true);


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
                mBuilder.setProgress(threadIds.size(), i, false)
                        .setContentText("Deleting " + i + "/" +threadIds.size() + " conversations");
                mNotifyManager.notify(0, mBuilder.build());

                context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/"), null, null);
            }
        } catch (Exception e) {
        }

        mBuilder.setContentText("Delete complete")
                .setProgress(0,0,false)
                .setOngoing(false);
        mNotifyManager.notify(0, mBuilder.build());
    }

}
