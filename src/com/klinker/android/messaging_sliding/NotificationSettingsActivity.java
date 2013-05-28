package com.klinker.android.messaging_sliding;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.blacklist.BlacklistActivity;

import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

public class NotificationSettingsActivity extends PreferenceActivity {
	
	public static Context context;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.notification_settings);
		setTitle(R.string.advanced_notifications);
		
		SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		
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
		
		Preference blacklistSettings = (Preference) findPreference("blacklist_settings");
		blacklistSettings.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 Intent intent = new Intent(context2, BlacklistActivity.class);
		     			 startActivity(intent);
		                 return true;
		             }
		         });
	}
	
	@Override
	public synchronized void onActivityResult(final int requestCode,
		    int resultCode, final Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
	}
}
