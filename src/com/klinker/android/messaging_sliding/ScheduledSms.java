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
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;

public class ScheduledSms extends Activity {

    public static Context context;
    public ListView templates;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<String> text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scheduled_sms);
        templates = (ListView) findViewById(R.id.smsListView);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        text = readFromFile(this);

        TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, text);
        templates.setAdapter(adapter);
        templates.setStackFromBottom(false);

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

        templates.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete_template))
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                text.remove(arg2);

                                TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, text);
                                templates.setAdapter(adapter);
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
                return false;
            }

        });

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final EditText input = new EditText(context);

                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.add_new))
                        .setView(input)
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputText = input.getText().toString();
                                text.add(inputText);

                                TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, text);
                                templates.setAdapter(adapter);
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();

            }

        });
    }

    @Override
    public void onBackPressed() {
        writeToFile(text, this);
        super.onBackPressed();
    }

    @SuppressWarnings("resource")
    private ArrayList<String> readFromFile(Context context) {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            InputStream inputStream;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt");
            } else
            {
                inputStream = context.openFileInput("templates.txt");
            }

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    ret.add(receiveString);
                }

                inputStream.close();
            }
        }
        catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    private void writeToFile(ArrayList<String> data, Context context) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (sharedPrefs.getBoolean("save_to_external", true))
            {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt"));
            } else
            {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("templates.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++)
            {
                outputStreamWriter.write(data.get(i) + "\n");
            }

            outputStreamWriter.close();
        }
        catch (IOException e) {

        }

    }
}