package com.klinker.android.messaging_card;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.DeleteOldService;
import com.klinker.android.messaging_sliding.NumberPickerDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.widget.Toast;

public class AdvancedSettingsActivity extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.card_advanced_settings);
		setTitle(R.string.advanced_settings);
		
		final SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if (sharedPrefs.getBoolean("override_lang", false))
		{
			String languageToLoad  = "en";
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		} else
		{
			String languageToLoad = Resources.getSystem().getConfiguration().locale.getLanguage();
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
		
		final Context context2 = this;
		
		Preference titleSettings = (Preference) findPreference("text_prefs");
		titleSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 Intent intent = new Intent(context2, TextSettingsActivity.class);
		     			 startActivity(intent);
		                 return true;
		             }
		         });
		
		Preference conversationSettings = (Preference) findPreference("conversation_prefs");
		conversationSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
           	 Intent intent = new Intent(context2, ConversationSettingsActivity.class);
    			 startActivity(intent);
                return true;
            }
        });
		
		Preference deleteOld = (Preference) findPreference("delete_old");
		deleteOld.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if (sharedPrefs.getBoolean("delete_old", false))
				{
					Intent deleteIntent = new Intent(context2, DeleteOldService.class);
					PendingIntent pintent = PendingIntent.getService(context2, 0, deleteIntent, 0);
					AlarmManager alarm = (AlarmManager)context2.getSystemService(Context.ALARM_SERVICE);
					alarm.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), 6*60*60*1000, pintent);
				}
				
				return false;
			}
			
		});
		
		Preference backup = (Preference) findPreference("backup");
		backup.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				File des = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backup.prefs");
				saveSharedPreferencesToFile(des);
				
				Toast.makeText(context2, context2.getResources().getString(R.string.backup_success), Toast.LENGTH_LONG).show();
				
				return false;
			}
			
		});
		
		Preference restore = (Preference) findPreference("restore");
		restore.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				File des = new File(Environment.getExternalStorageDirectory() + "/SlidingMessaging/backup.prefs");
				loadSharedPreferencesFromFile(des);
				
				Toast.makeText(context2, context2.getResources().getString(R.string.restore_success), Toast.LENGTH_LONG).show();
				
				if (sharedPrefs.getBoolean("background_service", false))
            	{
                	final ProgressDialog progDialog = new ProgressDialog(context2);
		               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		               progDialog.setMessage(context2.getResources().getString(R.string.caching));
		               progDialog.show();
		               
		               new Thread(new Runnable(){

							@Override
							public void run() {
								
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
								
								writeToFile(data, context2);
								
								((Activity) context2).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

									@Override
									public void run() {
										progDialog.dismiss();
									}
							    	
							    });
							}
		            	   
		               }).start();
            	}
				
				return false;
			}
			
		});
		
		Preference backgroundService = (Preference) findPreference("background_service");
		backgroundService.setOnPreferenceClickListener(new OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
           	 
            	if (sharedPrefs.getBoolean("background_service", false))
            	{
            		final AlertDialog.Builder dialog = new AlertDialog.Builder(context2);
                	dialog.setMessage(R.string.background_service_dialog);
                	dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

    					@Override
    					public void onClick(DialogInterface arg0, int arg1) {
    						SharedPreferences.Editor editor = sharedPrefs.edit();
		            		editor.putBoolean("override", true);
		            		editor.commit();
    					}
                		
                	});
                	
                	Dialog dialog2 = dialog.create();
                	dialog2.show();
                	
                	final ProgressDialog progDialog = new ProgressDialog(context2);
		               progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		               progDialog.setMessage(context2.getResources().getString(R.string.caching));
		               progDialog.show();
		               
		               new Thread(new Runnable(){

							@Override
							public void run() {
								
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
								
								writeToFile(data, context2);
								
								((Activity) context2).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

									@Override
									public void run() {
										progDialog.dismiss();
									}
							    	
							    });
							}
		            	   
		               }).start();
            	}
            	
                return true;
            }
        });
		
		Preference smsToStore = (Preference) findPreference("sms_limit");
		smsToStore.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if (sharedPrefs.getBoolean("delete_old", false))
				{
					NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
					        new NumberPickerDialog.OnNumberSetListener() {
					            public void onNumberSet(int limit) {
					            	SharedPreferences.Editor editor = sharedPrefs.edit();
					                
					                editor.putInt("sms_limit", limit);
					                editor.commit();
					            }
					    };
					    
					new NumberPickerDialog(context2, mSmsLimitListener, sharedPrefs.getInt("sms_limit", 500), 100, 1000, R.string.sms_limit).show();
				}
				
				return false;
			}
			
		});
		
//		Preference mmsToStore = (Preference) findPreference("mms_limit");
//		mmsToStore.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//
//			@Override
//			public boolean onPreferenceClick(Preference arg0) {
//				if (sharedPrefs.getBoolean("delete_old", false))
//				{
//					NumberPickerDialog.OnNumberSetListener mMmsLimitListener =
//					        new NumberPickerDialog.OnNumberSetListener() {
//					            public void onNumberSet(int limit) {
//					            	SharedPreferences.Editor editor = sharedPrefs.edit();
//					                
//					                editor.putInt("mms_limit", limit);
//					                editor.commit();
//					            }
//					    };
//					    
//					new NumberPickerDialog(context2, android.R.style.Theme_Holo_Dialog, mMmsLimitListener, sharedPrefs.getInt("mms_limit", 100), 50, 500, R.string.mms_limit).show();
//				}
//				
//				return false;
//			}
//			
//		});
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
	
	private boolean saveSharedPreferencesToFile(File dst)
	{
		boolean res = false;
		ObjectOutputStream output = null;
		
		try
		{
			output = new ObjectOutputStream(new FileOutputStream(dst));
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
			
			output.writeObject(pref.getAll());
			
			res = true;
		} catch (Exception e)
		{
			
		} finally
		{
			try
			{
				if (output != null)
				{
					output.flush();
					output.close();
				}
			} catch (Exception e)
			{
				
			}
		}
		
		return res;
	}
	
	private boolean loadSharedPreferencesFromFile(File src)
	{
		boolean res = false;
		ObjectInputStream input = null;
		
		try
		{
			input = new ObjectInputStream(new FileInputStream(src));
			Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit();
			prefEdit.clear();
			
			@SuppressWarnings("unchecked")
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			
			for (Entry<String, ?> entry : entries.entrySet()) 
			{
				Object v = entry.getValue();
				String key = entry.getKey();
				
				if (v instanceof Boolean)
				{
					prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
				} else if (v instanceof Float)
				{
					prefEdit.putFloat(key,  ((Float) v).floatValue());
				} else if (v instanceof Integer)
				{
					prefEdit.putInt(key,  ((Integer) v).intValue());
				} else if (v instanceof Long)
				{
					prefEdit.putLong(key,  ((Long) v).longValue());
				} else if (v instanceof String)
				{
					prefEdit.putString(key, ((String) v));
				}
			}
			
			prefEdit.commit();
			
			res = true;
		} catch (Exception e)
		{
			
		} finally
		{
			try
			{
				if (input != null)
				{
					input.close();
				}
			} catch (Exception e)
			{
				
			}
		}
		
		return res;
	}
	
	@Override
	public synchronized void onActivityResult(final int requestCode,
		    int resultCode, final Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
	}
}