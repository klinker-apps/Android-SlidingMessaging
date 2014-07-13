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
import com.klinker.android.messaging_donate.R;

public class ScheduledMessage {

    public static final long REPEAT_NEVER = -1;
    public static final long REPEAT_DAILY = 1000 * 60 * 60 * 24;
    public static final long REPEAT_WEEKLY = REPEAT_DAILY * 7;
    public static final long REPEAT_MONTHLY = REPEAT_DAILY * 30;
    public static final long REPEAT_YEARLY = REPEAT_WEEKLY * 52;

    public long id;
    public long date;
    public long repetition;
    public String address;
    public String body;
    public String attachment;

    public static ScheduledMessage fromOldStringArray(Context context, String[] m) {
        ScheduledMessage message = new ScheduledMessage();
        message.address = m[0];
        message.date = Long.parseLong(m[1]);

        if (m[2].equals(context.getString(R.string.never)))
            message.repetition = ScheduledMessage.REPEAT_NEVER;
        else if (m[2].equals(context.getString(R.string.daily)))
            message.repetition = ScheduledMessage.REPEAT_DAILY;
        else if (m[2].equals(context.getString(R.string.weekly)))
            message.repetition = ScheduledMessage.REPEAT_WEEKLY;
        else if (m[2].equals(context.getString(R.string.monthly)))
            message.repetition = ScheduledMessage.REPEAT_MONTHLY;
        else if (m[2].equals(context.getString(R.string.yearly)))
            message.repetition = ScheduledMessage.REPEAT_YEARLY;

        message.body = m[3];
        message.attachment = "";
        return message;
    }

}
