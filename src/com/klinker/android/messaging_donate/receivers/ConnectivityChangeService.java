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

package com.klinker.android.messaging_donate.receivers;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_donate.utils.Util;

public class ConnectivityChangeService extends IntentService {

    private static final String LOGTAG = "sliding_messaging_connectivity_change";

    public ConnectivityChangeService() {
        super("connectivity_change");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.v(LOGTAG, "starting service for connectivity to attempt resending messages");

        if (!Util.isDefaultSmsApp(this)) {
            return;
        }

        Cursor query = getContentResolver().query(
                Uri.parse("content://sms/"),
                new String[] {"_id", "body", "address", "date"},
                "type=?",
                new String[] {"5"},
                "date desc"
        );

        if (query != null && query.moveToFirst()) {
            Log.v(LOGTAG, "found messages that need to be resent");
            do {
                Log.v(LOGTAG, "sending message with body: " + query.getString(query.getColumnIndex("body")));
                resendMessage(query);
            } while (query.moveToNext());
        }
    }

    private void resendMessage(Cursor message) {
        SendUtil.sendMessage(
                this,
                message.getString(message.getColumnIndex("address")),
                message.getString(message.getColumnIndex("body"))
        );

        getContentResolver().delete(
                Uri.parse("content://sms/"),
                "_id=?",
                new String[] {message.getString(message.getColumnIndex("_id"))}
        );
    }
}
