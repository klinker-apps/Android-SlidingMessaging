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
import android.content.Intent;
import android.net.Uri;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.utils.ContactUtil;

public class LongClickableSpan extends ClickableSpan {

    public LongClickableSpan(Context context, String value) {
        mContext = context;
        mValue = value;
        mThemeColor = MainActivity.settings.linkColor;
    }

    private final Context mContext;
    private final String mValue;
    private int mThemeColor;

    @Override
    public void onClick(View widget) {
        if (Regex.EMAIL_ADDRESS_PATTERN.matcher(mValue).find()) {
            ContactUtil.showContactDialog(mContext, mValue, widget);
        } else if (Regex.WEB_URL_PATTERN.matcher(mValue).find()) {
            String data = mValue.replace("http://", "").replace("https://", "");
            Intent intent = new Intent(Intent.ACTION_VIEW).setData(
                    Uri.parse("http://" + data)
            );
            mContext.startActivity(intent);
        } else if (Regex.PHONE_PATTERN.matcher(mValue).find()) {
            ContactUtil.showContactDialog(mContext, mValue, widget);
        }
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setUnderlineText(true);
        ds.setColor(mThemeColor);
    }

    public void onLongClick(View v) {
        // do nothing on a long click
    }
}