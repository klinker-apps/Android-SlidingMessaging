package com.klinker.android.messaging_sliding.scheduled;

import android.app.*;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.settings.DrawerArrayAdapter;
import com.klinker.android.messaging_donate.settings.GetHelpSettingsActivity;
import com.klinker.android.messaging_donate.settings.OtherAppsSettingsActivity;
import com.klinker.android.messaging_donate.settings.SettingsPagerActivity;
import com.klinker.android.messaging_sliding.templates.TemplateActivity;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class ScheduledSms extends Activity {

    public final static String EXTRA_NUMBER = "com.klinker.android.messaging_sliding.NUMBER";
    public final static String EXTRA_DATE = "com.klinker.android.messaging_sliding.DATE";
    public final static String EXTRA_REPEAT = "com.klinker.android.messaging_sliding.REPEAT";
    public final static String EXTRA_MESSAGE = "com.klinker.android.messaging_sliding.MESSAGE";

    public static Context context;
    public ListView sms;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<String[]> text;

    private String[] linkItems;
    private String[] otherItems;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private LinearLayout mDrawer;
    private ActionBarDrawerToggle mDrawerToggle;

    private Activity activity;

    @Override
    protected void onResume()
    {
        super.onResume();
        text = readFromFile(this, false);

        SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(this, text);
        sms.setAdapter(adapter);
        sms.setStackFromBottom(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(5);

        sms = (ListView) findViewById(R.id.smsListView);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        text = readFromFile(this, true);

        SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(this, text);
        sms.setAdapter(adapter);
        sms.setStackFromBottom(false);

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

        sms.setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete_scheduled_sms))
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                cancelAlarm(Integer.parseInt(text.get(arg2)[4]), text.get(arg2)[2], Long.parseLong(text.get(arg2)[1]));

                                text.remove(arg2);

                                writeToFile(text, context);

                                onResume();
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
                return false;
            }
        });

        sms.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                           final int pos, long arg3) {

                cancelAlarm(Integer.parseInt(text.get(pos)[4]), text.get(pos)[2], Long.parseLong(text.get(pos)[1]));

                Intent intent = new Intent(context, NewScheduledSms.class);
                intent.putExtra(EXTRA_NUMBER, text.get(pos)[0]);
                intent.putExtra(EXTRA_DATE, text.get(pos)[1]);
                intent.putExtra(EXTRA_REPEAT, text.get(pos)[2]);
                intent.putExtra(EXTRA_MESSAGE, text.get(pos)[3]);

                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }
        });

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(context, NewScheduledSms.class);

                intent.putExtra(EXTRA_NUMBER, "");
                intent.putExtra(EXTRA_DATE, "");
                intent.putExtra(EXTRA_REPEAT, "0");
                intent.putExtra(EXTRA_MESSAGE, "");

                startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }

        });
        linkItems = new String[] { getResources().getString(R.string.theme_settings),
                getResources().getString(R.string.notification_settings),
                getResources().getString(R.string.popup_settings),
                getResources().getString(R.string.slideover_settings),
                getResources().getString(R.string.text_settings),
                getResources().getString(R.string.conversation_settings),
                getResources().getString(R.string.mms_settings),
                getResources().getString(R.string.google_voice_settings),
                getResources().getString(R.string.security_settings),
                getResources().getString(R.string.advanced_settings)   };

        otherItems = new String[] {getResources().getString(R.string.quick_templates),
                getResources().getString(R.string.scheduled_sms),
                getResources().getString(R.string.get_help),
                getResources().getString(R.string.other_apps),
                getResources().getString(R.string.rate_it) };

        DrawerArrayAdapter.current = 1;
        SettingsPagerActivity.settingsLinksActive = false;
        SettingsPagerActivity.inOtherLinks = true;

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.links_list);
        mDrawer = (LinearLayout) findViewById(R.id.drawer);

        Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,
                R.array.drawer_spinner_array, R.layout.drawer_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter2);

        spinner.setSelection(1);

        spinner.setOnItemSelectedListener(new SpinnerClickListener());

        // Set the adapter for the list view
        mDrawerList.setAdapter(new DrawerArrayAdapter(this,
                new ArrayList<String>(Arrays.asList(otherItems))));
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer icon to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        );

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        activity = this;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private class SpinnerClickListener implements  Spinner.OnItemSelectedListener {
        @Override
        // sets the string repetition to whatever is choosen from the spinner
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
            String selected = parent.getItemAtPosition(pos).toString();

            if (selected.equals("Settings Links")) {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(linkItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = true;
            } else {
                mDrawerList.setAdapter(new DrawerArrayAdapter(activity,
                        new ArrayList<String>(Arrays.asList(otherItems))));
                mDrawerList.invalidate();
                SettingsPagerActivity.settingsLinksActive = false;
            }


        }

        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView parent, View view, int position, long id) {

            // TODO: Make this smoother
            // TODO: Add the other settings options for not switching viewpager
            final Context context = getApplicationContext();
            final Intent intent;

            if (SettingsPagerActivity.settingsLinksActive) {
                mDrawerLayout.closeDrawer(mDrawer);

                final int mPositition = position;

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //onBackPressed();

                        Intent mIntent = new Intent(context, SettingsPagerActivity.class);
                        mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        mIntent.putExtra("page_number", mPositition);
                        startActivity(mIntent);
                        overridePendingTransition(0,0);
                    }
                }, 150);
            } else {
                mDrawerLayout.closeDrawer(mDrawer);

                switch (position) {
                    case 0:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, TemplateActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0,0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 100);
                        break;

                    case 1:

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, ScheduledSms.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mIntent);
                                overridePendingTransition(0,0);
                                //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 100);
                        break;

                    case 2:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, GetHelpSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mIntent);
                                //overridePendingTransition(0,0);
                                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 100);

                        break;

                    case 3:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent mIntent = new Intent(context, OtherAppsSettingsActivity.class);
                                mIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(mIntent);
                                //overridePendingTransition(0,0);
                                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                            }
                        }, 100);

                        break;

                    case 4:
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

                                try {
                                    startActivity(goToMarket);
                                } catch (ActivityNotFoundException e) {
                                    Toast.makeText(context, "Couldn't launch the market", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }, 100);

                        //overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
                        //mDrawerLayout.closeDrawer(mDrawer);
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Pass the event to ActionBarDrawerToggle, if it returns
                // true, then it has handled the app icon touch event
                if (mDrawerToggle.onOptionsItemSelected(item)) {

                    return true;
                }

                // Handle your other action bar items...

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        writeToFile(text, this);
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
        //overridePendingTransition(0,0);
    }

    @SuppressWarnings("resource")
    private ArrayList<String[]> readFromFile(Context context, boolean tryRemove) {

        ArrayList<String[]> ret = new ArrayList<String[]>();

        if (tryRemove)
            removeOld();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt");
            } else
            {
                inputStream = context.openFileInput("scheduledSMS.txt");
            }



            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {

                    String[] details = new String[5];
                    details[0] = receiveString;

                    for(int i = 1; i < 5; i++)
                        details[i] = bufferedReader.readLine();

                    ret.add(details);
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    private void writeToFile(ArrayList<String[]> data, Context context) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt"));
            } else
            {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("scheduledSMS.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++)
            {
                String[] details = data.get(i);

                for (int j = 0; j < 5; j++)
                {
                    outputStreamWriter.write(details[j] + "\n");
                }


            }

            outputStreamWriter.close();
        }
        catch (IOException e) {

        }

    }

    public void removeOld()
    {
        ArrayList<String[]> list = readFromFile(context, false);

        for(int i = 0; i < list.size(); i++)
        {
            try {
                Date sendDate = new Date(Long.parseLong(list.get(i)[1]));
                if (sendDate.before(new Date()) && list.get(i)[2].equals("None")) // date is earlier than current and no repetition
                {
                    list.remove(i);
                    i--;
                }
            } catch (Exception e) {

            }
        }

        writeToFile(list, context);
    }

    public void cancelAlarm(int alarmId, String repetition, long date)
    {
        Intent serviceIntent = new Intent(getApplicationContext(), ScheduledService.class);

        PendingIntent pi = getDistinctPendingIntent(serviceIntent, alarmId);

        AlarmManager am = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        am.cancel(pi);
    }

    protected PendingIntent getDistinctPendingIntent(Intent intent, int requestId)
    {
        PendingIntent pi =
                PendingIntent.getService(
                        this,         //context
                        requestId,    //request id
                        intent,       //intent to be delivered
                        0);

        return pi;
    }
}