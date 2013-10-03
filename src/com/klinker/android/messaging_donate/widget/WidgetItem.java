package com.klinker.android.messaging_donate.widget;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import com.klinker.android.messaging_donate.R;

import java.util.Locale;

public class WidgetItem {
    public String name;
    public String count;
    public String number;
    public String preview;
    public String read;

    public WidgetItem(String name, String count, String number, String text, String read, Context context) {
        this.name = name;
        this.count = count;
        this.number = number;
        this.preview = text;
        this.read = read;

        String[] ids = name.split(" ");
        this.number = "";

        for (int i = 0; i < ids.length; i++) {
            try {
                if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" "))) {
                    Cursor numbers = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);

                    if (numbers.moveToFirst()) {
                        this.number += numbers.getString(numbers.getColumnIndex("address")).replaceAll("-", "").replaceAll("\\)", "").replaceAll("\\(", "").replaceAll(" ", "") + " ";
                    } else {
                        this.number += "0 ";
                    }

                    numbers.close();
                } else {

                }
            } catch (Exception e) {
                this.number += "0 ";
            }
        }

        this.name = "";

        String[] numbers = this.number.split(" ");

        for (int i = 0; i < numbers.length; i++) {
            try {
                this.name += findContactName(numbers[i].replace("-", "").replace("+", "").replace(" ", "").replace("(", "").replace(")", ""), context) + " ";
            } catch (Exception e) {
                this.name += numbers[i] + " ";
            }
        }

        if (this.name.replace("-", "").replace("+", "").equals(this.number)) {
            this.number = "";
        }

        if (numbers.length > 1) {
            this.number = this.name;
            this.name = "Group MMS";
        }

        if (this.read.equals("1")) {
            this.read = "";
        } else {
            this.read = context.getResources().getString(R.string.unread);
        }
    }

    public String findContactName(String number, Context context) {
        String name = "";

        String origin = number;

        try {
            if (origin.length() != 0) {
                Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
                Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                if (phonesCursor != null && phonesCursor.moveToFirst()) {
                    name = phonesCursor.getString(0);
                } else {
                    if (!number.equals("")) {
                        try {
                            Locale sCachedLocale = Locale.getDefault();
                            int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                            Editable editable = new SpannableStringBuilder(number);
                            PhoneNumberUtils.formatNumber(editable, sFormatType);
                            name = editable.toString();
                        } catch (Exception e) {
                            name = number;
                        }
                    } else {
                    }
                }

                phonesCursor.close();
            } else {
                if (!number.equals("")) {
                    try {
                        Long.parseLong(number.replaceAll("[^0-9]", ""));
                        Locale sCachedLocale = Locale.getDefault();
                        int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                        Editable editable = new SpannableStringBuilder(number);
                        PhoneNumberUtils.formatNumber(editable, sFormatType);
                        name = editable.toString();
                    } catch (Exception e) {
                        try {
                            name = number;
                        } catch (Exception f) {
                        }
                    }
                } else {
                }
            }
        } catch (Exception e) {
            name = number;
        }

        return name;
    }
}
