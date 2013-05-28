package com.klinker.android.messaging_sliding;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

@SuppressLint("Wakelock")
public class ConversationSaverService extends IntentService {

	public ConversationSaverService() {
		super("conversation_saver_service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
			final WakeLock wakeLock = pm.newWakeLock((PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
	        wakeLock.acquire();
        
			ArrayList<String> data = new ArrayList<String>();
		
			String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
			Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
			Cursor query = getContentResolver().query(uri, projection, null, null, "date desc");
			
			if (query.moveToFirst())
			{
				do
				{
					data.add(query.getString(query.getColumnIndex("_id")));
					data.add(query.getString(query.getColumnIndex("message_count")));
					data.add(query.getString(query.getColumnIndex("read")));
					
					data.add(" ");
					
					try
					{
						data.set(data.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
					} catch (Exception e)
					{
					}
					
					data.add(query.getString(query.getColumnIndex("date")));
					
					String[] ids = query.getString(query.getColumnIndex("recipient_ids")).split(" ");
					String numbers = "";
					
					for (int i = 0; i < ids.length; i++)
					{
						try
						{
							if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" ")))
							{
								Cursor number = getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);
								
								if (number.moveToFirst())
								{
									numbers += number.getString(number.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
								} else
								{
									numbers += "0 ";
								}
								
								number.close();
							} else
							{
								
							}
						} catch (Exception e)
						{
							numbers += "0 ";
						}
					}
					
					data.add(numbers.trim());
					
					if (ids.length > 1)
					{
						data.add("yes");
					} else
					{
						data.add("no");
					}
				} while (query.moveToNext());
			}
			
			query.close();
			
			writeToFile(data, this);
            
			wakeLock.release();
        	stopSelf();
        	
	}
    
    private void writeToFile(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("conversationList.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
}
