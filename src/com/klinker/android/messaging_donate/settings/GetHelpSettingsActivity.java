package com.klinker.android.messaging_donate.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.developer_tips.MainActivity;
import wizardpager.ChangeLogMain;

import java.util.Locale;

public class GetHelpSettingsActivity extends PreferenceActivity {

    public static Context context;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.get_help_settings);
        setTitle(R.string.get_help);

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

        Preference devTips = findPreference("developer_tips");
        devTips.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent intent = new Intent(context, MainActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference changelog = findPreference("changelog");
        changelog.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                String version = "";

                try {
                    version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                Intent wizardintent = new Intent(getApplicationContext(), ChangeLogMain.class);
                wizardintent.putExtra("version", version);
                startActivity(wizardintent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference howTo = findPreference("initial_how_to");
        howTo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                final Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
                intent.putExtra("initial_run", true);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference googlePlus = findPreference("google_plus_page");
        googlePlus.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/u/0/communities/110180337009472136950")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference xdaThread = findPreference("xda_thread");
        xdaThread.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://forum.xda-developers.com/showthread.php?t=2343371")));
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                return false;
            }

        });

        Preference email = findPreference("contact_us");
        email.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference arg0) {
                Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {"slidingmessaging@gmail.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Sliding Messaging Pro");
                emailIntent.setType("plain/text");

                startActivity(emailIntent);

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
