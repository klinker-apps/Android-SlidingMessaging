package com.klinker.android.messaging_sliding;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemLongClickListener;
import com.klinker.android.messaging_donate.R;

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

    @Override
    protected void onResume()
    {
        text = readFromFile(this, true);

        SchedulesArrayAdapter adapter = new SchedulesArrayAdapter(this, text);
        sms.setAdapter(adapter);
        sms.setStackFromBottom(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms);
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

                Intent intent = new Intent(context, NewScheduledSms.class);
                intent.putExtra(EXTRA_NUMBER, text.get(pos)[0]);
                intent.putExtra(EXTRA_DATE, text.get(pos)[1]);
                intent.putExtra(EXTRA_REPEAT, text.get(pos)[2]);
                intent.putExtra(EXTRA_MESSAGE, text.get(pos)[3]);

                startActivity(intent);
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
            }

        });
    }

    @Override
    public void onBackPressed() {
        writeToFile(text, this);
        super.onBackPressed();
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

                    String[] details = new String[4];
                    details[0] = receiveString;

                    for(int i = 1; i < 4; i++)
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

                for (int j = 0; j < 4; j++)
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
            Date sendDate = new Date(Long.parseLong(list.get(i)[2]));
            if (sendDate.before(new Date()) && list.get(i)[2].equals("0")) // date is earlier than current and no repetition
            {
                list.remove(i);
                i--;
            }
        }

        writeToFile(list, context);
    }
}