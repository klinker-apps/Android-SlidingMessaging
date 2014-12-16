package com.klinker.android.messaging_sliding.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import com.klinker.android.messaging_sliding.DeleteOldService;
import com.klinker.android.messaging_sliding.scheduled.ScheduledService;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class OnBootReceiver extends BroadcastReceiver {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";

    private Context context;
    private SharedPreferences sharedPrefs;

    @Override
    public void onReceive(Context context, Intent intent) {

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

        if (sharedPrefs.getBoolean("quick_text", false)) {
            Intent mIntent = new Intent(context, QuickTextService.class);
            context.startService(mIntent);
        }

        if (sharedPrefs.getBoolean("delete_old", false)) {
            Intent deleteIntent = new Intent(context, DeleteOldService.class);
            PendingIntent pintent = PendingIntent.getService(context, 0, deleteIntent, 0);
            AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 6 * 60 * 60 * 1000, pintent);
        }

        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            Intent cacheService = new Intent(context, CacheService.class);
            context.startService(cacheService);
        }

        if (sharedPrefs.getBoolean("slideover_enabled", false)) {
            Intent service = new Intent(context, com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
            context.startService(service);
        }

        if (sharedPrefs.getString("repeatingVoiceInterval", null) != null) {
            startVoiceReceiverManager(context, Calendar.getInstance().getTimeInMillis(), Long.parseLong(sharedPrefs.getString("repeatingVoiceInterval", null)));
        }

        ScheduledService.scheduleNextAlarm(context);
    }

    public static void startVoiceReceiverManager(Context context, long startTime, long interval) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, startTime, interval, getVoiceReceiverPIntent(context));
    }

    public static void stopVoiceReceiverManager(Context context) {
        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getVoiceReceiverPIntent(context));
    }

    public static PendingIntent getVoiceReceiverPIntent(Context context) {
        return PendingIntent.getService(context, 0, new Intent(context, VoiceReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @SuppressWarnings("resource")
    private ArrayList<String[]> readFromFile(boolean tryRemove) {

        ArrayList<String[]> ret = new ArrayList<String[]>();

        if (tryRemove)
            removeOld();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt");
            } else {
                inputStream = context.openFileInput("scheduledSMS.txt");
            }


            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {

                    String[] details = new String[5];
                    details[0] = receiveString;

                    for (int i = 1; i < 5; i++)
                        details[i] = bufferedReader.readLine();

                    ret.add(details);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    private void writeToFile(ArrayList<String[]> data) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (sharedPrefs.getBoolean("save_to_external", true)) {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt"));
            } else {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("scheduledSMS.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++) {
                String[] details = data.get(i);

                for (int j = 0; j < 5; j++) {
                    outputStreamWriter.write(details[j] + "\n");
                }


            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    public void removeOld() {
        ArrayList<String[]> list = readFromFile(false);

        for (int i = 0; i < list.size(); i++) {
            try {
                Date sendDate = new Date(Long.parseLong(list.get(i)[1]));
                if (sendDate.before(new Date()) && list.get(i)[2].equals("None")) // date is earlier than current and no repetition
                {
                    list.remove(i);
                    i--;
                }
            } catch (Exception e) {

            }
        }

        writeToFile(list);
    }

}
