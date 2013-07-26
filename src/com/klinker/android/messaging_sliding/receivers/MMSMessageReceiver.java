package com.klinker.android.messaging_sliding.receivers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.content.*;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.google.android.mms.pdu_alt.*;
import com.klinker.android.messaging_donate.R;

import com.android.mms.transaction.HttpUtils;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu_alt.PduPersister;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SqliteWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Mms;
import android.provider.Telephony.Mms.Inbox;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import com.klinker.android.messaging_sliding.MainActivity;

public class MMSMessageReceiver extends BroadcastReceiver {
    public static String lastReceivedNumber = "";
    public static long lastReceivedTime = Calendar.getInstance().getTimeInMillis();

	public SharedPreferences sharedPrefs;
	public Context context;
	public String phoneNumber;
	public String picNumber;

    public WifiInfo currentWifi;
    public boolean currentWifiState;
    public boolean currentDataState;
	
	public void onReceive(final Context context, Intent intent)
	{
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
		this.context = context;
		String incomingNumber = null;
		
		if(intent.getAction().equals("android.provider.Telephony.WAP_PUSH_RECEIVED")){
            Bundle bundle = intent.getExtras();
            try{
                if (bundle != null){
                    String type = intent.getType();
                    if(type.trim().equalsIgnoreCase("application/vnd.wap.mms-message")){
                        byte[] buffer = bundle.getByteArray("data");
                        incomingNumber = new String(buffer);
                        int indx = incomingNumber.indexOf("/TYPE");
                        if(indx>0 && (indx-15)>0){
                            int newIndx = indx - 15;
                            incomingNumber = incomingNumber.substring(newIndx, indx);
                            indx = incomingNumber.indexOf("+");
                            if(indx>0){
                                incomingNumber = incomingNumber.substring(indx);
                            }
                        }
                    }
                }
            }catch(Exception e){
            }
        }

		if (incomingNumber != null)
		{
			MainActivity.messageRecieved = true;
			
			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "download MMS");
			wakeLock.acquire(5000);
			
			boolean error = false;
			
			if (sharedPrefs.getBoolean("override_stock", false) && !sharedPrefs.getBoolean("receive_with_stock", false))
			{
				byte[] pushData = intent.getByteArrayExtra("data");
	            PduParser parser = new PduParser(pushData);
	            GenericPdu pdu = parser.parse();
	            
	            if (null == pdu)
	            {
	            	return;
	            }
	            
	            int type = pdu.getMessageType();
	            long threadId = -1;
	
	            PduPersister p = PduPersister.getPduPersister(context);
	            
	            try {
	            	boolean groupMMS = false;
	            	
	            	if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false))
	            	{
	            		groupMMS = true;
	            	}
	            	
	            	switch (type)
	            	{
		            	case PduHeaders.MESSAGE_TYPE_DELIVERY_IND:
		            	case PduHeaders.MESSAGE_TYPE_READ_ORIG_IND:
		            	{
		            		threadId = findThreadId(context, pdu, type);
		            		
		            		if (threadId == -1)
		            		{
		            			break;
		            		}
		            		
		            		Uri uri = p.persist(pdu, Inbox.CONTENT_URI, true, groupMMS, null);
							ContentValues values = new ContentValues(1);
		                    values.put(Mms.THREAD_ID, threadId);
		                    SqliteWrapper.update(context, context.getContentResolver(), uri, values, null, null);
		                    break;
		            	}
		            	case PduHeaders.MESSAGE_TYPE_NOTIFICATION_IND:
		            	{
		            		p.persist(pdu, Inbox.CONTENT_URI, true, groupMMS, null);
		            		break;
		            	}
		            	default:
		            		Log.v("MMS Error", "Non recognized pdu_alt header - " + type);
	            	}
                    
				} catch (MmsException e1) {
					e1.printStackTrace();
					error = true;
				}
			}
            
			incomingNumber = incomingNumber.replace("+1", "").replace("+", "").replace("-", "").replace(" ", "").replace("(","").replace(")","");
			String mmsFrom = incomingNumber;
			picNumber = incomingNumber;

            if (lastReceivedNumber.equals(picNumber) && Calendar.getInstance().getTimeInMillis() < lastReceivedTime + (1000 * 10)) {
                return;
            }

            lastReceivedNumber = picNumber;
            lastReceivedTime = Calendar.getInstance().getTimeInMillis();
			
			try
	        {
				String[] incomingNumbers = incomingNumber.split(" ");
				Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumbers[0]));
				Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
		
				if(phonesCursor != null && phonesCursor.moveToFirst()) {
					mmsFrom = phonesCursor.getString(0);
				} else
				{
					Locale sCachedLocale = Locale.getDefault();
					int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
					Editable editable = new SpannableStringBuilder(incomingNumber);
					PhoneNumberUtils.formatNumber(editable, sFormatType);
					mmsFrom = editable.toString();
				}
				
				phonesCursor.close();
			
				ArrayList<String> newMessages = readFromFile(context);
				boolean flag = false;
				
				for (int i = 0; i < newMessages.size(); i++)
				{
					if (mmsFrom.equals(newMessages.get(i)))
					{
						flag = true;
					}
				}
				
				if (flag == false)
				{
					newMessages.add(mmsFrom);
				}
				
				writeToFile(newMessages, context);
	        } catch (IllegalArgumentException e)
	        {
                e.printStackTrace();
	        } catch (Exception e) {
                error = true;
            }
			
			phoneNumber = mmsFrom;
			
			if (!sharedPrefs.getBoolean("auto_download_mms", false) || !sharedPrefs.getBoolean("enable_mms", false) || sharedPrefs.getBoolean("receive_with_stock", false))
			{
				if (sharedPrefs.getBoolean("secure_notification", false))
				{
					makeNotification("New Picture Message", "", null);
				} else
				{
					makeNotification("New Picture Message", mmsFrom, null);
				}
			} else
			{
					new Thread(new Runnable() {

						@Override
						public void run() {
							
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e1) {
								e1.printStackTrace();
							}

                            if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                            {
                                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                currentWifi = wifi.getConnectionInfo();
                                currentWifiState = wifi.isWifiEnabled();
                                wifi.disconnect();
                                currentDataState = MainActivity.isMobileDataEnabled(context);
                                MainActivity.setMobileDataEnabled(context, true);
                            }
							
							Cursor locationQuery = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"ct_l", "thread_id", "_id"}, null, null, "date desc");
							locationQuery.moveToFirst();
							String downloadLocation = locationQuery.getString(locationQuery.getColumnIndex("ct_l"));
							String threadId = locationQuery.getString(locationQuery.getColumnIndex("thread_id"));
							String msgId = locationQuery.getString(locationQuery.getColumnIndex("_id"));
							locationQuery.close();
							
							try
							{
								ContentValues value = new ContentValues();
								value.put("read", true);
								context.getContentResolver().update(Uri.parse("content://mms/inbox"), value, "_id=" + msgId, null);
							} catch (Exception e)
							{
								e.printStackTrace();
							}
							
							List<APN> apns = new ArrayList<APN>();
							
							try
							{
								APNHelper helper = new APNHelper(context);
								apns = helper.getMMSApns();
								
							} catch (Exception e)
							{
								APN apn = new APN(sharedPrefs.getString("mmsc_url", ""), sharedPrefs.getString("mms_port", ""), sharedPrefs.getString("mms_proxy", ""));
								apns.add(apn);
								
								String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
								apns.get(0).MMSCenterUrl = mmscUrl;
								
								try
								{
									if (sharedPrefs.getBoolean("apn_username_password", false))
									{
										if (!sharedPrefs.getString("apn_username", "").equals("") && !sharedPrefs.getString("apn_username", "").equals(""))
										{
											String mmsc = apns.get(0).MMSCenterUrl;
											String[] parts = mmsc.split("://");
											String newMmsc = parts[0] + "://";
											
											newMmsc += sharedPrefs.getString("apn_username", "") + ":" + sharedPrefs.getString("apn_password", "") + "@";
											
											for (int i = 1; i < parts.length; i++)
											{
												newMmsc += parts[i];
											}
											
											apns.set(0, new APN(newMmsc, apns.get(0).MMSPort, apns.get(0).MMSProxy));
										}
									}
								} catch (Exception f)
								{
									
								}
							}
							
							
							byte[] resp;
							try {
								resp = HttpUtils.httpConnection(
								        context, SendingProgressTokenManager.NO_TOKEN,
								        downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
								        !TextUtils.isEmpty(apns.get(0).MMSProxy),
								        apns.get(0).MMSProxy,
								        Integer.parseInt(apns.get(0).MMSPort));
								
								RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
								PduPersister persister = PduPersister.getPduPersister(context);
								Uri msgUri = persister.persist(retrieveConf, Inbox.CONTENT_URI, true,
				                        true, null);
								ContentValues values = new ContentValues(1);
				                values.put(Mms.DATE, System.currentTimeMillis() / 1000L);
				                SqliteWrapper.update(context, context.getContentResolver(),
				                        msgUri, values, null, null);
				                SqliteWrapper.delete(context, context.getContentResolver(),
				                		Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadId, msgId});
							} catch (Exception e)
							{
                                e.printStackTrace();

								if (sharedPrefs.getBoolean("secure_notification", false))
								{
									makeNotification("New Picture Message", "", null);
								} else
								{
									makeNotification("New Picture Message", phoneNumber, null);
								}

                                if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                                {
                                    WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                    wifi.setWifiEnabled(false);
                                    wifi.setWifiEnabled(currentWifiState);
                                    Log.v("Reconnect", "" + wifi.reconnect());
                                    MainActivity.setMobileDataEnabled(context, currentDataState);
                                }
							}
							
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
			                
			                Cursor query = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"_id"}, null, null, null);
			                query.moveToFirst();
			                
			                String selectionPart = "mid=" + query.getString(query.getColumnIndex("_id"));
				        	Uri uri = Uri.parse("content://mms/part");
				        	Cursor cursor = context.getContentResolver().query(uri, null, selectionPart, null, null);
				        	
				        	String body = "";
				        	String image = "";
				        	
				        	if (cursor.moveToFirst()) {
				        	    do {
				        	        String partId = cursor.getString(cursor.getColumnIndex("_id"));
				        	        String type = cursor.getString(cursor.getColumnIndex("ct"));
				        	        String body2 = "";
				        	        if ("text/plain".equals(type)) {
				        	            String data = cursor.getString(cursor.getColumnIndex("_data"));
				        	            if (data != null) {
				        	                body2 = getMmsText(partId, (Activity) context);
				        	                body += body2;
				        	            } else {
				        	                body2 = cursor.getString(cursor.getColumnIndex("text"));
				        	                body += body2;
				        	                
				        	            }
				        	        }
				        	        
				        	        if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
				        	                "image/gif".equals(type) || "image/jpg".equals(type) ||
				        	                "image/png".equals(type)) {
				        	        	if (image == null)
				        	        	{
				        	        		image = "content://mms/part/" + partId;
				        	        	} else
				        	        	{
				        	        		image += " content://mms/part/" + partId;
				        	        	}
				        	        }
				        	    } while (cursor.moveToNext());
				        	}
				        	
				        	String images[] = image.trim().split(" ");
				        	
				        	if (sharedPrefs.getBoolean("secure_notification", false))
				        	{
				        		makeNotification("New MMS Message", "", null);
				        	} else
				        	{
				        		if (images[0].trim().equals(""))
				        		{
				        			makeNotification(phoneNumber, body, null);
				        		} else
				        		{
					        		Bitmap b = decodeFile(new File(getRealPathFromURI(Uri.parse(images[0].trim()))));
					        		makeNotification(phoneNumber, body, b);
				        		}
				        	}

                            if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                            {
                                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                wifi.setWifiEnabled(false);
                                wifi.setWifiEnabled(currentWifiState);
                                Log.v("Reconnect", "" + wifi.reconnect());
                            }
							
						}
						
					}).start();
			}

            if (!sharedPrefs.getBoolean("receive_with_stock", false))
            {
                error = true;
            }

            if (sharedPrefs.getBoolean("cache_conversations", false)) {
                Intent cacheService = new Intent(context, CacheService.class);
                context.startService(cacheService);
            }
			
			if (sharedPrefs.getBoolean("override_stock", false) && !error)
			{
				abortBroadcast();
			} else
			{
				clearAbortBroadcast();
			}
		}
	}
	
	private void makeNotification(String title, String text, Bitmap image)
	{
		if (sharedPrefs.getBoolean("notifications", true))
		{
	        NotificationCompat.Builder mBuilder =
	                new NotificationCompat.Builder(context)
	                .setSmallIcon(R.drawable.stat_notify_mms)
	                .setContentTitle(title)
	                .setContentText(text);
	        
	        if (!sharedPrefs.getBoolean("secure_notification", false))
	        {
	        	try
	        	{
	        		mBuilder.setLargeIcon(getFacebookPhoto(picNumber));
	        	} catch (Exception e)
	        	{
	        		
	        	}
	        }
	        
	        String notIcon = sharedPrefs.getString("notification_icon", "white");
	        
	        if (notIcon.equals("white"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms);
	        } else if (notIcon.equals("blue"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms_blue);
	        } else if (notIcon.equals("green"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms_green);
	        } else if (notIcon.equals("orange"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms_orange);
	        } else if (notIcon.equals("purple"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms_purple);
	        } else if (notIcon.equals("red"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_mms_red);
	        } else if (notIcon.equals("icon"))
	        {
	        	mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
	        }
	        
	        Intent resultIntent = new Intent(Intent.ACTION_MAIN);
	        resultIntent.setType("vnd.android-dir/mms-sms");
	
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
	        
	        if (sharedPrefs.getBoolean("vibrate", true))
	        {
	        	if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false))
	        	{
		        	String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");
		        	
		        	if (vibPat.equals("short"))
		        	{
		        		long[] pattern = {0L, 400L};
			        	mBuilder.setVibrate(pattern);
		        	} else if (vibPat.equals("long"))
		        	{
		        		long[] pattern = {0L, 800L};
		        		mBuilder.setVibrate(pattern);
		        	} else if (vibPat.equals("2short"))
		        	{
		        		long[] pattern = {0L, 400L, 100L, 400L};
		        		mBuilder.setVibrate(pattern);
		        	} else if (vibPat.equals("2long"))
		        	{
		        		long[] pattern = {0L, 800L, 200L, 800L};
		        		mBuilder.setVibrate(pattern);
		        	} else if (vibPat.equals("3short"))
		        	{
		        		long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
		        		mBuilder.setVibrate(pattern);
		        	} else if (vibPat.equals("3long"))
		        	{
		        		long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
		        		mBuilder.setVibrate(pattern);
		        	}
	        	} else
	        	{
	        		try
	        		{
		        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
		        		long[] pattern = new long[vibPat.length];
		        		
		        		for (int i = 0; i < vibPat.length; i++)
		        		{
		        			pattern[i] = Long.parseLong(vibPat[i]);
		        		}
		        		
		        		mBuilder.setVibrate(pattern);
	        		} catch (Exception e)
	        		{
	        			
	        		}
	        	}
	        }
	        
	        if (sharedPrefs.getBoolean("led", true))
	        {
		        String ledColor = sharedPrefs.getString("led_color", "white");
		        int ledOn = sharedPrefs.getInt("led_on_time", 1000);
		        int ledOff = sharedPrefs.getInt("led_off_time", 2000);
		        
		        if (ledColor.equalsIgnoreCase("white"))
		        {
		        	mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
		        } else if (ledColor.equalsIgnoreCase("blue"))
		        {
		        	mBuilder.setLights(0xFF33B5E5, ledOn, ledOff);
		        } else if (ledColor.equalsIgnoreCase("green"))
		        {
		        	mBuilder.setLights(0xFF00FF00, ledOn, ledOff);
		        } else if (ledColor.equalsIgnoreCase("orange"))
		        {
		        	mBuilder.setLights(0xFFFF8800, ledOn, ledOff);
		        } else if (ledColor.equalsIgnoreCase("red"))
		        {
		        	mBuilder.setLights(0xFFCC0000, ledOn, ledOff);
		        } else if (ledColor.equalsIgnoreCase("purple"))
		        {
		        	mBuilder.setLights(0xFFAA66CC, ledOn, ledOff);
		        } else
		        {
		        	mBuilder.setLights(0xFFFFFFFF, ledOn, ledOff);
		        }
	        }
	        
	        try
	        {
	        	mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
	        } catch(Exception e)
	        {
	        	mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
	        }
	        
	        NotificationManager mNotificationManager =
	            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
	        
	        Notification notification;
	        
	        if (image != null && !sharedPrefs.getBoolean("secure_notification", false))
	        {
		        NotificationCompat.BigPictureStyle picBuilder = new NotificationCompat.BigPictureStyle(mBuilder);
		        
		        try
		        {
		        	picBuilder.bigPicture(image);
		        } catch (Exception e)
		        {
		        	
		        }
		        
		        notification = picBuilder.build();
	        } else
	        {
	        	notification = mBuilder.build();
	        }
	        
	        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
	        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
	        mNotificationManager.notify(2, notification);
	        
	        Intent updateWidget = new Intent("com.klinker.android.messaging.RECEIVED_MMS");
			context.sendBroadcast(updateWidget);
			
			Intent newMms = new Intent("com.klinker.android.messaging.NEW_MMS");
			context.sendBroadcast(newMms);
			
			if (!sharedPrefs.getString("repeating_notification", "none").equals("none"))
            {
            	Calendar cal = Calendar.getInstance();
            	
            	Intent repeatingIntent = new Intent(context, NotificationRepeaterService.class);
            	PendingIntent pRepeatingIntent = PendingIntent.getService(context, 0, repeatingIntent, 0);
            	AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            	alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), pRepeatingIntent);
            }

            if (sharedPrefs.getBoolean("cache_conversations", false)) {
                Intent cacheService = new Intent(context, CacheService.class);
                context.startService(cacheService);
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
	
	public Bitmap getFacebookPhoto(String phoneNumber) {
	    Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
	    Uri photoUri = null;
	    ContentResolver cr = context.getContentResolver();
	    Cursor contact = cr.query(phoneUri,
	            new String[] { ContactsContract.Contacts._ID }, null, null, null);

	    try
	    {
		    if (contact.moveToFirst()) {
		        long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
		        photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
	
		    }
		    else {
		    	
		        return null;
		    }
		    if (photoUri != null) {
		        InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
		                cr, photoUri);
		        if (input != null) {
		        	contact.close();
		            return BitmapFactory.decodeStream(input);
		        }
		    } else {
		    	
		        return null;
		    }
		    return null;
	    } catch (Exception e)
	    {
	    	return null;
	    }
	}
	
	private static String getMmsText(String id, Activity context) {
	    Uri partURI = Uri.parse("content://mms/part/" + id);
	    InputStream is = null;
	    StringBuilder sb = new StringBuilder();
	    try {
	        is = context.getContentResolver().openInputStream(partURI);
	        if (is != null) {
	            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
	            BufferedReader reader = new BufferedReader(isr);
	            String temp = reader.readLine();
	            while (temp != null) {
	                sb.append(temp);
	                temp = reader.readLine();
	            }
	        }
	    } catch (IOException e) {}
	    finally {
	        if (is != null) {
	            try {
	                is.close();
	            } catch (IOException e) {}
	        }
	    }
	    return sb.toString();
	}
	
	private Bitmap decodeFile(File f){
	    try {
	        //Decode image size
	        BitmapFactory.Options o = new BitmapFactory.Options();
	        o.inJustDecodeBounds = true;
	        BitmapFactory.decodeStream(new FileInputStream(f),null,o);

	        //The new size we want to scale to
	        final int REQUIRED_SIZE=200;

	        //Find the correct scale value. It should be the power of 2.
	        int scale=1;
	        while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
	            scale*=2;

	        //Decode with inSampleSize
	        BitmapFactory.Options o2 = new BitmapFactory.Options();
	        o2.inSampleSize=scale;
	        return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
	    } catch (FileNotFoundException e) {e.printStackTrace();}
	    return null;
	}
	
	public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }
	
	private static long findThreadId(Context context, GenericPdu pdu, int type) {
        String messageId;

        if (type == PduHeaders.MESSAGE_TYPE_DELIVERY_IND) {
            messageId = new String(((DeliveryInd) pdu).getMessageId());
        } else {
            messageId = new String(((ReadOrigInd) pdu).getMessageId());
        }

        StringBuilder sb = new StringBuilder('(');
        sb.append(Mms.MESSAGE_ID);
        sb.append('=');
        sb.append(DatabaseUtils.sqlEscapeString(messageId));
        sb.append(" AND ");
        sb.append(Mms.MESSAGE_TYPE);
        sb.append('=');
        sb.append(PduHeaders.MESSAGE_TYPE_SEND_REQ);

        Cursor cursor = SqliteWrapper.query(context, context.getContentResolver(),
                            Mms.CONTENT_URI, new String[] { Mms.THREAD_ID },
                            sb.toString(), null, null);
        if (cursor != null) {
            try {
                if ((cursor.getCount() == 1) && cursor.moveToFirst()) {
                    return cursor.getLong(0);
                }
            } finally {
                cursor.close();
            }
        }

        return -1;
    }
}