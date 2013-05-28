package com.klinker.android.messaging_sliding;

import java.util.Locale;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

import com.klinker.android.messaging_donate.R;

public class PopupSettingsActivity  extends PreferenceActivity {
	
	public static Context context;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.popup_settings);
		setTitle(R.string.popup_settings);
		
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
		
		Preference popup = (Preference) findPreference("popup_reply");
		popup.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 if (sharedPrefs.getBoolean("popup_reply", false))
		            	 {
		            		 SharedPreferences.Editor editor = sharedPrefs.edit();
		            		 editor.putBoolean("override", true);
		            		 editor.commit();
		            	 }
		            	 
		                 return true;
		             }
		         });
	}
}