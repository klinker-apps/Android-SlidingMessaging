package com.klinker.android.messaging_sliding.notifications;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

import com.klinker.android.messaging_donate.R;

public class NotificationSetterActivity extends PreferenceActivity {
	
	public static Context context;
	public String name;
	public SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.custom_notification_settings);
		name = getIntent().getExtras().getString("com.klinker.android.messaging.CONTACT_NAME");
		setTitle(name);
		
		context = this;
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
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
	}
	
	@Override
	public void onBackPressed()
	{
		ArrayList<IndividualSetting> individuals = readFromFile(this);
		
		boolean flag = false;
		int pos = 0;
		
		for (int i = 0; i < individuals.size(); i++)
		{
			if (individuals.get(i).name.equals(name))
			{
				flag = true;
				pos = i;
				break;
			}
		}
		
		if (!flag)
		{
			individuals.add(new IndividualSetting(name, sharedPrefs.getInt("temp_led_color", getResources().getColor(R.color.white)), sharedPrefs.getString("temp_vibrate_pattern", "0L, 400L, 100L, 400L"), sharedPrefs.getString("temp_ringtone", "content://settings/system/notification_sound")));
		} else
		{
			individuals.set(pos, new IndividualSetting(name, sharedPrefs.getInt("temp_led_color", getResources().getColor(R.color.white)), sharedPrefs.getString("temp_vibrate_pattern", "0L, 400L, 100L, 400L"), sharedPrefs.getString("temp_ringtone", "content://settings/system/notification_sound")));
		}
		
		writeToFile(individuals, this);
		
		super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
	}
	
	@SuppressWarnings("resource")
	private ArrayList<IndividualSetting> readFromFile(Context context) {
		
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
	  	
	  	private void writeToFile(ArrayList<IndividualSetting> data, Context context) {
	        try {
	            OutputStreamWriter outputStreamWriter;
	            
	            if (sharedPrefs.getBoolean("save_to_external", true))
	            {
	            	outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt"));
	            } else
	            {
	            	outputStreamWriter = new OutputStreamWriter(context.openFileOutput("individualNotifications.txt", Context.MODE_PRIVATE));
	            }
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	IndividualSetting write = data.get(i);
	            	
	            	outputStreamWriter.write(write.name + "\n" + write.color + "\n" + write.vibratePattern + "\n" + write.ringtone + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
}