package com.klinker.android.messaging_donate;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity {
	
	public SharedPreferences sharedPrefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		NotificationManager mNotificationManager =
	            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);
		
		Intent fromIntent = getIntent();
		
		boolean flag = false;
		
		if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
		{
			flag = true;
		}
		
		if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout"))
		{
			final Intent intent = new Intent(this, com.klinker.android.messaging_sliding.MainActivity.class);
			intent.setAction(fromIntent.getAction());
			intent.setData(fromIntent.getData());
			
			try
			{
				intent.putExtras(fromIntent.getExtras());
			} catch (Exception e)
			{
				
			}
			
			if (flag)
			{
				intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
			}
			
			intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
		} else if (sharedPrefs.getString("run_as", "sliding").equals("card"))
		{
			final Intent intent = new Intent(this, com.klinker.android.messaging_card.MainActivity.class);
			intent.setAction(fromIntent.getAction());
			intent.setData(fromIntent.getData());
			
			try
			{
				intent.putExtras(fromIntent.getExtras());
			} catch (Exception e)
			{
				
			}
			
			if (flag)
			{
				intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
			}
			
			intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            finish();
		}
	}
}