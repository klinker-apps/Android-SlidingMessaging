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

package com.klinker.android.messaging_sliding;

import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactPickerDialog extends ListActivity implements AdapterView.OnItemClickListener {

    private ListView contactList;
    private EditText contactSearch;
    private Cursor people;
    private ArrayList<VCardContact> contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.FILL_PARENT;
        getWindow().setAttributes(params);

        setContentView(R.layout.contact_picker_dialog);

        contactList = getListView();
        contactList.setOnItemClickListener(this);
        contactSearch = (EditText) findViewById(R.id.searchBox);
        setContactAdapter();

        contactSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setContactAdapter();
            }
        });
    }

    private void setContactAdapter() {
        contacts = new ArrayList<VCardContact>();

        if (people == null) {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

            people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
        }

        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        if (people.moveToFirst()) {
            do {
                int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                contacts.add(new VCardContact(people.getString(indexName),
                        people.getString(indexNumber),
                        ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, customLabel).toString(),
                        type));
            } while (people.moveToNext());
        }

        final ArrayList<VCardContact> searchedContacts = new ArrayList<VCardContact>();

        String text = contactSearch.getText().toString();

        if (text.startsWith("+")) {
            text = text.substring(1);
        }

        Pattern pattern;

        try {
            pattern = Pattern.compile(text.toLowerCase());
        } catch (Exception e) {
            pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", "").replace("*", ""));
        }

        try {
            for (int i = 0; i < contacts.size(); i++) {
                try {
                    Long.parseLong(text);

                    if (text.length() <= contacts.get(i).number.length()) {
                        Matcher matcher = pattern.matcher(contacts.get(i).number);
                        if (matcher.find()) {
                            searchedContacts.add(contacts.get(i));
                        }
                    }
                } catch (Exception e) {
                    if (contacts == null) {
                        contacts = new ArrayList<VCardContact>();
                    }
                    if (text.length() <= contacts.get(i).name.length()) {
                        Matcher matcher = pattern.matcher(contacts.get(i).name.toLowerCase());
                        if (matcher.find()) {
                            searchedContacts.add(contacts.get(i));
                        }
                    }
                }
            }
        } catch (Exception e) {
        }

        contacts = searchedContacts;

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> numbers = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();

        for (int i = 0; i < contacts.size(); i++) {
            names.add(contacts.get(i).name);
            numbers.add(contacts.get(i).number);
            types.add(contacts.get(i).type);
        }

        contactList.setAdapter(new ContactSearchArrayAdapter(this, names, numbers, types));
    }

    public VCardContact getContact(int position) {
        return this.contacts.get(position);
    }

    @Override
    public void onItemClick(AdapterView<?> view, View v, int position, long id) {
        VCardContact contact = this.getContact(position);
        setResult(RESULT_OK, getIntent().putExtra("name", contact.name).putExtra("number", contact.number).putExtra("type", contact.typeNum));
        finish();
    }

    @Override
    public void finish() {
        people.close();
        super.finish();
    }
}