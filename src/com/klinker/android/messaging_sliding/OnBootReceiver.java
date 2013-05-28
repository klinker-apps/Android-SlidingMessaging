package com.klinker.android.messaging_sliding;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class OnBootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(context);

		if (sharedPrefs.getBoolean("quick_text", false))
		{
			Intent mIntent = new Intent(context, QuickTextService.class);
			context.startService(mIntent);
		}
		
		if (sharedPrefs.getBoolean("background_service", false))
		{
			Intent backIntent = new Intent(context, ConversationSaverService.class);
			context.startService(backIntent);
		}
		
		if (sharedPrefs.getBoolean("delete_old", false))
		{
			Intent deleteIntent = new Intent(context, DeleteOldService.class);
			PendingIntent pintent = PendingIntent.getService(context, 0, deleteIntent, 0);
			AlarmManager alarm = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 6*60*60*1000, pintent);
		}
		
	}

}
