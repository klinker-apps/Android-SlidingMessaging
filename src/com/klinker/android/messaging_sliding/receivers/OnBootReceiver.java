package com.klinker.android.messaging_sliding.receivers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

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

public class OnBootReceiver extends BroadcastReceiver {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";

    private Context context;
    private SharedPreferences sharedPrefs;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(context);
        this.context = context;

		if (sharedPrefs.getBoolean("quick_text", false))
		{
			Intent mIntent = new Intent(context, QuickTextService.class);
			context.startService(mIntent);
		}
		
		if (sharedPrefs.getBoolean("delete_old", false))
		{
			Intent deleteIntent = new Intent(context, DeleteOldService.class);
			PendingIntent pintent = PendingIntent.getService(context, 0, deleteIntent, 0);
			AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 6*60*60*1000, pintent);
		}

        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            Intent cacheService = new Intent(context, CacheService.class);
            context.startService(cacheService);
        }

        if (sharedPrefs.getBoolean("slideover_enabled", false)) {
            Intent service = new Intent(context, com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
            context.startService(service);
        }

        resetAlarms(sharedPrefs, context);
		
	}

    public void resetAlarms(SharedPreferences sharedPrefs, Context context)
    {
        ArrayList<String[]> list = readFromFile(true);

        for (int i = 0; i < list.size(); i++)
        {
            createAlarm(list.get(i));
        }
    }

    public void createAlarm(String[] details)
    {
        Intent serviceIntent = new Intent(context, ScheduledService.class);

        serviceIntent.putExtra(EXTRA_MESSAGE, details[3]);
        serviceIntent.putExtra(EXTRA_NUMBER, details[0]);

        PendingIntent pi = getDistinctPendingIntent(serviceIntent, Integer.parseInt(details[4]));

        // Schedule the alarm!
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (details[2].equals("None"))
        {
            am.set(AlarmManager.RTC_WAKEUP,
                    Long.parseLong(details[1]),
                    pi);
        } else
        {
            long alarmFireTime = getNextTime(details[2], Long.parseLong(details[1])); // finds the next time the alarm will go off by sending the repetition and the time it is suppose to go off

            if (details[2].equals("Daily"))
            {
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        alarmFireTime,
                        AlarmManager.INTERVAL_DAY,
                        pi);
            } else if (details[2].equals("Weekly"))
            {
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        alarmFireTime,
                        AlarmManager.INTERVAL_DAY * 7,
                        pi);
            } else if (details[2].equals("Yearly"))
            {
                am.setRepeating(AlarmManager.RTC_WAKEUP,
                        alarmFireTime,
                        AlarmManager.INTERVAL_DAY * 365,
                        pi);
            }
        }

    }

    public long getNextTime(String repetition, long firstFire)
    {
        long nextFireLong = 0;

        Calendar nextFire = Calendar.getInstance();
        nextFire.setTimeInMillis(firstFire);

        Calendar currentTime = Calendar.getInstance();

        while(nextFire.before(currentTime))
        {
            if (repetition.equals("Daily"))
                nextFire.add(Calendar.DATE, 1);
            else if (repetition.equals("Weekly"))
                nextFire.add(Calendar.DATE, 7);
            else
                nextFire.add(Calendar.DATE, 365);
        }

        nextFireLong = nextFire.getTimeInMillis();

        return nextFireLong;
    }

    protected PendingIntent getDistinctPendingIntent(Intent intent, int requestId)
    {
        PendingIntent pi =
                PendingIntent.getService(
                        context,         //context
                        requestId,    //request id
                        intent,       //intent to be delivered
                        0);

        return pi;
    }

    @SuppressWarnings("resource")
    private ArrayList<String[]> readFromFile(boolean tryRemove) {

        ArrayList<String[]> ret = new ArrayList<String[]>();

        if (tryRemove)
            removeOld();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt");
            } else
            {
                inputStream = context.openFileInput("scheduledSMS.txt");
            }



            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {

                    String[] details = new String[5];
                    details[0] = receiveString;

                    for(int i = 1; i < 5; i++)
                        details[i] = bufferedReader.readLine();

                    ret.add(details);
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    private void writeToFile(ArrayList<String[]> data) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt"));
            } else
            {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("scheduledSMS.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++)
            {
                String[] details = data.get(i);

                for (int j = 0; j < 5; j++)
                {
                    outputStreamWriter.write(details[j] + "\n");
                }


            }

            outputStreamWriter.close();
        }
        catch (IOException e) {

        }

    }

    public void removeOld()
    {
        ArrayList<String[]> list = readFromFile(false);

        for(int i = 0; i < list.size(); i++)
        {
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
