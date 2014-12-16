package com.klinker.android.messaging_sliding.notifications;

import android.app.ActionBar;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;

import java.util.ArrayList;
import java.util.Locale;

public class NotificationSetterActivity extends PreferenceActivity {

    public static Context context;
    public String name;
    public SharedPreferences sharedPrefs;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.custom_notification_settings);
        name = getIntent().getExtras().getString("com.klinker.android.messaging.CONTACT_NAME");
        setTitle(name);

        context = this;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        if (sharedPrefs.getBoolean("override_lang", false)) {
            String languageToLoad = "en";
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        } else {
            String languageToLoad = Resources.getSystem().getConfiguration().locale.getLanguage();
            Locale locale = new Locale(languageToLoad);
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        }

        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done_discard, null);

        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onBackPressed();
                    }
                });

        customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        ArrayList<IndividualSetting> individuals = IOUtil.readIndividualNotifications(this);

        boolean flag = false;
        int pos = 0;

        for (int i = 0; i < individuals.size(); i++) {
            if (individuals.get(i).name.equals(name)) {
                flag = true;
                pos = i;
                break;
            }
        }

        if (!flag) {
            individuals.add(new IndividualSetting(name, sharedPrefs.getInt("temp_led_color", getResources().getColor(R.color.white)), sharedPrefs.getString("temp_vibrate_pattern", "0L, 400L, 100L, 400L"), sharedPrefs.getString("temp_ringtone", "content://settings/system/notification_sound")));
        } else {
            individuals.set(pos, new IndividualSetting(name, sharedPrefs.getInt("temp_led_color", getResources().getColor(R.color.white)), sharedPrefs.getString("temp_vibrate_pattern", "0L, 400L, 100L, 400L"), sharedPrefs.getString("temp_ringtone", "content://settings/system/notification_sound")));
        }

        IOUtil.writeIndividualNotifications(individuals, this);

        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

}