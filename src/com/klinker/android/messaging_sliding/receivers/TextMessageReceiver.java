package com.klinker.android.messaging_sliding.receivers;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.Util;
import com.klinker.android.messaging_sliding.MainActivityPopup;
import com.klinker.android.messaging_sliding.blacklist.BlacklistContact;
import com.klinker.android.messaging_sliding.notifications.IndividualSetting;
import com.klinker.android.messaging_sliding.quick_reply.CardQuickReply;
import com.klinker.android.messaging_sliding.quick_reply.QmDelete;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.klinker.android.messaging_sliding.quick_reply.QuickReply;

import java.io.InputStream;
import java.util.*;

@SuppressWarnings("deprecation")
public class TextMessageReceiver extends BroadcastReceiver {
	public static final String SMS_EXTRA_NAME = "pdus";
	public SharedPreferences sharedPrefs;

    private boolean alert = true;
	
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

            boolean voiceMessage = intent.getBooleanExtra("voice_message", false);

            Log.v("refresh_voice", "sms receiver " + voiceMessage);

            if (!voiceMessage) {
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
            } else {
                body = intent.getStringExtra("voice_body");
                address = intent.getStringExtra("voice_address");
                date = intent.getLongExtra("voice_date", Calendar.getInstance().getTimeInMillis()) + "";
            }

            Calendar cal = Calendar.getInstance();
            dateReceived = cal.getTimeInMillis() + "";

            final String origBody = body;
            final String origDate = dateReceived;
            final String origAddress = address;

            Intent fnReceiver = new Intent("com.klinker.android.messaging_donate.FNRECEIVED");
            fnReceiver.putExtra("address", origAddress);
            fnReceiver.putExtra("body", origBody);
            context.sendBroadcast(fnReceiver);
	        
	        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

            if(!sharedPrefs.getBoolean("alert_in_call", true) && isCallActive(context))
            {
                alert = false;
            }
	        
	        ArrayList<BlacklistContact> blacklist = IOUtil.readBlacklist(context);
	        int blacklistType = 0;
	        
	        for (int i = 0; i < blacklist.size(); i++)
	        {
	        	if (blacklist.get(i).name.equals(address.replace("-", "").replace("(", "").replace(")", "").replace(" ", "").replace("+1", "")))
	        	{
	        		blacklistType = blacklist.get(i).type;
	        	}
	        }

            final ArrayList<String> prevNotifications = IOUtil.readNotifications(context);
	        
	        if (blacklistType == 2)
	        {
	        	abortBroadcast();
	        } else
	        {
		        if (sharedPrefs.getBoolean("override", false) || voiceMessage)
		        {
		        	ContentValues values = new ContentValues();
			        values.put("address", address);
			        values.put("body", body);
			        values.put("date", dateReceived);
			        values.put("read", "0");
			        values.put("date_sent", date);

                    if (voiceMessage) {
                        values.put("status", 2);
                    }

			        context.getContentResolver().insert(Uri.parse("content://sms/inbox"), values);
		        }
		        
		        if (blacklistType != 1)
		        {
			        Bundle bundle = new Bundle();
			        bundle.putString("body", body);
			        bundle.putString("date", date);
			        bundle.putString("address", address);
			        
			        String origin = address.replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
					
			        try
			        {
						Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
						Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME}, null, null, null);
				
						if (phonesCursor != null && phonesCursor.moveToFirst()) {
							name = phonesCursor.getString(0);
						} else {
							Locale sCachedLocale = Locale.getDefault();
							int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
							Editable editable = new SpannableStringBuilder(address);
							PhoneNumberUtils.formatNumber(editable, sFormatType);
							name = editable.toString();
						}
						
						phonesCursor.close();
					
						ArrayList<String> newMessages = IOUtil.readNewMessages(context);
						boolean flag = false;
						
						for (int i = 0; i < newMessages.size(); i++) {
							if (name.equals(newMessages.get(i))) {
								flag = true;
							}
						}
						
						if (!flag) {
							newMessages.add(name);
						}
						
						IOUtil.writeNewMessages(newMessages, context);
						
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
				    
				    InputStream input = ContactUtil.openDisplayPhoto(Long.parseLong(id), context);
					
					if (input == null)
					{
						input = context.getResources().openRawResource(R.drawable.default_avatar);
					}
					
					Bitmap contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 120, 120, true);
					
					Intent intent2 = new Intent(context, CardQuickReply.class);
                    intent2.putExtra("address", origAddress);
                    intent2.putExtra("body", origBody);
                    intent2.putExtra("date", origDate);
                    int pIntentExtra = PendingIntent.FLAG_UPDATE_CURRENT;
					
					if (sharedPrefs.getBoolean("use_old_popup", false))
					{
						intent2 = new Intent(context, QuickReply.class);
                        pIntentExtra = 0;
                    }

                    if (sharedPrefs.getBoolean("full_app_popup", true)) {
                        intent2 = new Intent(context, MainActivityPopup.class);
                        intent2.putExtra("fromWidget", false);
                        intent2.putExtra("fromNotification", true);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        pIntentExtra = 0;
                    }

                    if (sharedPrefs.getBoolean("halo_popup", false)) {
                        intent2 = new Intent(context, MainActivity.class);
                        pIntentExtra = 0;

                        try
                        {
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                            intent2.putExtra("halo_popup", true);
                        } catch (Exception e)
                        {
                            intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        }
                    }

                    intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent2.putExtra("notification", "true");
					PendingIntent pIntent = PendingIntent.getActivity(context, 0, intent2, pIntentExtra);
			
					if (sharedPrefs.getBoolean("notifications", true))
					{
						if (sharedPrefs.getBoolean("wake_screen", false))
						{
							PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
				            final WakeLock wakeLock = pm.newWakeLock((PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP), "TAG");
				            wakeLock.acquire(Long.parseLong(sharedPrefs.getString("screen_timeout", "5"))*1000);
						}
				        
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
						        
						        setIcon(mBuilder, context);
						        
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
						        
						        Intent resultIntent = new Intent(context, MainActivity.class);
						        resultIntent.setAction(Intent.ACTION_SENDTO);
						        resultIntent.putExtra("com.klinker.android.OPEN", address);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
						                    PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context, alert))
						        {
							        if (sharedPrefs.getBoolean("vibrate", true) && alert)
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

                                    if(alert)
                                    {
                                        try
                                        {
                                            mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                        } catch(Exception e)
                                        {
                                            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                        }
                                    }
						        }
						        
						        final  NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                                    mBuilder.setTicker(null);
                                    mBuilder.setSmallIcon(android.R.color.transparent);
                                    mBuilder.setPriority(Notification.PRIORITY_LOW);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNotificationManager.cancel(1);
                                        }
                                    }, 1000);
                                }

                                Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
                                Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

                                mNotificationManager.notify(1, notification);


                                ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        IOUtil.writeNotifications(newNotifications, context);
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

                                if (sharedPrefs.getBoolean("stack_notifications", true)) {
					        	    body = body2 + " | " + body;
                                }
					        	
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle(name)
						                .setContentText(body)
						                .setTicker(name + ": " + body);
						        
						        if (!id.equals("0"))
						        	mBuilder.setLargeIcon(contactImage);

                                setIcon(mBuilder, context);
						        
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
						        
						        Intent resultIntent = new Intent(context, MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context, alert))
						        {
							        if (sharedPrefs.getBoolean("vibrate", true) && alert)
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

                                    if(alert)
                                    {
                                        try
                                        {
                                            mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                        } catch(Exception e)
                                        {
                                            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                        }
                                    }
						        }
						        
						        final NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                                    mBuilder.setTicker(null);
                                    mBuilder.setSmallIcon(android.R.color.transparent);
                                    mBuilder.setPriority(Notification.PRIORITY_LOW);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNotificationManager.cancel(1);
                                        }
                                    }, 1000);
                                }
						        
						        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).bigText(body).build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

                                mNotificationManager.notify(1, notification);
						        
						        ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        IOUtil.writeNotifications(newNotifications, context);
					        } else
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle(prevNotifications.size() + 1 + " New Messages")
						                .setTicker(prevNotifications.size() + 1 + " New Messages");

                                setIcon(mBuilder, context);
					        	
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
						        
						        Intent resultIntent = new Intent(context, MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if(!individualNotification(mBuilder, name, context, alert))
						        {
							        if (sharedPrefs.getBoolean("vibrate", true) && alert)
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

                                    if(alert)
                                    {
                                        try
                                        {
                                            mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                        } catch(Exception e)
                                        {
                                            mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                        }
                                    }
						        }
						        
						        final NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						        
						        NotificationCompat.InboxStyle notification2 = new NotificationCompat.InboxStyle(mBuilder);
						        
						        prevNotifications.add(name + ": " + body);
						        
						        for (int i = 0; i < prevNotifications.size(); i++)
						        {
						        	notification2.addLine(prevNotifications.get(i));
						        }
						        
						        notification2.setSummaryText(prevNotifications.size() + " New Messages");

                                if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                                    mBuilder.setTicker(null);
                                    mBuilder.setSmallIcon(android.R.color.transparent);
                                    mBuilder.setPriority(Notification.PRIORITY_LOW);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNotificationManager.cancel(1);
                                        }
                                    }, 1000);
                                }
						        
						        Notification notification = notification2.build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

                                mNotificationManager.notify(1, notification);
						        
						        IOUtil.writeNotifications(prevNotifications, context);
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

                                setIcon(mBuilder, context);
						        
						        Intent resultIntent = new Intent(context, MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if (sharedPrefs.getBoolean("vibrate", true) && alert)
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

                                if(alert)
                                {
                                    try
                                    {
                                        mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                    } catch(Exception e)
                                    {
                                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    }
                                }
						        
						        final NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                                    mBuilder.setTicker(null);
                                    mBuilder.setSmallIcon(android.R.color.transparent);
                                    mBuilder.setPriority(Notification.PRIORITY_LOW);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNotificationManager.cancel(1);
                                        }
                                    }, 1000);
                                }
						        
						        Notification notification = new NotificationCompat.BigTextStyle(mBuilder).build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

                                mNotificationManager.notify(1, notification);
						        
						        ArrayList<String> newNotifications = new ArrayList<String>();
						        newNotifications.add(name + ": " + body);
						        IOUtil.writeNotifications(newNotifications, context);
					        } else
					        {
					        	NotificationCompat.Builder mBuilder =
						                new NotificationCompat.Builder(context)
						                .setSmallIcon(R.drawable.stat_notify_sms)
						                .setContentTitle("New Messages")
						                .setTicker("New Messages")
						                .setContentText("");

                                setIcon(mBuilder, context);
						        
						        Intent resultIntent = new Intent(context, MainActivity.class);
						
						        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
						        stackBuilder.addParentStack(MainActivity.class);
						        stackBuilder.addNextIntent(resultIntent);
						        PendingIntent resultPendingIntent =
						                stackBuilder.getPendingIntent(
						                    0,
                                            PendingIntent.FLAG_CANCEL_CURRENT
						                );
						        
						        mBuilder.setContentIntent(resultPendingIntent);
						        mBuilder.setAutoCancel(true);
						        
						        if (sharedPrefs.getBoolean("vibrate", true) && alert)
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

                                if(alert)
                                {
                                    try
                                    {
                                        mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                    } catch(Exception e)
                                    {
                                        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                    }
                                }
						        
						        final NotificationManager mNotificationManager =
						            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
						        
						        NotificationCompat.InboxStyle notification2 = new NotificationCompat.InboxStyle(mBuilder);
						        
						        prevNotifications.add(name + ": " + body);
						        
						        notification2.setSummaryText(prevNotifications.size() + " New Messages");

                                if (sharedPrefs.getBoolean("slideover_enabled", false) && sharedPrefs.getBoolean("slideover_hide_notifications", false)) {
                                    mBuilder.setTicker(null);
                                    mBuilder.setSmallIcon(android.R.color.transparent);
                                    mBuilder.setPriority(Notification.PRIORITY_LOW);

                                    new Handler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            mNotificationManager.cancel(1);
                                        }
                                    }, 1000);
                                }
						        
						        Notification notification = notification2.build();
						        Intent deleteIntent = new Intent(context, NotificationReceiver.class); 
						        notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);

                                mNotificationManager.notify(1, notification);
						        
						        IOUtil.writeNotifications(prevNotifications, context);
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
		        
		        if (!Util.isRunning(context) && blacklistType != 1)
		        {
		        	Handler handler = new Handler();
		        	handler.postDelayed(new Runnable() {
	
						@Override
						public void run() {
							if (sharedPrefs.getBoolean("popup_reply", false) && !sharedPrefs.getBoolean("secure_notification", false))
					        {
					        	Intent intent3 = new Intent(context, CardQuickReply.class);
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
										intent3 = new Intent(context, MainActivity.class);
									} else {
										intent3 = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);

                                        if (prevNotifications.size() > 0) {
                                            intent3.putExtra("multipleNew", true);
                                        }
									}

									try
									{
										if (halo) {
											intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                                            intent3.putExtra("halo_popup", true);
										} else {
											intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
										}
									} catch (Exception e)
									{
										intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
									}

                                    PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

                                    if (pm.isScreenOn() || sharedPrefs.getBoolean("unlock_screen", false))
                                    {
                                        if (!sharedPrefs.getBoolean("full_app_popup", true) || (sharedPrefs.getBoolean("full_app_popup", true) && !sharedPrefs.getBoolean("slideover_popup_lockscreen_only", false)) || (sharedPrefs.getBoolean("unlock_screen", false) && !sharedPrefs.getBoolean("full_app_popup", true))) {
                                            final Intent popup = intent3;

                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    context.startActivity(popup);
                                                }
                                            }, 250);
                                        }
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

                if (voiceMessage) {
                    Intent voice = new Intent("com.klinker.android.messaging.NEW_MMS");
                    voice.putExtra("address", address);
                    voice.putExtra("body", body);
                    voice.putExtra("date", date);
                    context.sendBroadcast(voice);
                }
		        
		        if (sharedPrefs.getBoolean("override", false))
		        {
                    try {
		        	    this.abortBroadcast();
                    } catch (Exception e) {

                    }
		        }
	        }
		} catch (Exception e)
		{
			
		}
	}

    public static void setIcon(NotificationCompat.Builder mBuilder, Context context)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

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
	
	public static boolean individualNotification(NotificationCompat.Builder mBuilder, String name, Context context, boolean alert)
	{
		ArrayList<IndividualSetting> individuals = IOUtil.readIndividualNotifications(context);
		
		for (int i = 0; i < individuals.size(); i++)
		{
			if (individuals.get(i).name.equals(name))
			{
                if (alert)
				    mBuilder.setSound(Uri.parse(individuals.get(i).ringtone));
				
				try
				{
					String[] vibPat = individuals.get(i).vibratePattern.replace("L", "").split(", ");
	        		long[] pattern = new long[vibPat.length];
	        		
	        		for (int j = 0; j < vibPat.length; j++)
	        		{
	        			pattern[j] = Long.parseLong(vibPat[j]);
	        		}
	        		if (alert)
	        		    mBuilder.setVibrate(pattern);
				} catch (Exception e)
				{
					e.printStackTrace();
				}
        		
        		mBuilder.setLights(individuals.get(i).color, PreferenceManager.getDefaultSharedPreferences(context).getInt("led_on_time", 1000), PreferenceManager.getDefaultSharedPreferences(context).getInt("led_off_time", 2000));
        		
        		return true;
			}
		}
		
		return false;
	}

    public static boolean isCallActive(Context context){
        AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
        if(manager.getMode()==AudioManager.MODE_IN_CALL){
            return true;
        }
        else{
            return false;
        }
    }
}
