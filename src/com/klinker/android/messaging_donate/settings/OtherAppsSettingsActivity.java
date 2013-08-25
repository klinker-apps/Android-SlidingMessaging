package com.klinker.android.messaging_donate.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.klinker.android.messaging_donate.R;

import java.util.Locale;

public class OtherAppsSettingsActivity extends PreferenceActivity {

    public static Context context;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.other_apps);
        setTitle(R.string.other_apps);

        context = this;

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

        Preference emojiKeyboard = findPreference("emoji_keyboard");
        emojiKeyboard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.emoji_keyboard_trial&hl=en")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference emojiUnlock = findPreference("emoji_unlock");
        emojiUnlock.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.emoji_keyboard&hl=en")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference haloPop = findPreference("halo_pop");
        haloPop.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.halopop&hl=en")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference themeEngine = findPreference("theme_engine");
        themeEngine.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.klinker.android.messaging_theme&hl=en")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}
