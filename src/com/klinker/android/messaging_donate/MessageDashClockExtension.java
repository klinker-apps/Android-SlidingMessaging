package com.klinker.android.messaging_donate;

import com.klinker.android.messaging_donate.R;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class MessageDashClockExtension extends DashClockExtension {

	@Override
	protected void onInitialize(boolean isReconnect)
	{
		super.onInitialize(isReconnect);
		
		String[] watcher = {"content://sms"};
		this.addWatchContentUris(watcher);
		this.setUpdateWhenScreenOn(true);
	}
	
	@Override
	protected void onUpdateData(int arg0) {
		ArrayList<String> senderNames = readFromFile(this);
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (senderNames.size() > 0)
		{
			String expandedTitle = senderNames.size() + " New Message";
			
			if (senderNames.size() > 1)
			{
				expandedTitle += "s";
			}
			
			String names = senderNames.get(0);
			
			for (int i = 1; i < senderNames.size(); i++)
			{
				names += ", " + senderNames.get(i);
			}
			
			if (sharedPrefs.getBoolean("secure", false))
			{
				names = " ";
			}
			
			Intent intent = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);
			Bundle b = new Bundle();
			b.putBoolean("dashclock", true);
			intent.putExtras(b);
			
			publishUpdate(new ExtensionData()
	        	.visible(true)
	        	.icon(R.drawable.dashclock)
	        	.status(senderNames.size() + "")
	        	.expandedTitle(expandedTitle)
	        	.expandedBody(names)
	        	.clickIntent(intent));
		} else
		{
			if (!sharedPrefs.getBoolean("persistent", false))
			{
				publishUpdate(new ExtensionData());
			} else
			{
				Intent intent = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);
				Bundle b = new Bundle();
				b.putBoolean("dashclock", true);
				intent.putExtras(b);
				
				publishUpdate(new ExtensionData()
		        	.visible(true)
		        	.icon(R.drawable.dashclock)
		        	.status("0")
		        	.expandedTitle("0 New Messages")
		        	.clickIntent(intent));
			}
		}
	}
	
	private ArrayList<String> readFromFile(Context context) {
		
	      ArrayList<String> ret = new ArrayList<String>();
	      
	      try {
	          InputStream inputStream = context.openFileInput("newMessages.txt");
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null ) {
	          		ret.add(receiveString);
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
}