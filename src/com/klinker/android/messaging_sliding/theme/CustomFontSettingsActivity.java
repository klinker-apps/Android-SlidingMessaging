package com.klinker.android.messaging_sliding.theme;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class CustomFontSettingsActivity extends Activity {

    public static Context context;
    public ListView fonts;
    public SharedPreferences sharedPrefs;
    public ArrayList<String> name, path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_fonts);
        fonts = (ListView) findViewById(R.id.fontListView);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        name = new ArrayList<String>();
        path = new ArrayList<String>();

        name.add(getResources().getString(R.string.default_font));
        path.add(getResources().getString(R.string.default_font_summary));

        final ProgressDialog progDialog = new ProgressDialog(context);
        progDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progDialog.setMessage(getResources().getString(R.string.find_fonts));
        progDialog.show();

        new Thread(new Runnable() {

            @Override
            public void run() {
                findFiles(Environment.getExternalStorageDirectory());

                ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        fonts.setAdapter(new CustomFontArrayAdapter((Activity) context, name, path));
                        try {
                            progDialog.dismiss();
                        } catch (Exception e) {
                            // probably just never actually showed or something because it went through to fast?
                        }
                    }

                });
            }

        }).start();

        fonts.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                if (arg2 == 0) {
                    Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putBoolean("custom_font", false);
                    prefEdit.commit();
                } else {
                    Editor prefEdit = sharedPrefs.edit();
                    prefEdit.putBoolean("custom_font", true);
                    prefEdit.putString("custom_font_path", path.get(arg2));
                    prefEdit.commit();
                }

                Toast.makeText(context, getResources().getString(R.string.font_set), Toast.LENGTH_SHORT).show();
                finish();
            }

        });

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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    public File findFiles(File dir) {
        try {
            File[] children = dir.listFiles();

            for (File child : children) {
                if (child.isDirectory()) {
                    File found = findFiles(child);

                    if (found != null) {
                        return found;
                    }
                } else {
                    if (child.getPath().endsWith(".ttf")) {
                        this.name.add(child.getName());
                        this.path.add(child.getPath());
                    }
                }
            }
        } catch (Exception e) {

        }

        return null;
    }
}