package com.klinker.android.messaging_sliding;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

public class QmDelete extends IntentService {

	public QmDelete() {
		super("service");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		
        	deleteSMS(this);
        	
        	NotificationManager notificationManager =
                    (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.cancel(1);
            
            writeToFile2(new ArrayList<String>(), this);
            writeToFile(new ArrayList<String>(), this);
            
            Intent intent2 = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
    	    this.sendBroadcast(intent2);
    	    
    	    Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
 		   	PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
 		   	AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
 		   	alarm.cancel(pStopRepeating);

            Intent floatingNotifications = new Intent();
            floatingNotifications.setAction("robj.floating.notifications.dismiss");
            floatingNotifications.putExtra("package", getPackageName());
            sendBroadcast(floatingNotifications);

            Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
            sendBroadcast(updateWidget);
            
        	stopSelf();
		
	}

	public void deleteSMS(Context context) {
	    try {
	        Uri uriSms = Uri.parse("content://sms/inbox");
	        Cursor c = context.getContentResolver().query(uriSms,
	            new String[] { "_id" }, null, null, "date desc");

	        if (c != null && c.moveToFirst()) {
                String id = c.getString(c.getColumnIndex("_id"));
                
                getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
	        }
	        c.close();
	    } catch (Exception e) {
	    	
	    }
    }
    
    private void writeToFile2(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("notifications.txt", Context.MODE_PRIVATE));
            
            for (int i = 0; i < data.size(); i++)
            {
            	outputStreamWriter.write(data.get(i) + "\n");
            }
            	
            outputStreamWriter.close();
        }
        catch (IOException e) {
            
        } 
		
	}
    
    private void writeToFile(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("newMessages.txt", Context.MODE_PRIVATE));
            
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
