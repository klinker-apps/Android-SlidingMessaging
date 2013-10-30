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
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

public class ContactPickerDialog extends ListActivity implements AdapterView.OnItemClickListener {

    private ListView contactList;
    private ArrayList<NewContact> contacts;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        contactList = getListView();
        setContactAdapter();
    }

    private void setContactAdapter() {
        contacts = new ArrayList<NewContact>();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};

        Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
        int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
        int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

        if (people.moveToFirst()) {
            do {
                int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));

                contacts.add(new NewContact(people.getString(indexName),
                        people.getString(indexNumber).replaceAll("[^0-9\\+]", ""),
                        ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources(), type, customLabel).toString()));
            } while (people.moveToNext());
        }

        people.close();

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> numbers = new ArrayList<String>();
        ArrayList<String> types = new ArrayList<String>();

        for (int i = 0; i < contacts.size(); i++) {
            names.add(contacts.get(i).name);
            numbers.add(contacts.get(i).number);
            types.add(contacts.get(i).type);
        }

        contactList.setAdapter(new ContactSearchArrayAdapter(this, names, numbers, types));
        contactList.setOnItemClickListener(this);
    }

    public NewContact getContact(int position) {
        return this.contacts.get(position);
    }

    @Override
    public void onItemClick(AdapterView<?> view, View v, int position, long id) {
        NewContact contact = this.getContact(position);
        setResult(RESULT_OK, getIntent().putExtra("name", contact.name).putExtra("number", contact.number).putExtra("type", contact.type));
        finish();
    }
}
