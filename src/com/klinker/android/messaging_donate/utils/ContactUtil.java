package com.klinker.android.messaging_donate.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.io.InputStream;
import java.util.Locale;

public class ContactUtil {
    public static String findContactNumber(String id, Context context) {
        try {
            String[] ids = id.split(" ");
            String numbers = "";

            for (int i = 0; i < ids.length; i++) {
                try {
                    if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" "))) {
                        Cursor number = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), null, "_id=" + ids[i], null, null);

                        if (number.moveToFirst()) {
                            numbers += number.getString(number.getColumnIndex("address")).replace("-", "").replace(")", "").replace("(", "").replace(" ", "") + " ";
                        } else {
                            numbers += ids[i] + " ";
                        }

                        number.close();
                    }
                } catch (Exception e) {
                    numbers += "0 ";
                }
            }

            return numbers;
        } catch (Exception e) {
            return id;
        }
    }

    public static String findRecipientId(String number, Context context) {
        try {
            String[] ids = number.split(" ");
            String numbers = "";

            Cursor id = context.getContentResolver().query(Uri.parse("content://mms-sms/canonical-addresses"), new String[]{"_id", "address"}, null, null, null);

            for (int i = 0; i < ids.length; i++) {
                try {
                    if (ids[i] != null && (!ids[i].equals("") || !ids[i].equals(" "))) {
                        if (id.moveToFirst()) {
                            do {
                                String numberToMatch = id.getString(id.getColumnIndex("address")).replace("+1", "").replace(" ", "").replace("-", "").replace(")", "").replace("(", "");
                                String against = ids[i].replace("+1", "").replace(" ", "").replace("-", "").replace(")", "").replace("(", "");

                                if (numberToMatch.startsWith(against) || numberToMatch.endsWith(against) || numberToMatch.equals(against)) {
                                    numbers += id.getString(id.getColumnIndex("_id")) + " ";
                                }
                            } while (id.moveToNext());
                        } else {
                            numbers += ids[i] + " ";
                        }
                    }
                } catch (Exception e) {
                    numbers += "0 ";
                }
            }

            id.close();

            return numbers.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return number;
        }
    }

    public static String findContactName(String number, Context context) {
        try {
            String name = "";

            String origin = number;

            if (origin.length() != 0) {
                Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
                Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                try {
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
                            name = "No Information";
                        }
                    }
                } finally {
                    phonesCursor.close();
                }
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
                        name = number;
                    }
                } else {
                    name = "No Information";
                }
            }

            return name;
        } catch (Exception e) {
            // something weird when looking up the number I think (mms tokens sometimes give a weird initial number instead of phone number)
            return number;
        }
    }

    public static String loadGroupContacts(String numbers, Context context) {
        String names = "";
        String[] number;

        try {
            number = numbers.split(" ");
        } catch (Exception e) {
            return "";
        }

        for (int i = 0; i < number.length; i++) {
            try {
                String origin = number[i];

                Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
                Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                try {
                    if (phonesCursor != null && phonesCursor.moveToFirst()) {
                        names += ", " + phonesCursor.getString(0);
                    } else {
                        try {
                            Locale sCachedLocale = Locale.getDefault();
                            int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                            Editable editable = new SpannableStringBuilder(number[i]);
                            PhoneNumberUtils.formatNumber(editable, sFormatType);
                            names += ", " + editable.toString();
                        } catch (Exception e) {
                            names += ", " + number;
                        }
                    }
                } finally {
                    try {
                        phonesCursor.close();
                    } catch (Exception e) {
                        // null cursor causing force close
                    }
                }
            } catch (IllegalArgumentException e) {
                try {
                    Locale sCachedLocale = Locale.getDefault();
                    int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                    Editable editable = new SpannableStringBuilder(number[i]);
                    PhoneNumberUtils.formatNumber(editable, sFormatType);
                    names += ", " + editable.toString();
                } catch (Exception f) {
                    names += ", " + number;
                }
            }
        }

        try {
            return names.substring(2);
        } catch (Exception e) {
            return "";
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable, Context context) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        try {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            if (MainActivity.settings.darkContactImage) {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
            } else {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            }
        }
    }

    public static InputStream openDisplayPhoto(long contactId, Context context) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);

        InputStream avatarDataStream = ContactsContract.Contacts.openContactPhotoInputStream(
                context.getContentResolver(),
                contactUri);

        return avatarDataStream;
    }

    public static Bitmap getFacebookPhoto(String phoneNumber, Context context) {
        try {
            if (phoneNumber.split(" ").length > 1) {
                return getGroupPhoto(phoneNumber.split(" "), context);
            }
        } catch (Exception e) {
            // problem splitting the string that caused a force close
        }

        try {
            Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Uri photoUri;
            ContentResolver cr = context.getContentResolver();
            Cursor contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null, null);

            try {
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
                    contact.close();
                } else {
                    Bitmap defaultPhoto;

                    if (!MainActivity.settings.ctDarkContactPics) {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                    } else {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }

                if (photoUri != null) {
                    InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(
                            cr, photoUri);
                    if (input != null) {
                        contact.close();
                        return BitmapFactory.decodeStream(input);
                    }
                } else {
                    Bitmap defaultPhoto;

                    if (!MainActivity.settings.ctDarkContactPics) {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                    } else {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }

                Bitmap defaultPhoto;

                if (!MainActivity.settings.ctDarkContactPics) {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                } else {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e) {
                contact.close();
                Bitmap defaultPhoto;

                if (!MainActivity.settings.ctDarkContactPics) {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                } else {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }

                return defaultPhoto;
            } finally {
                contact.close();
            }
        } catch (Exception e) {
            Bitmap defaultPhoto;

            try {
                if (!MainActivity.settings.ctDarkContactPics) {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                } else {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }
            } catch (Exception f) {
                // settings is null, so get it manually
                if (!PreferenceManager.getDefaultSharedPreferences(context).getBoolean("ct_darkContactImage", false)) {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                } else {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }
            }

            return defaultPhoto;
        }
    }

    private static Bitmap getGroupPhoto(String[] numbers, Context context) {
        try {
            switch (numbers.length) {
                case 2:
                    Bitmap[] bitmaps = new Bitmap[numbers.length];
                    for (int i = 0; i < bitmaps.length; i++) {
                        bitmaps[i] = getFacebookPhoto(numbers[i], context);
                        bitmaps[i] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[i], 180, 180, false), 45, 0, 90, 180);
                    }

                    Bitmap image = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], 90, 0, null);

                    Paint linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(90, 0, 90, 180, linePaint);
                    return image;
                case 3:
                    bitmaps = new Bitmap[numbers.length];
                    bitmaps[0] = getFacebookPhoto(numbers[0], context);
                    bitmaps[0] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[0], 180, 180, false), 45, 0, 90, 180);
                    bitmaps[1] = getFacebookPhoto(numbers[1], context);
                    bitmaps[1] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[1], 180, 180, false), 45, 45, 90, 90);
                    bitmaps[2] = getFacebookPhoto(numbers[2], context);
                    bitmaps[2] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[2], 180, 180, false), 45, 45, 90, 90);

                    image = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], 90, 0, null);
                    canvas.drawBitmap(bitmaps[2], 90, 90, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(90, 0, 90, 180, linePaint);
                    canvas.drawLine(90, 90, 180, 90, linePaint);
                    return image;
                case 4:
                    bitmaps = new Bitmap[numbers.length];
                    bitmaps[0] = getFacebookPhoto(numbers[0], context);
                    bitmaps[0] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[0], 180, 180, false), 45, 45, 90, 90);
                    bitmaps[1] = getFacebookPhoto(numbers[1], context);
                    bitmaps[1] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[1], 180, 180, false), 45, 45, 90, 90);
                    bitmaps[2] = getFacebookPhoto(numbers[2], context);
                    bitmaps[2] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[2], 180, 180, false), 45, 45, 90, 90);
                    bitmaps[3] = getFacebookPhoto(numbers[3], context);
                    bitmaps[3] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[3], 180, 180, false), 45, 45, 90, 90);

                    image = Bitmap.createBitmap(180, 180, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], 90, 0, null);
                    canvas.drawBitmap(bitmaps[2], 90, 90, null);
                    canvas.drawBitmap(bitmaps[3], 0, 90, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(90, 0, 90, 180, linePaint);
                    canvas.drawLine(0, 90, 180, 90, linePaint);
                    return image;
            }
        } catch (Exception e) {
            // fall through if an exception occurs and just show the default image
        }

        Bitmap defaultPhoto;

        if (!MainActivity.settings.ctDarkContactPics) {
            defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.group_avatar);
        } else {
            defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.group_avatar_dark);
        }

        return defaultPhoto;
    }

    public static String getMyPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }
}
