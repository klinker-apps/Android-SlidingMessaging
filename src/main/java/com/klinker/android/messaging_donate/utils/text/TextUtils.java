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

package com.klinker.android.messaging_donate.utils.text;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import java.util.regex.Matcher;

public class TextUtils {

    public static void linkifyText(Context context, TextView textView, View holder, boolean clickable) {
        Linkify.TransformFilter filter = new Linkify.TransformFilter() {
            public final String transformUrl(final Matcher match, String url) {
                return match.group();
            }
        };

        textView.setLinksClickable(clickable);

        Linkify.addLinks(context, textView, Regex.EMAIL_ADDRESS_PATTERN, null, filter, textView, holder);
        Linkify.addLinks(context, textView, Regex.WEB_URL_PATTERN, Linkify.sUrlMatchFilter, filter, textView, holder);
        Linkify.addLinks(context, textView, Regex.PHONE_PATTERN, Linkify.sPhoneNumberMatchFilter, filter, textView, holder);
    }
}
