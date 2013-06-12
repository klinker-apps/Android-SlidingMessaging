package com.klinker.android.messaging_sliding;

import com.klinker.android.messaging_donate.R;

import java.net.URLEncoder;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;

public class MmsSettingsActivity  extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	public Preference mmsc, proxy, port;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.mms_settings);
		setTitle(R.string.mms_settings);
		
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
		
		Preference smsToStore = (Preference) findPreference("mms_after");
		smsToStore.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				if (sharedPrefs.getBoolean("send_as_mms", false))
				{
					NumberPickerDialog.OnNumberSetListener mSmsLimitListener =
					        new NumberPickerDialog.OnNumberSetListener() {
					            public void onNumberSet(int limit) {
					            	SharedPreferences.Editor editor = sharedPrefs.edit();
					                
					                editor.putInt("mms_after", limit);
					                editor.commit();
					            }
					    };
					    
					new NumberPickerDialog(context2, mSmsLimitListener, sharedPrefs.getInt("mms_after", 4), 1, 1000, R.string.mms_after).show();
				}
				
				return false;
			}
			
		});
		
		mmsc = (Preference) findPreference("mmsc_url");
		mmsc.setSummary(sharedPrefs.getString("mmsc_url", ""));
		
		proxy = (Preference) findPreference("mms_proxy");
		proxy.setSummary(sharedPrefs.getString("mms_proxy", ""));
		
		port = (Preference) findPreference("mms_port");
		port.setSummary(sharedPrefs.getString("mms_port", ""));
		
		Preference presets = (Preference) findPreference("preset_apns");
		presets.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.mms.APNSettingsActivity.class);
				startActivity(intent);
				return false;
			}
			
		});
		
		Preference getHelp = (Preference) findPreference("get_apn_help");
		getHelp.setOnPreferenceClickListener(new OnPreferenceClickListener() {

			@Override
			public boolean onPreferenceClick(Preference arg0) {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setData(Uri.parse("http://www.google.com"));
				startActivity(intent);
				return false;
			}
			
		});
	}
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onResume() {
        super.onResume();
       
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
	
	@SuppressWarnings("deprecation")
	@Override
    protected void onPause() {
        super.onPause();
           
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);    
    }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences arg0, String arg1) {
		if (arg1.equals("mmsc_url"))
		{
			mmsc.setSummary(arg0.getString("mmsc_url", ""));
		} else if (arg1.equals("mms_proxy"))
		{
			proxy.setSummary(arg0.getString("mms_proxy", ""));
		} else if (arg1.equals("mms_port"))
		{
			port.setSummary(arg0.getString("mms_port", ""));
		}
		
	}
}