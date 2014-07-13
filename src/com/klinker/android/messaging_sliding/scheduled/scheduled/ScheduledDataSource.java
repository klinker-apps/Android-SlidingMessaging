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

package com.klinker.android.messaging_sliding.scheduled.scheduled;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.klinker.android.logger.Log;

import java.util.ArrayList;

public class ScheduledDataSource {
    private static final String TAG = "ScheduledDataSource";

    // Database fields
    private SQLiteDatabase database;
    private ScheduledSQLiteHelper dbHelper;
    private String[] allColumns = {
            ScheduledSQLiteHelper.COLUMN_ID,
            ScheduledSQLiteHelper.COLUMN_DATE,
            ScheduledSQLiteHelper.COLUMN_REPETITION,
            ScheduledSQLiteHelper.COLUMN_ADDRESS,
            ScheduledSQLiteHelper.COLUMN_BODY,
            ScheduledSQLiteHelper.COLUMN_ATTACHMENT
    };

    public ScheduledDataSource(Context context) {
        dbHelper = new ScheduledSQLiteHelper(context);
    }

    public void open() throws SQLException {
        Log.v(TAG, "Opened database");
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        Log.v(TAG, "closed database");
        dbHelper.close();
    }

    private ScheduledMessage getMessage(long id) {
        Log.v(TAG, "getting message at id " + id);
        Cursor cursor = database.query(ScheduledSQLiteHelper.TABLE_SCHEDULED,
                allColumns, ScheduledSQLiteHelper.COLUMN_ID + "=?", new String[] {id + ""},
                null, null, ScheduledSQLiteHelper.COLUMN_DATE + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            Log.v(TAG, "found message with id " + id);
            ScheduledMessage message = cursorToMessage(cursor);
            cursor.close();
            return message;
        } else {
            Log.v(TAG, "couldn't find message, returning null");
            return null;
        }
    }

    public ArrayList<ScheduledMessage> getMessages(String address) {
        Log.v(TAG, "getting messages for address " + address);
        Cursor cursor = database.query(ScheduledSQLiteHelper.TABLE_SCHEDULED,
                allColumns, ScheduledSQLiteHelper.COLUMN_ADDRESS + "=?", new String[] {address},
                null, null, ScheduledSQLiteHelper.COLUMN_DATE + " ASC");

        ArrayList<ScheduledMessage> messages = new ArrayList<ScheduledMessage>();
        if (cursor != null && cursor.moveToFirst()) {
            Log.v(TAG, "found messages for address, adding them to list");
            do {
                messages.add(cursorToMessage(cursor));
            } while (cursor.moveToNext());
        } else {
            Log.v(TAG, "couldn't find any messages for this address");
        }

        return messages;
    }

    public ArrayList<ScheduledMessage> getMessages() {
        Log.v(TAG, "getting messages for all addresses");
        Cursor cursor = database.query(ScheduledSQLiteHelper.TABLE_SCHEDULED,
                allColumns, null, null,
                null, null, ScheduledSQLiteHelper.COLUMN_DATE + " ASC");

        ArrayList<ScheduledMessage> messages = new ArrayList<ScheduledMessage>();
        if (cursor != null && cursor.moveToFirst()) {
            Log.v(TAG, "found message, adding them to list");
            do {
                messages.add(cursorToMessage(cursor));
            } while (cursor.moveToNext());
        } else {
            Log.v(TAG, "couldn't find any messages");
        }

        return messages;
    }

    public ScheduledMessage getFirstMessage() {
        Log.v(TAG, "getting first message");
        Cursor cursor = database.query(ScheduledSQLiteHelper.TABLE_SCHEDULED,
                allColumns, null, null,
                null, null, ScheduledSQLiteHelper.COLUMN_DATE + " ASC LIMIT 1");

        if (cursor != null && cursor.moveToFirst()) {
            ScheduledMessage message = cursorToMessage(cursor);
            cursor.close();
            return message;
        } else {
            Log.v(TAG, "couldn't find message, returning null");
            return null;
        }
    }

    public void deleteMessage(ScheduledMessage message) {
        Log.v(TAG, "pulling id from message object to delete");
        deleteMessage(message.id);
    }

    public void deleteMessage(long id) {
        try {
            Log.v(TAG, "attempting to delete message with id " + id);
            database.delete(ScheduledSQLiteHelper.TABLE_SCHEDULED, ScheduledSQLiteHelper.COLUMN_ID
                    + "=?", new String[] {id + ""});
        } catch (Exception e) {
            Log.v(TAG, "deleting failed");
            Log.e(TAG, "logging error", e);
        }
    }

    public ScheduledMessage addMessage(ScheduledMessage message) {
        Log.v(TAG, "adding new scheduled message to database");

        ContentValues values = new ContentValues();
        values.put(ScheduledSQLiteHelper.COLUMN_DATE, message.date);
        values.put(ScheduledSQLiteHelper.COLUMN_REPETITION, message.repetition);
        values.put(ScheduledSQLiteHelper.COLUMN_ADDRESS, message.address);
        values.put(ScheduledSQLiteHelper.COLUMN_BODY, message.body);
        values.put(ScheduledSQLiteHelper.COLUMN_ATTACHMENT, message.attachment);

        Log.v(TAG, "created content value, inserting into database");
        long id = database.insert(ScheduledSQLiteHelper.TABLE_SCHEDULED, null, values);
        Log.v(TAG, "inserted to id " + id);
        message.id = id;
        return message;
    }

    private ScheduledMessage cursorToMessage(Cursor cursor) {
        Log.v(TAG, "creating message from cursor");

        ScheduledMessage message = new ScheduledMessage();
        message.id = cursor.getLong(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_ID));
        message.date = cursor.getLong(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_DATE));
        message.repetition = cursor.getLong(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_REPETITION));
        message.address = cursor.getString(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_ADDRESS));
        message.body = cursor.getString(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_BODY));
        message.attachment = cursor.getString(cursor.getColumnIndex(ScheduledSQLiteHelper.COLUMN_ATTACHMENT));

        Log.v(TAG, "finished creating message...\n" + "id: " + message.id + "\ndate: " + message.date
                + "\nrepetition rate: " + message.repetition + "\naddress: " + message.address
                + "\nbody: " + message.body + "\nattachment: " + message.attachment);

        return message;
    }
}