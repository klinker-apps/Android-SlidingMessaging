package com.klinker.android.messaging_card.theme;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;

public class CustomPopupActivity extends PreferenceActivity {
	public SharedPreferences sharedPrefs;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.custom_popup_settings);
		
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
		
		final Context context = this;
		
		Preference saveTheme = (Preference) findPreference("save_theme");
		saveTheme.setOnPreferenceClickListener(new OnPreferenceClickListener() {
		             public boolean onPreferenceClick(Preference preference) {
		            	 String data = "";
		            	 data += sharedPrefs.getString("cp_theme_name", "Light Theme") + "\n";
		            	 data += sharedPrefs.getInt("cp_messageBackground", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_sendBarBackground", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_dividerColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_nameTextColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_numberTextColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_dateTextColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_messageTextColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_draftTextColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_buttonColor", -1) + "\n";
		            	 data += sharedPrefs.getInt("cp_emojiButtonColor", -1);
		            	 
		            	 writeToFile(data, context, sharedPrefs.getString("cp_theme_name", "Light Theme").replace(" ", "") + ".theme2");
		 				 
		 				 Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_theme_saved), Toast.LENGTH_LONG).show();
		            	 finish();
                         overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
		                 return true;
		             }
		         });
		
		ColorPickerPreference sentBack = (ColorPickerPreference) findPreference("cp_messageBackground");
		sentBack.setAlphaSliderEnabled(true);
		
		ColorPickerPreference receiveBack = (ColorPickerPreference) findPreference("cp_sendBarBackground");
		receiveBack.setAlphaSliderEnabled(true);
	}
	
	@Override
	public void onBackPressed() {
		 String data = "";
         data += sharedPrefs.getString("cp_theme_name", "Light Theme") + "\n";
         data += sharedPrefs.getInt("cp_messageBackground", -1) + "\n";
         data += sharedPrefs.getInt("cp_sendBarBackground", -1) + "\n";
         data += sharedPrefs.getInt("cp_dividerColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_nameTextColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_numberTextColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_dateTextColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_messageTextColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_draftTextColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_buttonColor", -1) + "\n";
         data += sharedPrefs.getInt("cp_emojiButtonColor", -1);
	   	 
	   	 writeToFile(data, this, sharedPrefs.getString("cp_theme_name", "Light Theme").replace(" ", "") + ".theme2");
		 
		 Toast.makeText(getBaseContext(), "Theme Saved", Toast.LENGTH_LONG).show();
		 
		 super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
	}
	
	private void writeToFile(String data, Context context, String name) {
		String[] data2 = data.split("\n");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", name);
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            
            for (int i = 0; i < data2.length; i++)
            {
            	pw.println(data2[i]);
            }
            
            pw.flush();
            pw.close();
            f.close();
        }
        catch (Exception e) {
            
        } 
		
	}
}
