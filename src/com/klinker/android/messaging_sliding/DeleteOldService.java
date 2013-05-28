package com.klinker.android.messaging_sliding;

import java.util.ArrayList;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public class DeleteOldService extends IntentService {

	public DeleteOldService() {
		super("delete_old_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
			final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
			final ContentResolver contentResolver = getContentResolver();
			
			ArrayList<String> threads = new ArrayList<String>();
			
			Cursor query = contentResolver.query(Uri.parse("content://sms/conversations/?simple=true"), 
					                                  null, 
					                                  null, 
					                                  null, 
					                                  null);
			
			if (query.moveToFirst())
			{
				do
				{
					if (Integer.parseInt(query.getString(query.getColumnIndex("msg_count"))) > sharedPrefs.getInt("sms_limit", 500))
					{
						threads.add(query.getString(query.getColumnIndex("thread_id")));
					}
				} while (query.moveToNext());
			}
			
			for (int i = 0; i < threads.size(); i++)
			{
				Cursor deleter = contentResolver.query(Uri.parse("content://sms/conversations/" + threads.get(i)), 
						                               new String[]{"_id", "date"}, 
						                               null, 
						                               null, 
						                               "date asc");
				
				int index = deleter.getCount();
				
				if (deleter.moveToFirst())
				{
					do
					{
						String id = deleter.getString(deleter.getColumnIndex("_id"));
						
						contentResolver.delete(Uri.parse("content://sms/" + id), null, null);
						index--;
					} while (deleter.moveToNext() && index > sharedPrefs.getInt("sms_limit", 500));
				}
				
			}
			
			stopSelf();
			
//			new Thread(new Runnable() {
//
//				@Override
//				public void run() {
//					ArrayList<String> threads = new ArrayList<String>();
//					
//					Cursor query = contentResolver.query(Uri.parse("content://mms/conversations/?simple=true"), 
//							                                  null, 
//							                                  null, 
//							                                  null, 
//							                                  null);
//					
//					Log.v("query", query + "");
//					
//					if (query.moveToFirst())
//					{
//						do
//						{
//							if (Integer.parseInt(query.getString(query.getColumnIndex("msg_count"))) > sharedPrefs.getInt("mms_limit", 100))
//							{
//								threads.add(query.getString(query.getColumnIndex("thread_id")));
//							}
//						} while (query.moveToNext());
//					}
//					
//					for (int i = 0; i < threads.size(); i++)
//					{
//						Cursor deleter = contentResolver.query(Uri.parse("content://mms/conversations/" + threads.get(i)), 
//								                               new String[]{"_id", "date"}, 
//								                               null, 
//								                               null, 
//								                               "date asc");
//						
//						int index = deleter.getCount();
//						
//						if (deleter.moveToFirst())
//						{
//							do
//							{
//								String id = deleter.getString(deleter.getColumnIndex("_id"));
//								
//								contentResolver.delete(Uri.parse("content://mms/" + id), null, null);
//								index--;
//							} while (deleter.moveToNext() && index > sharedPrefs.getInt("mms_limit", 100));
//						}
//						
//					}
//					
//				}
//				
//			}).start();
	}
}