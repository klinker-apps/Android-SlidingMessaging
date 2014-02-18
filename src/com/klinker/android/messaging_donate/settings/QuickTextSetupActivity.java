/*
 * Copyright 2013 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.messaging_donate.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.ContactPickerDialog;
import com.klinker.android.messaging_sliding.receivers.QuickTextService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuickTextSetupActivity extends Activity {

    private ListView contactList;
    private Button addNew;
    private SharedPreferences sharedPrefs;
    private String[] favorites;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.templates);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        ((DrawerLayout) findViewById(R.id.drawer_layout)).setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED, Gravity.LEFT);

        contactList = (ListView) findViewById(R.id.templateListView2);
        findViewById(R.id.templateListView).setVisibility(View.GONE);
        addNew = (Button) findViewById(R.id.addNewButton);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        favorites = sharedPrefs.getString("quick_text_favorites", "").split("--");
        if (favorites.length == 1 && favorites[0].equals("")) {
            favorites = new String[0];
        }

        setAdapter();

        contactList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                removeFavorite(i);
                Intent attachVCard = new Intent(QuickTextSetupActivity.this, ContactPickerDialog.class);
                startActivityForResult(attachVCard, 1);
                setAdapter();
            }
        });

        contactList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, final View view, final int position, long l) {
                new AlertDialog.Builder(QuickTextSetupActivity.this)
                        .setMessage(R.string.delete_favorite)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                removeFavorite(position);
                                setAdapter();
                            }
                        })
                        .setNegativeButton(R.string.no, null)
                        .show();
                return true;
            }
        });

        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (favorites.length >= 3) {
                    Toast.makeText(QuickTextSetupActivity.this, getString(R.string.quick_text_limit), Toast.LENGTH_LONG).show();
                    return;
                }

                if (!sharedPrefs.getBoolean("quick_text", false)) {
                    Toast.makeText(QuickTextSetupActivity.this, getString(R.string.quick_text_needs_enabled), Toast.LENGTH_LONG).show();
                    return;
                }

                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                    Toast.makeText(QuickTextSetupActivity.this, getString(R.string.need_jellybean), Toast.LENGTH_LONG).show();
                    return;
                }

                Intent attachVCard = new Intent(QuickTextSetupActivity.this, ContactPickerDialog.class);
                startActivityForResult(attachVCard, 1);
            }
        });
        addNew.setText(getString(R.string.add_favorite));
    }

    private void removeFavorite(int position) {
        String[] favs = new String[favorites.length - 1];
        int favPos = 0;
        for (int i = 0; i < favorites.length; i++) {
            if (position != i) {
                favs[favPos++] = favorites[i];
            }
        }
        favorites = favs;
    }

    @Override
    public void onStop() {
        super.onStop();
        String favs = "";

        for (String fav : favorites) {
            if (!fav.equals("")) {
                favs += fav + "--";
            }
        }

        try {
            favs = favs.substring(0, favs.length() - 2);
        } catch (Exception e) { }

        sharedPrefs.edit().putString("quick_text_favorites", favs).commit();
        startService(new Intent(this, QuickTextService.class));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == RESULT_OK) {
            String name = data.getStringExtra("name");
            String number = data.getStringExtra("number");

            addFavorite(name + ", " + number);
            setAdapter();
        }
    }

    private void addFavorite(String newFav) {
        String[] favs = new String[favorites.length + 1];
        for (int i = 0; i < favorites.length; i++) {
            favs[i] = favorites[i];
        }
        favs[favs.length - 1] = newFav;
        favorites = favs;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_quick_text, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                break;
            case R.id.menu_enable_quick_text:
                if (item.isChecked()) {
                    item.setChecked(false);
                    PreferenceManager.getDefaultSharedPreferences(QuickTextSetupActivity.this).edit().putBoolean("quick_text", false).commit();
                } else {
                    item.setChecked(true);
                    PreferenceManager.getDefaultSharedPreferences(QuickTextSetupActivity.this).edit().putBoolean("quick_text", true).commit();
                }
                startService(new Intent(this, QuickTextService.class));
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_enable_quick_text).setChecked(PreferenceManager.getDefaultSharedPreferences(this).getBoolean("quick_text", false));
        return super.onPrepareOptionsMenu(menu);
    }

    private void setAdapter() {
        List<Map<String, String>> items = new ArrayList<Map<String, String>>();

        for (String favorite : favorites) {
            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put("favorite", favorite);
            items.add(temp);
        }

        contactList.setAdapter(new SimpleAdapter(this, items, android.R.layout.simple_list_item_1, new String[]{"favorite"}, new int[]{android.R.id.text1}));

        startService(new Intent(this, QuickTextService.class));
    }
}
