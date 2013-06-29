package com.klinker.android.messaging_sliding.quick_reply;

import android.widget.*;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_donate.StripAccents;
import com.klinker.android.messaging_sliding.ContactSearchArrayAdapter2;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;

public class SendMessage extends Activity {
	
	public ArrayList<String> contactNames, contactNumbers, contactIds, contactTypes;
	public boolean firstContactSearch = true;
	public String inputText;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setFinishOnTouchOutside(false);
		
		Intent intent = getIntent();
		String action = intent.getAction();
		inputText = "";
		
		if (Intent.ACTION_SEND.equals(action))
		{
			try
			{
				inputText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT).toString();
			} catch (Exception e)
			{
				
			}
		}
		
		final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		{
			setTheme(android.R.style.Theme_Holo_Dialog);
		} else
		{
			setTheme(android.R.style.Theme_Holo_Light_Dialog);
		}
		
		LayoutInflater inflater2 = (LayoutInflater) this
		        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		final View newMessageView = inflater2.inflate(R.layout.send_to, (ViewGroup) this.getWindow().getDecorView(), false);
		
		if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		{
			if (sharedPrefs.getBoolean("pitch_black_theme", false))
			{
				newMessageView.setBackgroundColor(getResources().getColor(R.color.black));
			} else
			{
				newMessageView.setBackgroundColor(getResources().getColor(R.color.dark_silver));
			}
		} else
		{
			newMessageView.setBackgroundColor(getResources().getColor(R.color.light_silver));
		}
		
		final TextView mTextView = (TextView) newMessageView.findViewById(R.id.charsRemaining2);
		final EditText mEditText = (EditText) newMessageView.findViewById(R.id.messageEntry2);
		
		mEditText.addTextChangedListener(new TextWatcher() {
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
	        	
	            mTextView.setText(pages + "/" + (size - length));
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		mEditText.setText(inputText);
		
		if (!sharedPrefs.getBoolean("keyboard_type", true))
		{
			mEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
			mEditText.setImeOptions(EditorInfo.IME_ACTION_NONE);
		}
		
		final Context context = (Context) this;
		final EditText contact = (EditText) newMessageView.findViewById(R.id.contactEntry);

		contact.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	if (firstContactSearch)
	        	{
	        		try
	        		{
	        			contactNames = new ArrayList<String>();
	        			contactNumbers = new ArrayList<String>();
	        			contactTypes = new ArrayList<String>();
	        			
	        			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        			String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	        			                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
	        	
	        			Cursor people = getContentResolver().query(uri, projection, null, null, null);
	        	
	        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
	        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	        	
	        			people.moveToFirst();
	        			do {
	        				int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	        				String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
	        				
	        				if (sharedPrefs.getBoolean("mobile_only", false))
	        				{
	        					if (type == 2)
	        					{
	        						contactNames.add(people.getString(indexName));
	    	        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        						contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        					}
	        				} else
	        				{
	        					contactNames.add(people.getString(indexName));
		        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        					contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        				}
	        			} while (people.moveToNext());
	        			people.close();
	        		} catch (IllegalArgumentException e)
	        		{
	        			
	        		}
	        		
	        		firstContactSearch = false;
	        	}
	        }

	        @SuppressLint("DefaultLocale")
			public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	ArrayList<String> searchedNames = new ArrayList<String>();
	        	ArrayList<String> searchedNumbers = new ArrayList<String>();
	        	ArrayList<String> searchedTypes = new ArrayList<String>();
	        	
	        	String text = contact.getText().toString();
	        	
	        	String[] text2 = text.split("; ");
	        	
	        	text = text2[text2.length-1];
	        	
	        	try
	        	{
	        		if (text.substring(0,1).equals("+"))
	        		{
	        			text = "\\" + text;
	        		}
	        	} catch (Exception e)
	        	{
	        		
	        	}
	        	
	  		    Pattern pattern;

                try
                {
                    pattern = Pattern.compile(text.toLowerCase());
                } catch (Exception e)
                {
                    pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
                }
	        	
			    for (int i = 0; i < contactNames.size(); i++)
			    {
			    	try
			    	{
			    		Long.parseLong(text);
			    		
				        if (text.length() <= contactNumbers.get(i).length())
				        {
				        	Matcher matcher = pattern.matcher(contactNumbers.get(i));
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	} catch (Exception e)
			    	{
			    		if (text.length() <= contactNames.get(i).length())
				        {
			    			Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	}
			    }
	        	
		        ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);
		        ContactSearchArrayAdapter2 adapter;
		        
		        if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		        {
		        	searchView.setBackgroundColor(getResources().getColor(R.color.dark_silver));
		        } else
		        {
		        	searchView.setBackgroundColor(getResources().getColor(R.color.light_silver));
		        }
		        
		        if (text.length() != 0)
		        {
	        		adapter = new ContactSearchArrayAdapter2((Activity)context, searchedNames, searchedNumbers, searchedTypes);
		        } else
		        {
	        		adapter = new ContactSearchArrayAdapter2((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		        }
	        	
	        	searchView.setAdapter(adapter);
	        	
	        	searchView.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
						
						String[] t1 = contact.getText().toString().split("; ");
						String string = "";
						
						for (int i = 0; i < t1.length - 1; i++)
						{
							string += t1[i] + "; ";
						}
						
						contact.setText(string + view2.getText() + "; ");
						contact.setSelection(contact.getText().toString().length());
						
					}
	        		
	        	});
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		ImageButton sendButton = (ImageButton) newMessageView.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (contact.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: No valid recipients", Toast.LENGTH_SHORT).show();
				} else if (mEditText.getText().toString().equals(""))
				{
					Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
				} else
				{
					String[] contacts = contact.getText().toString().split("; ");
					
					for (int i = 0; i < contacts.length; i++)
					{
						String body2 = mEditText.getText().toString();
						
						if (!sharedPrefs.getString("signature", "").equals(""))
						{
							body2 += "\n" + sharedPrefs.getString("signature", "");
						}
						
						final String body = body2;
						
						final String address = contacts[i];
						
						new Thread(new Runnable() {
	
							@Override
							public void run() {
								try
								{
									if(sharedPrefs.getBoolean("delivery_reports", false))
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
                                                try {
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

                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                            stackBuilder.addParentStack(MainActivity.class);
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
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(SENT));
								 
								        //---when the SMS has been delivered---
								        registerReceiver(new BroadcastReceiver(){
								            @Override
								            public void onReceive(Context arg0, Intent arg1) {
                                                try {
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
                                                                    ((MainActivity) context).refreshViewPager3();
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
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }
                                                                break;
                                                        }
                                                    } else
                                                    {
                                                        switch (getResultCode())
                                                        {
                                                            case Activity.RESULT_OK:
                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                {
                                                                    Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                                                                }

                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query.moveToFirst())
                                                                {
                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "0");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }

                                                                break;
                                                            case Activity.RESULT_CANCELED:
                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                {
                                                                    Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                                                                }

                                                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                if (query2.moveToFirst())
                                                                {
                                                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                                                    ContentValues values = new ContentValues();
                                                                    values.put("status", "64");
                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                    ((MainActivity) context).refreshViewPager3();
                                                                }
                                                                break;
                                                        }
                                                    }

                                                    context.unregisterReceiver(this);
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(DELIVERED));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
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
												
												smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
												dPI.add(deliveredPI);
											}
											
											smsManager.sendMultipartTextMessage(address, null, parts, sPI, dPI);
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
                                                try {
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

                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                            stackBuilder.addParentStack(MainActivity.class);
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
                                                } catch (Exception e) {

                                                }
								            }
								        }, new IntentFilter(SENT));
								        
								        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
								        
								        String body2 = body;
								        
								        if (sharedPrefs.getBoolean("strip_unicode", false))
								        {
								        	body2 = StripAccents.stripAccents(body2);
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
												
												smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
											}
										} else
										{
											ArrayList<String> parts = smsManager.divideMessage(body2); 
											
											for (int i = 0; i < parts.size(); i++)
											{
												sPI.add(sentPI);
											}
											
											smsManager.sendMultipartTextMessage(address, null, parts, sPI, null);
										}
									}
								} catch (NullPointerException e)
								{
									Toast.makeText(context, "Error sending message", Toast.LENGTH_SHORT).show();
								}
								
								String address2 = address;
							    
							    Calendar cal = Calendar.getInstance();
							    ContentValues values = new ContentValues();
							    values.put("address", address2); 
							    values.put("body", StripAccents.stripAccents(body)); 
							    values.put("date", cal.getTimeInMillis() + "");
							    values.put("read", 1);
							    getContentResolver().insert(Uri.parse("content://sms/sent"), values);

                                if (sharedPrefs.getBoolean("cache_conversations", false)) {
                                    Intent cacheService = new Intent(context, CacheService.class);
                                    context.startService(cacheService);
                                }
								
							    getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
	
									@Override
									public void run() {
										((Activity) context).finish();
									}
							    	
							    });
							}
							
						}).start();
					}
					
					contact.setText("");
					mEditText.setText("");
				}
			}
			
		});
		
		ImageButton emojiButton = (ImageButton) newMessageView.findViewById(R.id.display_emoji);
		
		if (!sharedPrefs.getBoolean("emoji", false))
		{
			emojiButton.setVisibility(View.GONE);
			LayoutParams params = (RelativeLayout.LayoutParams)mEditText.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			mEditText.setLayoutParams(params);
		} else
		{
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
								mEditText.setText(EmojiConverter2.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							} else
							{
								mEditText.setText(EmojiConverter.getSmiledText(context, mEditText.getText().toString() + editText.getText().toString()));
								mEditText.setSelection(mEditText.getText().length());
							}
							
							dialog.dismiss();
						}
						
					});
				}
				
			});
		}
		
		ListView searchView = (ListView) newMessageView.findViewById(R.id.contactSearch);

        try {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
        } catch (Exception e) {
            mEditText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)));
        }
		
		View v1 = newMessageView.findViewById(R.id.view1);
		View v2 = newMessageView.findViewById(R.id.sentBackground);
		
		if (sharedPrefs.getBoolean("dark_theme_quick_reply", true))
		{
			mTextView.setBackgroundColor(getResources().getColor(R.color.black));
			v1.setBackgroundColor(getResources().getColor(R.color.black));
			v2.setBackgroundColor(getResources().getColor(R.color.black));
			sendButton.setBackgroundResource(R.drawable.dark_send_button);
			sendButton.setImageResource(R.drawable.ic_action_send_white);
			searchView.setBackgroundColor(getResources().getColor(R.color.dark_silver));
			emojiButton.setBackgroundColor(getResources().getColor(R.color.black));
		} else
		{
			mTextView.setBackgroundColor(getResources().getColor(R.color.white));
			v1.setBackgroundColor(getResources().getColor(R.color.white));
			v2.setBackgroundColor(getResources().getColor(R.color.white));
			sendButton.setBackgroundResource(R.drawable.light_send_button);
			sendButton.setImageResource(R.drawable.ic_action_send_black);
			searchView.setBackgroundColor(getResources().getColor(R.color.light_silver));
			emojiButton.setBackgroundColor(getResources().getColor(R.color.white));
		}
		
		if (sharedPrefs.getBoolean("custom_font", false))
		{
			mTextView.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
			mEditText.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
			contact.setTypeface(Typeface.createFromFile(sharedPrefs.getString("custom_font_path", "")));
		}
		
		setContentView(newMessageView);
	}

	@Override
	public void onStart()
	{
		super.onStart();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	}
}
