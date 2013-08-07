package com.klinker.android.messaging_sliding.receivers;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.MainActivityPopup;
import com.klinker.android.messaging_sliding.quick_reply.QmDelete;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.klinker.android.messaging_sliding.blacklist.BlacklistContact;
import com.klinker.android.messaging_sliding.notifications.IndividualSetting;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import com.klinker.android.messaging_sliding.quick_reply.QuickReply;

@SuppressWarnings("deprecation")
public class TextMessageReceiver extends BroadcastReceiver {
	public static final String SMS_EXTRA_NAME = "pdus";
	private static final String FILENAME = "newMessages.txt";
	public SharedPreferences sharedPrefs;
	
	@SuppressLint("Wakelock")
	public void onReceive(final Context context, Intent intent)
	{
		try
		{
	        Bundle extras = intent.getExtras();
	         
	        String body = "";
	        String address = "";
	        String name = "";
	        String id;
	        String date = "";
            String dateReceived;
	         
	        if ( extras != null )
	        {
	            Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );
	            
	            for ( int i = 0; i < smsExtra.length; ++i )
	            {
	                SmsMessage sms = SmsMessage.createFromPdu((byte[])smsExtra[i]);
	                 
	                body += sms.getMessageBody().toString();
	                address = sms.getOriginatingAddress();
	                date = sms.getTimestampMillis() + "";
	            }
            }

            Calendar cal = Calendar.getInstance();
            dateReceived = cal.getTimeInMillis() + "";

            final String origBody = body;
            final String origDate = dateReceived;
            final String origAddress = address;
	        
	        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
	        
	        ArrayList<BlacklistContact> blacklist = readFromFile5(context);
	        int blacklistType = 0;
	        
	        for (int i = 0; i < blacklist.size(); i++)
	        {
	        	if (blacklist.get(i).name.equals(address.replace("-", "").replace("(", "").replace(")", "").replace(" ", "").replace("+1", "")))
	        	{
	        		blacklistType = blacklist.get(i).type;
	        	}
	        }
	        
	        if (blacklistType == 2)
	        {
	        	abortBroadcast();
	        } else
	        {
		        if (sharedPrefs.getBoolean("override", false))
		        {
		        	ContentValues values = new ContentValues();
			        values.put("address", address);
			        values.put("body", body);
			        values.put("date", dateReceived);
			        values.put("read", "0");
			        values.put("date_sent", date);
			        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		        }
		        
		        if (blacklistType != 1)
		        {
			        Bundle bundle = new Bundle();
			        bundle.putString("body", body);
			        bundle.putString("date", date);
			        bundle.putString("address", address);
			        
			        String origin = address.replaceAll("[^0-9\\+]", "");
					
			        try
			        {
						Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
						Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
				
						if(phonesCursor != null && phonesCursor.moveToFirst()) {
							name = phonesCursor.getString(0);
						} else
						{
							Locale sCachedLocale = Locale.getDefault();
							int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
							Editable editable = new SpannableStringBuilder(address);
							PhoneNumberUtils.formatNumber(editable, sFormatType);
							name = editable.toString();
						}
						
						phonesCursor.close();
					
						ArrayList<String> newMessages = readFromFile(context);
						boolean flag = false;
						
						for (int i = 0; i < newMessages.size(); i++)
						{
							if (name.equals(newMessages.get(i)))
							{
								flag = true;
							}
						}
						
						if (flag == false)
						{
							newMessages.add(name);
						}
						
						writeToFile(newMessages, context);
						
						phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts._ID}, null, null, null);
				
						if(phonesCursor != null && phonesCursor.moveToFirst()) {
							id = phonesCursor.getString(0);
						} else
							id = "0";
						
						phonesCursor.close();
			        } catch (IllegalArgumentException e)
			        {
			        	name = address;
			        	id = "0";
			        }
				    
				    InputStream input = openDisplayPhoto(Long.parseLong(id), context);
					
					if (input == null)
					{
						input = context.getResources().openRawResource(R.drawable.ic_contact_picture);
					}
					
					Bitmap contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 120, 120, true);
					
					Intent intent2 = new Intent(context, com.klinker.android.messaging_card.QuickReply.class);
                    intent2.putExtra("address", origAddress);
                    intent2.putExtra("body", origBody);
                    intent2.putExtra("date", origDate);
					
					if (sharedPrefs.getBoolean("use_old_popup", false))
					{
						intent2 = new Intent(context, QuickReply.class);
					}

                    if (sharedPrefs.getBoolean("full_app_popup", true)) {
                        intent2 = new Intent(context, MainActivityPopup.class);
                    }

                    if (sharedPrefs.getBoolean("halo_popup", false)) {
                        intent2 = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

                        try
                        {
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                        } catch (Exception e)
                        {
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }

                    }
					
					intent2.putExtra("notification", "true");
					PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_CANCEL_CURRENT);
			
					if (sharedPrefs.getBoolean("notifications", true))
					{
						if (sharedPrefs.getBoolean("wake_screen", false))
						{
							PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				            final WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
				            wakeLock.acquire(Long.parseLong(sharedPrefs.getString("screen_timeout", "5"))*1000);
						}
						
				        ArrayList<String> prevNotifications = readFromFile2(context);
				        
				        Intent callIntent = new Intent(Intent.ACTION_CALL);
				        callIntent.setData(Uri.parse("tel:"+address));
				        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				        PendingIntent callPendingIntent = PendingIntent.getActivity(context, 0, callIntent, PendingIntent.FLAG_UPDATE_CURRENT);
				        
				        Intent mrIntent = new Intent();
			            mrIntent.setClass(context, QmMarkRead2.class);
			            PendingIntent mrPendingIntent = PendingIntent.getService(context, 0, mrIntent,
			                    PendingIntent.FLAG_UPDATE_CURRENT);
			            
			            Intent deleterIntent = new Intent();
			            deleterIntent.setClass(context, QmDelete.class);
			            PendingIntent deletePendingIntent = PendingIntent.getService(context, 0, deleterIntent,
			                    PendingIntent.FLAG_UPDATE_CURRENT);
				        
			            if (!sharedPrefs.getBoolean("secure_notification", false))
			            {
					        if (prevNotifications.size() == 0)
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle(name)
						                .setContentText(body)
						                .setTicker(name + ": " + body);
						        
						        if (!id.equals("0"))
						        	mBuilder.setLargeIcon(contactImage);
						        
						        setIcon(mBuilder);
						        
						        HashSet<String> set = new HashSet<String>();
						        set.add("1");
						        set.add("2");
						        set.add("3");
						        set.add("4");
						        
						        Set<String> buttons = sharedPrefs.getStringSet("button_options", set);
						        
						        int[] buttonArray = new int[buttons.size()];
						        
						        for (int i = 0; i < buttons.size(); i++)
						        {
						        	buttonArray[i] = Integer.parseInt((String) buttons.toArray()[i]);
						        }
						        
						        Arrays.sort(buttonArray);
						        
						        for (int i = 0; i < buttonArray.length; i++)
						        {
						        	String[] labels = context.getResources().getStringArray(R.array.quickReplyOptions);
						        	
						        	int option = buttonArray[i];
						        	
						        	if (option == 1)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_msg_compose_holo_dark, labels[0], pIntent);
						        	} else if (option == 2)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_done_holo_dark, labels[1], mrPendingIntent);
						        	} else if (option == 3)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_call, labels[2], callPendingIntent);
						        	} else if (option == 4)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_delete, labels[3], deletePendingIntent);
						        	}
						        }
						        
						        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
						        resultIntent.setAction(Intent.ACTION_SENDTO);
						        resultIntent.putExtra("com.klinker.android.OPEN", address);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
						                    PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context))
						        {
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
								        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
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
								        	mBuilder.setLights(0xFF0099CC, ledOn, ledOff);
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
						        }
						        
						        NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						        
						        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
						        mNotificationManager.notify(1, notification);
						        
						        ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        writeToFile2(newNotifications, context);
					        } else if (prevNotifications.size() == 1 && prevNotifications.get(0).startsWith(name))
					        {
					        	String body2 = prevNotifications.get(0);
					        	
					        	for (int i = 0; i < body2.length() - 1; i++)
					        	{
					        		if (body2.substring(i, i+1).equals(":"))
					        		{
					        			body2 = body2.substring(i+1);
					        			break;
					        		}
					        	}
					        	
					        	body = body2 + " | " + body;
					        	
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle(name)
						                .setContentText(body)
						                .setTicker(name + ": " + body);
						        
						        if (!id.equals("0"))
						        	mBuilder.setLargeIcon(contactImage);

                                setIcon(mBuilder);
						        
						        HashSet<String> set = new HashSet<String>();
						        set.add("1");
						        set.add("2");
						        set.add("3");
						        set.add("4");
						        
						        Set<String> buttons = sharedPrefs.getStringSet("button_options", set);
						        
						        int[] buttonArray = new int[buttons.size()];
						        
						        for (int i = 0; i < buttons.size(); i++)
						        {
						        	buttonArray[i] = Integer.parseInt((String) buttons.toArray()[i]);
						        }
						        
						        Arrays.sort(buttonArray);
						        
						        for (int i = 0; i < buttonArray.length; i++)
						        {
						        	String[] labels = context.getResources().getStringArray(R.array.quickReplyOptions);
						        	
						        	int option = buttonArray[i];
						        	
						        	if (option == 1)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_msg_compose_holo_dark, labels[0], pIntent);
						        	} else if (option == 2)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_done_holo_dark, labels[1], mrPendingIntent);
						        	} else if (option == 3)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_call, labels[2], callPendingIntent);
						        	}
						        }
						        
						        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context))
						        {
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
								        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
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
						        }
						        
						        NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						        
						        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
						        mNotificationManager.notify(1, notification);
						        
						        ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        writeToFile2(newNotifications, context);
					        } else
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle(prevNotifications.size() + 1 + " New Messages")
						                .setTicker(prevNotifications.size() + 1 + " New Messages");

                                setIcon(mBuilder);
					        	
					        	HashSet<String> set = new HashSet<String>();
						        set.add("1");
						        set.add("2");
						        set.add("3");
						        set.add("4");
						        
						        Set<String> buttons = sharedPrefs.getStringSet("button_options", set);
						        
						        int[] buttonArray = new int[buttons.size()];
						        
						        for (int i = 0; i < buttons.size(); i++)
						        {
						        	buttonArray[i] = Integer.parseInt((String) buttons.toArray()[i]);
						        }
						        
						        Arrays.sort(buttonArray);
						        
						        for (int i = 0; i < buttonArray.length; i++)
						        {
						        	String[] labels = context.getResources().getStringArray(R.array.quickReplyOptions);
						        	
						        	int option = buttonArray[i];
						        	
						        	if (option == 1)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_msg_compose_holo_dark, labels[0], pIntent);
						        	} else if (option == 2)
						        	{
						        		mBuilder.addAction(R.drawable.ic_menu_done_holo_dark, labels[1], mrPendingIntent);
						        		break;
						        	}
						        }
						        
						        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context))
						        {
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
								        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
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
						        }
						        
						        NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						        
						        NotificationCompat.InboxStyle notification2 = new NotificationCompat.InboxStyle(mBuilder);
						        
						        prevNotifications.add(name + ": " + body);
						        
						        for (int i = 0; i < prevNotifications.size(); i++)
						        {
						        	notification2.addLine(prevNotifications.get(i));
						        }
						        
						        notification2.setSummaryText(prevNotifications.size() + " New Messages");
						        
						        Notification notification = notification2.build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
						        mNotificationManager.notify(1, notification);
						        
						        writeToFile2(prevNotifications, context);
					        }
			            } else
			            {
			            	if (prevNotifications.size() == 0)
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle("New Message")
						                .setTicker("New Message")
						                .setContentText("");

                                setIcon(mBuilder);
						        
						        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
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
							        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
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
						        
						        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
						        mNotificationManager.notify(1, notification);
						        
						        ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        writeToFile2(newNotifications, context);
					        } else
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle("New Messages")
						                .setTicker("New Messages")
						                .setContentText("");

                                setIcon(mBuilder);
						        
						        Intent resultIntent = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(com.klinker.android.messaging_donate.MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
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
							        		String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 400, 100, 400").replace("L", "").split(", ");
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
						        
						        NotificationCompat.InboxStyle notification2 = new NotificationCompat.InboxStyle(mBuilder);
						        
						        prevNotifications.add(name + ": " + body);
						        
						        notification2.setSummaryText(prevNotifications.size() + " New Messages");
						        
						        Notification notification = notification2.build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
						        mNotificationManager.notify(1, notification);
						        
						        writeToFile2(prevNotifications, context);
					        }
			            }
			            
			            if (!sharedPrefs.getString("repeating_notification", "none").equals("none"))
			            {
			            	cal = Calendar.getInstance();
			            	
			            	Intent repeatingIntent = new Intent(context, NotificationRepeaterService.class);
			            	PendingIntent pRepeatingIntent = PendingIntent.getService(context, 0, repeatingIntent, 0);
			            	AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
			            	alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis() + Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), Long.parseLong(sharedPrefs.getString("repeating_notification", "none")), pRepeatingIntent);
			            }
					}
		        }
		        
		        MainActivity.messageRecieved = true;
		        
		        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
				context.sendBroadcast(updateWidget);
				
				Intent updateHalo = new Intent("com.klinker.android.messaging.UPDATE_HALO");
				updateHalo.putExtra("name", name);
				updateHalo.putExtra("message", body);
				context.sendBroadcast(updateHalo);

                if (sharedPrefs.getBoolean("cache_conversations", false)) {
                    Intent cacheService = new Intent(context, CacheService.class);
                    context.startService(cacheService);
                }
		        
		        if (!isRunning(context) && blacklistType != 1)
		        {
		        	Handler handler = new Handler();
		        	handler.postDelayed(new Runnable() {
	
						@Override
						public void run() {
							if (sharedPrefs.getBoolean("popup_reply", false) && !sharedPrefs.getBoolean("secure_notification", false))
					        {
					        	Intent intent3 = new Intent(context, com.klinker.android.messaging_card.QuickReply.class);
                                intent3.putExtra("address", origAddress);
                                intent3.putExtra("body", origBody);
                                intent3.putExtra("date", origDate);
					        	
					        	if (sharedPrefs.getBoolean("use_old_popup", false))
								{
									intent3 = new Intent(context, QuickReply.class);
								}

								if (sharedPrefs.getBoolean("halo_popup", false) || sharedPrefs.getBoolean("full_app_popup", true))
								{
									boolean halo = sharedPrefs.getBoolean("halo_popup", false);
									
									if (halo) {
										intent3 = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);
									} else {
										intent3 = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);
									}

									try
									{
										if (halo) {
											intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
										} else {
											intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
										}
									} catch (Exception e)
									{
										intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									}

                                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                                    if (pm.isScreenOn())
                                    {
                                        final Intent popup = intent3;

                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                context.startActivity(popup);
                                            }
                                        }, 250);
                                    } else
                                    {
                                        UnlockReceiver.openApp = true;
                                    }
								} else
								{
									intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent3);
								}
					        }
							
						}
		        		
		        	}, 200);
		        }
		        
		        if (sharedPrefs.getBoolean("override", false))
		        {
		        	this.abortBroadcast();
		        }
	        }
		} catch (Exception e)
		{
			
		}
	}

    public void setIcon(NotificationCompat.Builder mBuilder)
    {
        if (!sharedPrefs.getBoolean("breath", false))
        {
            String notIcon = sharedPrefs.getString("notification_icon", "white");
            int notImage = Integer.parseInt(sharedPrefs.getString("notification_image", "1"));

            switch (notImage)
            {
                case 1:
                    if (notIcon.equals("white"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms);
                    } else if (notIcon.equals("blue"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_blue);
                    } else if (notIcon.equals("green"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_green);
                    } else if (notIcon.equals("orange"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_orange);
                    } else if (notIcon.equals("purple"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_purple);
                    } else if (notIcon.equals("red"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_red);
                    } else if (notIcon.equals("icon"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 2:
                    if (notIcon.equals("white"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble);
                    } else if (notIcon.equals("blue"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_blue);
                    } else if (notIcon.equals("green"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_green);
                    } else if (notIcon.equals("orange"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_orange);
                    } else if (notIcon.equals("purple"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_purple);
                    } else if (notIcon.equals("red"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_bubble_red);
                    } else if (notIcon.equals("icon"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 3:
                    if (notIcon.equals("white"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point);
                    } else if (notIcon.equals("blue"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_blue);
                    } else if (notIcon.equals("green"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_green);
                    } else if (notIcon.equals("orange"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_orange);
                    } else if (notIcon.equals("purple"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_purple);
                    } else if (notIcon.equals("red"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_point_red);
                    } else if (notIcon.equals("icon"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 4:
                    if (notIcon.equals("white"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane);
                    } else if (notIcon.equals("blue"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_blue);
                    } else if (notIcon.equals("green"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_green);
                    } else if (notIcon.equals("orange"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_orange);
                    } else if (notIcon.equals("purple"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_purple);
                    } else if (notIcon.equals("red"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_airplane_red);
                    } else if (notIcon.equals("icon"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }

                    break;
                case 5:
                    if (notIcon.equals("white"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud);
                    } else if (notIcon.equals("blue"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_blue);
                    } else if (notIcon.equals("green"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_green);
                    } else if (notIcon.equals("orange"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_orange);
                    } else if (notIcon.equals("purple"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_purple);
                    } else if (notIcon.equals("red"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_cloud_red);
                    } else if (notIcon.equals("icon"))
                    {
                        mBuilder.setSmallIcon(R.drawable.stat_notify_sms_icon);
                    }
                    break;
            }
        } else
        {
            mBuilder.setSmallIcon(R.drawable.stat_notify_sms_breath);
        }
    }
	
	public boolean individualNotification(NotificationCompat.Builder mBuilder, String name, Context context)
	{
		ArrayList<IndividualSetting> individuals = readFromFile4(context);
		
		for (int i = 0; i < individuals.size(); i++)
		{
			if (individuals.get(i).name.equals(name))
			{
				mBuilder.setSound(Uri.parse(individuals.get(i).ringtone));
				
				try
				{
					String[] vibPat = individuals.get(i).vibratePattern.replace("L", "").split(", ");
	        		long[] pattern = new long[vibPat.length];
	        		
	        		for (int j = 0; j < vibPat.length; j++)
	        		{
	        			pattern[j] = Long.parseLong(vibPat[j]);
	        		}
	        		
	        		mBuilder.setVibrate(pattern);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
        		
        		mBuilder.setLights(individuals.get(i).color, sharedPrefs.getInt("led_on_time", 1000), sharedPrefs.getInt("led_off_time", 2000));
        		
        		return true;
			}
		}
		
		return false;
	}
	
	public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName())) 
                return true;                                  
        }

        return false;
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
	  
	  private ArrayList<String> readFromFile(Context context) {
			
	      ArrayList<String> ret = new ArrayList<String>();
	      
	      try {
	          InputStream inputStream = context.openFileInput(FILENAME);
	          
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
	            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(FILENAME, Context.MODE_PRIVATE));
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	outputStreamWriter.write(data.get(i) + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
	  	
	  	private ArrayList<String> readFromFile2(Context context) {
			
		      ArrayList<String> ret = new ArrayList<String>();
		      
		      try {
		          InputStream inputStream = context.openFileInput("notifications.txt");
		          
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
		  	
		  	@SuppressWarnings("resource")
			private ArrayList<IndividualSetting> readFromFile4(Context context) {
				
			      ArrayList<IndividualSetting> ret = new ArrayList<IndividualSetting>();
			      
			      try {
			          InputStream inputStream;
			          
			          if (sharedPrefs.getBoolean("save_to_external", true))
			          {
			         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt");
			          } else
			          {
			        	  inputStream = context.openFileInput("individualNotifications.txt");
			          }
			          
			          if ( inputStream != null ) {
			          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			          	String receiveString = "";
			          	
			          	while ( (receiveString = bufferedReader.readLine()) != null) {
			          		ret.add(new IndividualSetting(receiveString, Integer.parseInt(bufferedReader.readLine()), bufferedReader.readLine(), bufferedReader.readLine()));
			          	}
			          	
			          	inputStream.close();
			          }
			      }
			      catch (FileNotFoundException e) {
			      	
					} catch (IOException e) {
						
					}

			      return ret;
				}
		  	
		  	@SuppressWarnings("resource")
			private ArrayList<BlacklistContact> readFromFile5(Context context) {
				
			      ArrayList<BlacklistContact> ret = new ArrayList<BlacklistContact>();
			      
			      try {
			    	  InputStream inputStream;
			          
			          if (sharedPrefs.getBoolean("save_to_external", true))
			          {
			         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt");
			          } else
			          {
			        	  inputStream = context.openFileInput("blacklist.txt");
			          }
			          
			          if ( inputStream != null ) {
			          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			          	String receiveString = "";
			          	
			          	while ( (receiveString = bufferedReader.readLine()) != null) {
			          		ret.add(new BlacklistContact(receiveString, Integer.parseInt(bufferedReader.readLine())));
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
