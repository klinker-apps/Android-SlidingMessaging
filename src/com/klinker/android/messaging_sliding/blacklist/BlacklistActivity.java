package com.klinker.android.messaging_sliding.blacklist;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.util.ArrayList;
import java.util.Locale;

public class BlacklistActivity extends Activity {

    public static Context context;
    public ListView contacts;
    public Button addNew;
    public SharedPreferences sharedPrefs;
    public ArrayList<BlacklistContact> individuals;
    public ArrayList<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.templates);
        contacts = (ListView) findViewById(R.id.templateListView2);
        findViewById(R.id.templateListView).setVisibility(View.GONE);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        context = this;

        individuals = IOUtil.readBlacklist(this);
        names = new ArrayList<String>();

        for (int i = 0; i < individuals.size(); i++) {
            names.add(ContactUtil.findContactName(individuals.get(i).name, this));
        }

        TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
        contacts.setAdapter(adapter);
        contacts.setStackFromBottom(false);

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

        contacts.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                new AlertDialog.Builder(context)
                        .setMessage(context.getResources().getString(R.string.delete_blacklist_contact))
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                individuals.remove(arg2);
                                names.remove(arg2);

                                TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, names);
                                contacts.setAdapter(adapter);

                                IOUtil.writeBlacklist(individuals, context);
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
                return true;
            }

        });

        contacts.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                IOUtil.writeBlacklist(individuals, context);
                Intent intent = new Intent(context, NewBlacklistActivity.class);
                intent.putExtra("com.klinker.android.messaging.BLACKLIST_NAME", individuals.get(arg2).name);
                intent.putExtra("com.klinker.android.messaging.BLACKLIST_TYPE", individuals.get(arg2).type);
                context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
            }

        });

        addNew.setText(getResources().getString(R.string.add_new_individual));

        addNew.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                IOUtil.writeBlacklist(individuals, context);
                Intent intent = new Intent(context, NewBlacklistActivity.class);
                context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);

            }

        });
    }

    @Override
    public void onResume() {
        super.onResume();

        individuals = IOUtil.readBlacklist(this);
        names = new ArrayList<String>();

        for (int i = 0; i < individuals.size(); i++) {
            names.add(individuals.get(i).name);
        }

        TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
        contacts.setAdapter(adapter);
        contacts.setStackFromBottom(false);
    }

    @Override
    public void onBackPressed() {
        IOUtil.writeBlacklist(individuals, this);
        super.onBackPressed();
        finish();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_block_all:
                if (item.isChecked()) {
                    item.setChecked(false);
                    PreferenceManager.getDefaultSharedPreferences(BlacklistActivity.this).edit().putBoolean("block_all", false).commit();
                } else {
                    new AlertDialog.Builder(this)
                            .setMessage(R.string.block_all_summary)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    item.setChecked(true);
                                    PreferenceManager.getDefaultSharedPreferences(BlacklistActivity.this).edit().putBoolean("block_all", true).commit();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_blacklist, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_block_all).setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("block_all", false));
        return super.onPrepareOptionsMenu(menu);
    }
}