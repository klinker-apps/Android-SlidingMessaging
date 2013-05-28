package com.klinker.android.messaging_card;

import com.klinker.android.messaging_donate.R;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import com.klinker.android.messaging_sliding.MmsSettingsActivity;
import com.klinker.android.messaging_sliding.NotificationSettingsActivity;
import com.klinker.android.messaging_sliding.PopupSettingsActivity;
import com.klinker.android.messaging_sliding.TemplateActivity;
import com.klinker.android.messaging_sliding.notifications.NotificationsSettingsActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.provider.MediaStore;

public class SettingsActivity extends PreferenceActivity {
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.card_settings);
		
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
		
//		Preference donate = (Preference) findPreference("donate");
//		donate.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//		             public boolean onPreferenceClick(Preference preference) {
//		            	 Intent intent = new Intent(Intent.ACTION_VIEW);
//		            	 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_donate"));
//		            	 startActivity(intent);
//		                 return true;
//		             }
//		         });
		
		Preference advancedSettings = (Preference) findPreference("advanced");
		advancedSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 Intent intent = new Intent(context2, AdvancedSettingsActivity.class);
		     			 startActivity(intent);
		                 return true;
		             }
		         });
		
		Preference notificationSettings = (Preference) findPreference("notification_advanced");
		notificationSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 Intent intent = new Intent(context2, NotificationSettingsActivity.class);
		     			 startActivity(intent);
		                 return true;
		             }
		         });
		
		Preference mms = (Preference) findPreference("mms_settings");
		mms.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 Intent intent = new Intent(context2, MmsSettingsActivity.class);
		     			 startActivity(intent);
		                 return true;
		             }
		         });
		
		Preference popup = (Preference) findPreference("popup_settings");
		popup.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(context2, PopupSettingsActivity.class);
				startActivity(intent);
				return false;
			}
			
		});
		
		Preference indiv = (Preference) findPreference("individual_notification_settings");
		indiv.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(context2, NotificationsSettingsActivity.class);
				startActivity(intent);
				return false;
			}
			
		});
		
		Preference templates = (Preference) findPreference("quick_templates");
		templates.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(context2, TemplateActivity.class);
				startActivity(intent);
				return false;
			}
			
		});
		
		final Context context = this;
		
		Preference font = (Preference) findPreference("font_settings");
		font.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference pref)
			{
				Intent intent3 = new Intent(context, com.klinker.android.messaging_sliding.theme.CustomFontSettingsActivity.class);
	       		startActivity(intent3);
				return true;
			}
		});
		
//		Preference gesture = (Preference) findPreference("gesture_settings");
//		gesture.setOnPreferenceClickListener(new OnPreferenceClickListener() {
//			public boolean onPreferenceClick(Preference pref)
//			{
//				Intent intent3 = new Intent(context, com.klinker.android.messaging_card.GestureSettingsActivity.class);
//	       		startActivity(intent3);
//				return true;
//			}
//		});
		
		Preference deleteAll = (Preference) findPreference("delete_all");
		deleteAll.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
		    	AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
				builder2.setMessage(context.getResources().getString(R.string.delete_all));
				builder2.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					@SuppressLint("SimpleDateFormat")
					public void onClick(DialogInterface dialog, int id) {
						
						final ProgressDialog progDialog = new ProgressDialog(context);
			            progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			            progDialog.setMessage(context.getResources().getString(R.string.deleting));
			            progDialog.show();
			            
			            new Thread(new Runnable(){

								@Override
								public void run() {
									deleteSMS(context);
									writeToFile(new ArrayList<String>(), context);
									
									((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

										@Override
										public void run() {
											progDialog.dismiss();
										}
								    	
								    });
								}
			         	   
			            }).start();

			           }
					});
				builder2.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			           public void onClick(DialogInterface dialog, int id) {
			               dialog.dismiss();
			           }
			       });
				AlertDialog dialog2 = builder2.create();
				
				dialog2.show();
	            
		    	return true;
			}
			
		});
	}
	
	public void deleteSMS(Context context) {
		ArrayList<String> threadIds = new ArrayList<String>();
		String[] projection = new String[]{"_id"};
		Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");
		Cursor query = context.getContentResolver().query(uri, projection, null, null, null);
		
		if (query.moveToFirst())
		{
			do
			{
				threadIds.add(query.getString(query.getColumnIndex("_id")));
			} while (query.moveToNext());
		}
		
	    try {
	    	for (int i = 0; i < threadIds.size(); i++)
	    	{
	        	context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds.get(i) + "/"), null, null);
	    	}
	    } catch (Exception e) {
	    }
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
	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
		if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout"))
		{
			Intent i = new Intent(this, com.klinker.android.messaging_sliding.MainActivity.class);
		    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    startActivity(i);
		} else if (sharedPrefs.getString("run_as", "sliding").equals("card"))
		{
			Intent i = new Intent(this, com.klinker.android.messaging_card.MainActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(i);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

        if (requestCode == 1)
        {
            if(resultCode == RESULT_OK){  
                Uri selectedImage = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                
                SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();
                
                editor.putString("custom_background_location", filePath);
                editor.commit();

            }
        } else if (requestCode == 2)
        {
        	if(resultCode == RESULT_OK){  
                Uri selectedImage = imageReturnedIntent.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};

                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();

                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String filePath = cursor.getString(columnIndex);
                cursor.close();
                
                SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                SharedPreferences.Editor editor = sharedPrefs.edit();
                
                editor.putString("custom_background2_location", filePath);
                editor.commit();

            }
        } else
        {
        	
        }
	}
}
