package com.klinker.android.messaging_donate;

import java.util.Locale;

import android.app.ActionBar;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.Toast;

public class CardWidgetSettingsActivity  extends PreferenceActivity {
	
	public static Context context;
    public SharedPreferences sharedPrefs;

    private boolean widgetDarkTitle;
    private boolean widgetDarkCards;
    private boolean widgetDarkBackground;
    private boolean useBackground;
	
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.widget_settings);
		setTitle(R.string.widget_settings);
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());

        widgetDarkTitle = sharedPrefs.getBoolean("widget_title_dark_theme", false);
        widgetDarkCards = sharedPrefs.getBoolean("widget_dark_theme", false);
        widgetDarkBackground = sharedPrefs.getBoolean("dark_background", false);
        useBackground = sharedPrefs.getBoolean("widget_background", true);
		
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

        // Inflate a "Done" custom action bar view to serve as the "Up" affordance.
        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.actionbar_custom_view_done, null);
        customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doneClick();
                    }
                });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.discard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.discard:
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("widget_title_dark_theme", widgetDarkTitle);
                editor.putBoolean("widget_dark_theme", widgetDarkCards);
                editor.putBoolean("dark_background", widgetDarkBackground);
                editor.putBoolean("widget_background", useBackground);
                editor.commit();
                finish(); // TODO: don't just finish()!
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean doneClick()
    {
        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

        // step 1
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }


        // step 2: preform changes here

        // step 3
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

        // step 4
        RemoteViews views = new RemoteViews(getPackageName(),
                R.layout.card_widget);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        // step 5
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        appWidgetManager.updateAppWidget(mAppWidgetId, views);

        // broadcast to update widget
        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        this.sendBroadcast(updateWidget);

        finish();
        return true;
    }
/*
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.done:
                int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

                // step 1
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    mAppWidgetId = extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }


                // step 2: preform changes here

                // step 3
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

                // step 4
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.card_widget);
                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                // step 5
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultValue);
                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                // broadcast to update widget
                Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                this.sendBroadcast(updateWidget);

                finish();
                return true;

            default:
                return true;
        }
    }*/
}