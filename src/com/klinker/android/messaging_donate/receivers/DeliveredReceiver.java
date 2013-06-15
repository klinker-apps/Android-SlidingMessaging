package com.klinker.android.messaging_donate.receivers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

public class DeliveredReceiver extends BroadcastReceiver {

	   @Override 
	   public void onReceive(Context context, Intent intent) {
		   
		   SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(context);
		   
		   if (sharedPrefs.getString("delivery_options", "2").equals("1"))
       	   {
               switch (getResultCode())
               {
                   case Activity.RESULT_OK:
                   	AlertDialog.Builder builder = new AlertDialog.Builder(context);
                       builder.setMessage(R.string.message_delivered)
                              .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int id) {
                                      dialog.dismiss();
                                  }
                              });
                       
                       builder.create().show();
                       
                       Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
                       
                       if (query.moveToFirst())
                       {
                       	String id = query.getString(query.getColumnIndex("_id"));
                       	ContentValues values = new ContentValues();
                       	values.put("status", "0");
                       	values.put("read", true);
                       	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                       }
                       
                       query.close();
                       
                       break;
                   case Activity.RESULT_CANCELED:
                   	AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                       builder2.setMessage(R.string.message_not_delivered)
                              .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int id) {
                                      dialog.dismiss();
                                  }
                              });
                       
                       builder2.create().show();
                       
                       Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
                       
                       if (query2.moveToFirst())
                       {
                       	String id = query2.getString(query2.getColumnIndex("_id"));
                       	ContentValues values = new ContentValues();
                       	values.put("status", "64");
                       	values.put("read", true);
                       	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                       }
                       
                       query2.close();
                       
                       break;
               }
       	} else
       	{
       		switch (getResultCode())
               {
                   case Activity.RESULT_OK:
                   	Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                   	
                   	Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
                       
                       if (query.moveToFirst())
                       {
                       	String id = query.getString(query.getColumnIndex("_id"));
                       	ContentValues values = new ContentValues();
                       	values.put("status", "0");
                       	values.put("read", true);
                       	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                       }
                       
                       query.close();
                       
                       break;
                   case Activity.RESULT_CANCELED:
                   	Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                   	
                   	Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
                       
                       if (query2.moveToFirst())
                       {
                       	String id = query2.getString(query2.getColumnIndex("_id"));
                       	ContentValues values = new ContentValues();
                       	values.put("status", "64");
                       	values.put("read", true);
                       	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                       }
                       
                       query2.close();
                       
                       break;
               }
       	}
	   } 
	}
