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

package com.klinker.android.messaging_sliding.quick_reply;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import com.klinker.android.messaging_donate.utils.SendUtil;

public class QuickResponseService extends Service {
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String address = "";

        if (intent.getData() != null) {
            address = intent.getData().toString();
            try {
                if (address.charAt(3) == ':') {
                    address = Uri.decode(intent.getDataString()).substring("sms:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                } else if (address.charAt(5) == ':') {
                    address = Uri.decode(intent.getDataString()).substring("smsto:".length()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                } else {
                    address = Uri.decode(intent.getDataString()).replace("(", "").replace(")", "").replace("-", "").replace(" ", "");
                }
            } catch (Exception e) {
            }
        } else {
            address = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
        }

        String body = intent.getStringExtra(Intent.EXTRA_TEXT);
        SendUtil.sendMessage(this, address, body);

        return START_NOT_STICKY;
    }

    private String parseNumber(String number) {
        return number.replace("(", "").replace(")", "").replace("-", "").replace(" ", "").replace("+1", "").replace("+", "");
    }
}
