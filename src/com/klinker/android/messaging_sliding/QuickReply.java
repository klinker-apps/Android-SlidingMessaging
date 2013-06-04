package com.klinker.android.messaging_sliding;

import android.widget.*;
import com.klinker.android.messaging_donate.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;

@SuppressWarnings("deprecation")
public class QuickReply extends Activity {
	
	public EditText messageEntry;
	
	@SuppressLint("SimpleDateFormat")
    @Override
	protected void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		this.setFinishOnTouchOutside(false);
		
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		{
			setTheme(android.R.style.Theme_Holo_Dialog);
		} else
		{
			setTheme(android.R.style.Theme_Holo_Light_Dialog);
		}
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.popup);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
		          WindowManager.LayoutParams.FLAG_BLUR_BEHIND);
	    
	    QuickContactBadge contactBadge = (QuickContactBadge) findViewById(R.id.popupBadge);
	    TextView contactName = (TextView) findViewById(R.id.popupName);
	    TextView contactDate = (TextView) findViewById(R.id.popupDate);
	    TextView contactBody = (TextView) findViewById(R.id.popupBody);
	    messageEntry = (EditText) findViewById(R.id.popupEntry);
	    final TextView charsRemaining = (TextView) findViewById(R.id.popupChars);
	    ImageButton sendButton = (ImageButton) findViewById(R.id.popupSend);
	    Button viewConversation = (Button) findViewById(R.id.viewConversation);
	    ImageButton readButton = (ImageButton) findViewById(R.id.readButton);
	    ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
	    
	    viewConversation.setText("View Conversation");
	    
	    if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
	    {
	    	sendButton.setBackgroundResource(R.drawable.dark_send_button_popup);
	    	sendButton.setImageResource(R.drawable.ic_action_send_white);
	    	readButton.setImageResource(R.drawable.ic_menu_done_holo_dark);
	    	readButton.setBackgroundResource(R.drawable.dark_send_button_popup);
	    	deleteButton.setImageResource(R.drawable.ic_menu_delete);
	    	deleteButton.setBackgroundResource(R.drawable.dark_send_button_popup);
	    	charsRemaining.setTextColor(getResources().getColor(R.color.dark_grey));
	    	contactName.setTextColor(this.getResources().getColor(R.color.dark_grey));
			contactDate.setTextColor(this.getResources().getColor(R.color.dark_grey));
			contactBody.setTextColor(this.getResources().getColor(R.color.dark_grey));
	    } else
	    {
	    	sendButton.setBackgroundResource(R.drawable.light_send_button_popup);
	    	sendButton.setImageResource(R.drawable.ic_action_send_black);
	    	readButton.setImageResource(R.drawable.ic_menu_done_holo_light);
	    	readButton.setBackgroundResource(R.drawable.light_send_button_popup);
	    	deleteButton.setImageResource(R.drawable.ic_menu_delete_light);
	    	deleteButton.setBackgroundResource(R.drawable.light_send_button_popup);
	    	charsRemaining.setTextColor(getResources().getColor(R.color.light_grey));
	    	contactName.setTextColor(this.getResources().getColor(R.color.light_grey));
			contactDate.setTextColor(this.getResources().getColor(R.color.light_grey));
			contactBody.setTextColor(this.getResources().getColor(R.color.light_grey));
	    }
		  
		  if (sharedPrefs.getBoolean("custom_font", false))
		  {
			  Typeface font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
			  
			  contactName.setTypeface(font);
			  contactDate.setTypeface(font);
			  contactBody.setTypeface(font);
			  messageEntry.setTypeface(font);
			  charsRemaining.setTypeface(font);
		  }
	    
	    if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
	    
	    String body = "";
	    String number = "";
	    String date = "";
	    String name = "";
	    String id = "";
	    
	    try
	    {
		    Bundle b = this.getIntent().getBundleExtra("bundle");
			
			if (b != null)
			{
				body = b.getString("body", "placeholder");
				number = b.getString("address", "placeholder");
				date = b.getString("date", "0");
				
				try
				{
					if (b.getString("notification").equals("true"))
					{
						KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE); 
				        KeyguardLock keyguardLock =  keyguardManager.newKeyguardLock("TAG");
				        keyguardLock.disableKeyguard();
					}
				} catch (Exception e)
				{
					
				}
			} else
			{
				Uri uri = Uri.parse("content://sms/inbox");
				Cursor c = getContentResolver().query(uri, null, null ,null,null);
				
				try
				{
					if(c.moveToFirst()){
			            body = c.getString(c.getColumnIndexOrThrow("body")).toString();
			            number = c.getString(c.getColumnIndexOrThrow("address")).toString().replaceAll("[^0-9\\+]", "");
			            date = c.getString(c.getColumnIndexOrThrow("date")).toString();
			            
			            if (number.length() == 11)
			            	number = number.substring(1,11);
					}
				} finally
				{
					c.close();
				}
			}
	    } catch (Exception e)
	    {
	    	Uri uri = Uri.parse("content://sms/inbox");
			Cursor c = getContentResolver().query(uri, null, null ,null,null);
			
			try
			{
				if(c.moveToFirst()){
		            body = c.getString(c.getColumnIndexOrThrow("body")).toString();
		            number = c.getString(c.getColumnIndexOrThrow("address")).toString().replaceAll("[^0-9\\+]", "");
		            date = c.getString(c.getColumnIndexOrThrow("date")).toString();
		            
		            if (number.length() == 11)
		            	number = number.substring(1,11);
				}
			} finally
			{
				c.close();
			}
	    }
		
		Date date2;
	    
		try
		{
			date2 = new Date(Long.parseLong(date));
		} catch (Exception e)
		{
			Calendar cal = Calendar.getInstance();
			date2 = new Date(cal.getTimeInMillis());
		}
		
		  if (sharedPrefs.getBoolean("hour_format", false))
		  {
			  contactDate.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
		  } else
		  {
			  contactDate.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
		  }

	    if (sharedPrefs.getString("smilies", "with").equals("with"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
			  } else
			  {
				  contactBody.setText(EmoticonConverter2.getSmiledText(this, body));
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("without"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter.getSmiledText(this, body)));
			  } else
			  {
				  contactBody.setText(EmoticonConverter.getSmiledText(this, body));
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("none"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  if (sharedPrefs.getBoolean("emoji_type", true))
				  {
					  contactBody.setText(EmojiConverter2.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
				  } else
				  {
					  contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
				  }
			  } else
			  {
				  contactBody.setText(body);
			  }
		  } else if (sharedPrefs.getString("smilies", "with").equals("both"))
		  {
			  String patternStr = "[^\\x20-\\x7E]";
			  Pattern pattern = Pattern.compile(patternStr);
			  Matcher matcher = pattern.matcher(body);
			  
			  if (matcher.find())
			  {
				  if (sharedPrefs.getBoolean("emoji_type", true))
				  {
					  contactBody.setText(EmojiConverter2.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
				  } else
				  {
					  contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
				  }
		      } else
			  {
				  contactBody.setText(EmoticonConverter3.getSmiledText(this, body));
			  }
		  }
	    
	    contactBody.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
	    
	    try
        {
			Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
			Cursor phonesCursor = this.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID}, null, null, null);
	
			if(phonesCursor != null && phonesCursor.moveToFirst()) {
				name = phonesCursor.getString(0);
				id = phonesCursor.getString(1);
			} else
			{
				Locale sCachedLocale = Locale.getDefault();
				int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
				Editable editable = new SpannableStringBuilder(number);
				PhoneNumberUtils.formatNumber(editable, sFormatType);
				name = editable.toString();
				
				id = "0";
			}
			
			phonesCursor.close();
        } catch (IllegalArgumentException e)
        {
        	name = number;
        	id = "0";
        } catch (NullPointerException e)
        {
        	name = number;
        	id = "0";
        }
	    
	    contactName.setText(name);
	    
	    InputStream input = openDisplayPhoto(Long.parseLong(id), this);
		
		if (input == null)
		{
			if (sharedPrefs.getBoolean("ct_darkContctImage", false))
			{
				input = this.getResources().openRawResource(R.drawable.ic_contact_dark);
			} else
			{
				input = this.getResources().openRawResource(R.drawable.ic_contact_picture);
			}
		}
		
		Bitmap contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 120, 120, true);
		
		contactBadge.assignContactFromPhone(number, false);
		contactBadge.setMode(ContactsContract.QuickContact.MODE_LARGE);
		contactBadge.setImageBitmap(contactImage);
		final Context context = this;
		final String sendTo = number;
		
		sendButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (messageEntry.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
				} else
				{
					if (sharedPrefs.getBoolean("delivery_reports", false))
					{
						String SENT = "SMS_SENT";
				        String DELIVERED = "SMS_DELIVERED";
				 
				        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				            new Intent(SENT), 0);
				 
				        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
				            new Intent(DELIVERED), 0);
				 
				        //---when the SMS has been sent---
				        registerReceiver(new BroadcastReceiver(){
				            @Override
				            public void onReceive(Context arg0, Intent arg1) {
				                switch (getResultCode())
				                {
				                case Activity.RESULT_OK:
			                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "2");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        break;
			                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                    	NotificationCompat.Builder mBuilder =
			    	                	new NotificationCompat.Builder(context)
			    	                .setSmallIcon(R.drawable.ic_alert)
			    	                .setContentTitle("Error")
			    	                .setContentText("Could not send message");
					    	        
					    	        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
					    	
					    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
					    	        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
					    	        stackBuilder.addNextIntent(resultIntent);
					    	        PendingIntent resultPendingIntent =
					    	                stackBuilder.getPendingIntent(
					    	                    0,
					    	                    PendingIntent.FLAG_UPDATE_CURRENT
					    	                );
					    	        
					    	        mBuilder.setContentIntent(resultPendingIntent);
					    	        mBuilder.setAutoCancel(true);
					    	        long[] pattern = {0L, 400L, 100L, 400L};
					    	        mBuilder.setVibrate(pattern);
					    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
					    	        
					    	        try
					    	        {
					    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
					    	        } catch(Exception e)
					    	        {
					    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
					    	        }
					    	        
					    	        NotificationManager mNotificationManager =
					    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					    	        
					    	        Notification notification = mBuilder.build();
					    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
					    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
					    	        mNotificationManager.notify(1, notification);
			                        break;
			                    case SmsManager.RESULT_ERROR_NO_SERVICE:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "No service", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
			                    case SmsManager.RESULT_ERROR_NULL_PDU:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "Null PDU", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
			                    case SmsManager.RESULT_ERROR_RADIO_OFF:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "Radio off", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
				                }
				                
				                unregisterReceiver(this);
				            }
				        }, new IntentFilter(SENT));
				 
				        //---when the SMS has been delivered---
				        registerReceiver(new BroadcastReceiver(){
				            @Override
				            public void onReceive(Context arg0, Intent arg1) {
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
					                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
					                        }
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
					                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
					                        }
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
					                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
					                        }
					                        
					                        break;
					                    case Activity.RESULT_CANCELED:
					                    	Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
					                    	
					                    	Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");
					                        
					                        if (query2.moveToFirst())
					                        {
					                        	String id = query2.getString(query2.getColumnIndex("_id"));
					                        	ContentValues values = new ContentValues();
					                        	values.put("status", "64");
					                        	context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
					                        }
					                        
					                        break;
					                }
				            	}
				                
				                context.unregisterReceiver(this);
				            }
				        }, new IntentFilter(DELIVERED));
				        
				        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
				        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
				        
				        String body2 = messageEntry.getText().toString();
				        
				        if (sharedPrefs.getBoolean("strip_unicode", false))
				        {
				        	body2 = StripAccents.stripAccents(body2);
				        }
				        
				        if (!sharedPrefs.getString("signature", "").equals(""))
						{
							body2 += "\n" + sharedPrefs.getString("signature", "");
						}
				        
						SmsManager smsManager = SmsManager.getDefault();

						if (sharedPrefs.getBoolean("split_sms", false))
						{
							int length = 160;
							
							String patternStr = "[^\\x20-\\x7E]";
							Pattern pattern = Pattern.compile(patternStr);
							Matcher matcher = pattern.matcher(body2);
							  
							if (matcher.find())
							{
								length = 70;
							}
							
							String[] textToSend = MainActivity.splitByLength(body2, length);
							
							for (int i = 0; i < textToSend.length; i++)
							{
								ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
								
								for (int j = 0; j < parts.size(); j++)
								{
									sPI.add(sentPI);
									dPI.add(deliveredPI);
								}
								
								smsManager.sendMultipartTextMessage(sendTo, null, parts, sPI, dPI);
							}
						} else
						{
							ArrayList<String> parts = smsManager.divideMessage(body2); 
							
							for (int i = 0; i < parts.size(); i++)
							{
								sPI.add(sentPI);
								dPI.add(deliveredPI);
							}
							
							smsManager.sendMultipartTextMessage(sendTo, null, parts, sPI, dPI);
						}
					} else
					{
						String SENT = "SMS_SENT";
						 
				        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
				            new Intent(SENT), 0);
				 
				        //---when the SMS has been sent---
				        context.registerReceiver(new BroadcastReceiver(){
				            @Override
				            public void onReceive(Context arg0, Intent arg1) {
				                switch (getResultCode())
				                {
				                case Activity.RESULT_OK:
			                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "2");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        break;
			                    case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                    	NotificationCompat.Builder mBuilder =
			    	                	new NotificationCompat.Builder(context)
			    	                .setSmallIcon(R.drawable.ic_alert)
			    	                .setContentTitle("Error")
			    	                .setContentText("Could not send message");
					    	        
					    	        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
					    	
					    	        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
					    	        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
					    	        stackBuilder.addNextIntent(resultIntent);
					    	        PendingIntent resultPendingIntent =
					    	                stackBuilder.getPendingIntent(
					    	                    0,
					    	                    PendingIntent.FLAG_UPDATE_CURRENT
					    	                );
					    	        
					    	        mBuilder.setContentIntent(resultPendingIntent);
					    	        mBuilder.setAutoCancel(true);
					    	        long[] pattern = {0L, 400L, 100L, 400L};
					    	        mBuilder.setVibrate(pattern);
					    	        mBuilder.setLights(0xFFffffff, 1000, 2000);
					    	        
					    	        try
					    	        {
					    	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
					    	        } catch(Exception e)
					    	        {
					    	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
					    	        }
					    	        
					    	        NotificationManager mNotificationManager =
					    	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					    	        
					    	        Notification notification = mBuilder.build();
					    	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
					    	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
					    	        mNotificationManager.notify(1, notification);
			                        break;
			                    case SmsManager.RESULT_ERROR_NO_SERVICE:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "No service", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
			                    case SmsManager.RESULT_ERROR_NULL_PDU:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "Null PDU", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
			                    case SmsManager.RESULT_ERROR_RADIO_OFF:
			                    	try
			                    	{
			                    		wait(500);
			                    	} catch (Exception e)
			                    	{
			                    		
			                    	}
			                    	
			                    	query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);
			                        
			                        if (query.moveToFirst())
			                        {
			                        	String id = query.getString(query.getColumnIndex("_id"));
			                        	ContentValues values = new ContentValues();
			                        	values.put("type", "5");
			                        	context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
			                        }
			                        
			                        Toast.makeText(context, "Radio off", 
			                                Toast.LENGTH_SHORT).show();
			                        break;
				                }
				                
				                unregisterReceiver(this);
				            }
				        }, new IntentFilter(SENT));
				        
				        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
				        
				        String body2 = messageEntry.getText().toString();
				        
				        if (sharedPrefs.getBoolean("strip_unicode", false))
				        {
				        	body2 = StripAccents.stripAccents(body2);
				        }
				        
				        if (!sharedPrefs.getString("signature", "").equals(""))
						{
							body2 += "\n" + sharedPrefs.getString("signature", "");
						}
				        
						SmsManager smsManager = SmsManager.getDefault();

						if (sharedPrefs.getBoolean("split_sms", false))
						{
							int length = 160;
							
							String patternStr = "[^\\x20-\\x7E]";
							Pattern pattern = Pattern.compile(patternStr);
							Matcher matcher = pattern.matcher(body2);
							  
							if (matcher.find())
							{
								length = 70;
							}
							
							String[] textToSend = MainActivity.splitByLength(body2, length);
							
							for (int i = 0; i < textToSend.length; i++)
							{
								ArrayList<String> parts = smsManager.divideMessage(textToSend[i]); 
								
								for (int j = 0; j < parts.size(); j++)
								{
									sPI.add(sentPI);
								}
								
								smsManager.sendMultipartTextMessage(sendTo, null, parts, sPI, null);
							}
						} else
						{
							ArrayList<String> parts = smsManager.divideMessage(body2); 
							
							for (int i = 0; i < parts.size(); i++)
							{
								sPI.add(sentPI);
							}
							
							smsManager.sendMultipartTextMessage(sendTo, null, parts, sPI, null);
						}
					}
				    
				    String address = sendTo;
				    String body2 = messageEntry.getText().toString();
				    
				    if (!sharedPrefs.getString("signature", "").equals(""))
					{
						body2 += "\n" + sharedPrefs.getString("signature", "");
					}
				    
				    Calendar cal = Calendar.getInstance();
				    ContentValues values = new ContentValues();
				    values.put("address", address); 
				    values.put("body", StripAccents.stripAccents(body2)); 
				    values.put("date", cal.getTimeInMillis() + "");
				    values.put("read", 1);
				    getContentResolver().insert(Uri.parse("content://sms/sent"), values);
				    
				    Intent mrIntent = new Intent();
		            mrIntent.setClass(context, QmMarkRead2.class);
		            mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
		            mrIntent.putExtra("body", messageEntry.getText().toString());
		            mrIntent.putExtra("date", cal.getTimeInMillis() + "");
		            mrIntent.putExtra("address", address);
		            startService(mrIntent);
				    
				    ((Activity) context).finish();
				    
				    NotificationManager mNotificationManager =
				            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(1);
					
					writeToFile2(new ArrayList<String>(), context);
					
					context.startService(new Intent(context, ConversationSaverService.class));
					
					Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
				    context.sendBroadcast(intent);
					
					Toast.makeText(context, "Sending Message...", Toast.LENGTH_SHORT).show();
					
					Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
					PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
					AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					alarm.cancel(pStopRepeating);

                    Intent floatingNotifications = new Intent();
                    floatingNotifications.setAction("robj.floating.notifications.dismiss");
                    floatingNotifications.putExtra("package", getPackageName());
                    sendBroadcast(floatingNotifications);

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);
					
					MainActivity.messageRecieved = true;
				}
				
			}
			
		});
		
		readButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 	Intent mrIntent = new Intent();
		            mrIntent.setClass(context, QmMarkRead2.class);
		            startService(mrIntent);
				    
				    ((Activity) context).finish();
				    
				    NotificationManager mNotificationManager =
				            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(1);
					
					writeToFile2(new ArrayList<String>(), context);
					
					Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
				    context.sendBroadcast(intent);
				    
				    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
					PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
					AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					alarm.cancel(pStopRepeating);

                    Intent floatingNotifications = new Intent();
                    floatingNotifications.setAction("robj.floating.notifications.dismiss");
                    floatingNotifications.putExtra("package", getPackageName());
                    sendBroadcast(floatingNotifications);

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);
					
					MainActivity.messageRecieved = true;
				
			}
			
		});
		
		deleteButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				 	Intent deleteIntent = new Intent();
		            deleteIntent.setClass(context, QmDelete.class);
		            startService(deleteIntent);
				    
				    ((Activity) context).finish();
				    
				    NotificationManager mNotificationManager =
				            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
					mNotificationManager.cancel(1);
					
					writeToFile2(new ArrayList<String>(), context);
					
					Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
				    context.sendBroadcast(intent);
				    
				    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
					PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
					AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
					alarm.cancel(pStopRepeating);

                    Intent floatingNotifications = new Intent();
                    floatingNotifications.setAction("robj.floating.notifications.dismiss");
                    floatingNotifications.putExtra("package", getPackageName());
                    sendBroadcast(floatingNotifications);

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);
					
					MainActivity.messageRecieved = true;
				
			}
			
		});
		
		messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
		
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			messageEntry.postDelayed(new Runnable() {
	            @Override
	            public void run() {
	                InputMethodManager keyboard = (InputMethodManager)
	                getSystemService(Context.INPUT_METHOD_SERVICE);
	                keyboard.showSoftInput(messageEntry, 0); 
	            }
	        },200);
		}
		
		messageEntry.addTextChangedListener(new TextWatcher() {
	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	int length = Integer.parseInt(String.valueOf(s.length()));
	        	
	        	if (!sharedPrefs.getString("signature", "").equals(""))
	        	{
	        		length += ("\n" + sharedPrefs.getString("signature", "")).length();
	        	}
	        	
	        	String patternStr = "[^" + MainActivity.GSM_CHARACTERS_REGEX + "]";
				Pattern pattern = Pattern.compile(patternStr);
				Matcher matcher = pattern.matcher(s);
				
				int size = 160;
				
				if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false))
				{
					size = 70;
				}
	        	
	        	int pages = 1;
	        	
	        	while (length > size)
	        	{
	        		length-=size;
	        		pages++;
	        	}
	        	
	            charsRemaining.setText(pages + "/" + (size - length));
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		ImageButton emojiButton = (ImageButton) findViewById(R.id.display_emoji2);
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)messageEntry.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			messageEntry.setLayoutParams(params);
		} else
		{
            if (sharedPrefs.getString("run_as", "sliding").equals("hangout"))
            {
                emojiButton.setImageResource(R.drawable.ic_emoji_dark);
                emojiButton.setColorFilter(context.getResources().getColor(R.color.holo_green));
            }

			emojiButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					AlertDialog.Builder builder = new AlertDialog.Builder(context);
					builder.setTitle("Insert Emojis");
					LayoutInflater inflater = ((Activity) context).getLayoutInflater();
					View frame = inflater.inflate(R.layout.emoji_frame, null);
					
					final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);
					
					final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
					Button okButton = (Button) frame.findViewById(R.id.emoji_ok);
					
					if (sharedPrefs.getBoolean("emoji_type", true))
					{
						emojiGrid.setAdapter(new EmojiAdapter2(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
					} else
					{
						emojiGrid.setAdapter(new EmojiAdapter(context));
						emojiGrid.setOnItemClickListener(new OnItemClickListener() {
						
								public void onItemClick(AdapterView<?> parent, View v, int position, long id)
								{
									editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
									editText.setSelection(editText.getText().length());
								}
						});

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
					}
					
					builder.setView(frame);
					final AlertDialog dialog = builder.create();
					dialog.show();
					
					okButton.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (sharedPrefs.getBoolean("emoji_type", true))
							{
								messageEntry.setText(EmojiConverter2.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							} else
							{
								messageEntry.setText(EmojiConverter.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
								messageEntry.setSelection(messageEntry.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		final String number2 = number;
		
		if (sharedPrefs.getBoolean("enable_view_conversation", false))
		{
			viewConversation.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View arg0) {
					
					Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+number2));
		            intent.setClass(context, com.klinker.android.messaging_donate.MainActivity.class);
		            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            intent.setAction(Intent.ACTION_SENDTO);
			        intent.putExtra("com.klinker.android.OPEN", number2);
		            startActivity(intent);
				}
			
			});
		} else
		{
			viewConversation.setVisibility(View.GONE);
			deleteButton.setVisibility(View.GONE);
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	public InputStream openDisplayPhoto(long contactId, Context context) {
		  Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		     Cursor cursor = context.getContentResolver().query(photoUri,
		          new String[] {Contacts.Photo.PHOTO}, null, null, null);
		     if (cursor == null) {
		         return null;
		     }
		     try {
		         if (cursor.moveToFirst()) {
		             byte[] data = cursor.getBlob(0);
		             if (data != null) {
		                 return new ByteArrayInputStream(data);
		             }
		         }
		     } finally {
		         cursor.close();
		     }
		     return null;
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
	
	private void writeToFile3(ArrayList<String> data, Context context) {
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
