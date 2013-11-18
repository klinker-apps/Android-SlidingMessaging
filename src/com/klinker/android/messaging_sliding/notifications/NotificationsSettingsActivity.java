package com.klinker.android.messaging_sliding.notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class NotificationsSettingsActivity extends Activity {

    public static Context context;
    public ListView templates;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<IndividualSetting> individuals;
    public ArrayList<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.templates);
        templates = (ListView) findViewById(R.id.templateListView2);
        findViewById(R.id.templateListView).setVisibility(View.GONE);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        individuals = IOUtil.readIndividualNotifications(this);
        names = new ArrayList<String>();

        for (int i = 0; i < individuals.size(); i++) {
            names.add(individuals.get(i).name);
        }

        TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
        templates.setAdapter(adapter);
        templates.setStackFromBottom(false);

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

        templates.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete_contact_settings))
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                individuals.remove(arg2);
                                names.remove(arg2);

                                TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, names);
                                templates.setAdapter(adapter);

                                IOUtil.writeIndividualNotifications(individuals, context);
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
                return true;
            }

        });

        templates.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString("temp_ringtone", individuals.get(arg2).ringtone);
                editor.putInt("temp_led_color", individuals.get(arg2).color);
                editor.putString("temp_vibrate_pattern", individuals.get(arg2).vibratePattern);
                editor.commit();

                Intent intent = new Intent(context, NotificationSetterActivity.class);
                intent.putExtra("com.klinker.android.messaging.CONTACT_NAME", individuals.get(arg2).name);

                individuals.remove(arg2);
                IOUtil.writeIndividualNotifications(individuals, context);
                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }

        });

        addNew.setText(getResources().getString(R.string.add_new_individual));

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                IOUtil.writeIndividualNotifications(individuals, context);
                Intent intent = new Intent(context, ContactFinderActivity.class);
                context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);

            }

        });

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
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
    public void onResume() {
        super.onResume();

        individuals = IOUtil.readIndividualNotifications(this);
        names = new ArrayList<String>();

        for (int i = 0; i < individuals.size(); i++) {
            names.add(individuals.get(i).name);
        }

        TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
        templates.setAdapter(adapter);
        templates.setStackFromBottom(false);
    }

    @Override
    public void onBackPressed() {
        IOUtil.writeIndividualNotifications(individuals, this);
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}