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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScheduledSQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_SCHEDULED = "scheduled_sms";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "next_date";
    public static final String COLUMN_REPETITION = "repetition_rate";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_BODY = "body";
    public static final String COLUMN_ATTACHMENT = "attachment";

    public static final String DATABASE_NAME = "scheduled_sms.db";
    private static final int DATABASE_VERSION = 1;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_SCHEDULED + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY," + COLUMN_DATE + " INTEGER,"
            + COLUMN_REPETITION + " INTEGER," + COLUMN_ATTACHMENT + " TEXT,"
            + COLUMN_ADDRESS + " TEXT,"  + COLUMN_BODY + " TEXT" + ")";

    public ScheduledSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULED);
        onCreate(db);
    }
}