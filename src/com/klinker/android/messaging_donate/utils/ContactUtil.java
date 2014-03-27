package com.klinker.android.messaging_donate.utils;

import android.app.AlertDialog;
import android.content.*;
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
import android.util.Log;
import android.view.View;
import android.widget.Toast;
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
                contactUri,
                true);

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
                            cr, photoUri, true);
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

    private static final int GROUP_RES = 500;
    private static Bitmap getGroupPhoto(String[] numbers, Context context) {
        try {
            switch (numbers.length) {
                case 2:
                    Bitmap[] bitmaps = new Bitmap[numbers.length];
                    for (int i = 0; i < bitmaps.length; i++) {
                        bitmaps[i] = getFacebookPhoto(numbers[i], context);
                        bitmaps[i] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[i], GROUP_RES, GROUP_RES, false), GROUP_RES/4, 0, GROUP_RES/2, GROUP_RES);
                    }

                    Bitmap image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES/2, 0, null);

                    Paint linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(GROUP_RES/2, 0, GROUP_RES/2, GROUP_RES, linePaint);
                    return image;
                case 3:
                    bitmaps = new Bitmap[numbers.length];
                    bitmaps[0] = getFacebookPhoto(numbers[0], context);
                    bitmaps[0] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[0], GROUP_RES, GROUP_RES, false), GROUP_RES/4, 0, GROUP_RES/2, GROUP_RES);
                    bitmaps[1] = getFacebookPhoto(numbers[1], context);
                    bitmaps[1] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[1], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);
                    bitmaps[2] = getFacebookPhoto(numbers[2], context);
                    bitmaps[2] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[2], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);

                    image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES/2, 0, null);
                    canvas.drawBitmap(bitmaps[2], GROUP_RES/2, GROUP_RES/2, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(GROUP_RES/2, 0, GROUP_RES/2, GROUP_RES, linePaint);
                    canvas.drawLine(GROUP_RES/2, GROUP_RES/2, GROUP_RES, GROUP_RES/2, linePaint);
                    return image;
                case 4:
                    bitmaps = new Bitmap[numbers.length];
                    bitmaps[0] = getFacebookPhoto(numbers[0], context);
                    bitmaps[0] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[0], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);
                    bitmaps[1] = getFacebookPhoto(numbers[1], context);
                    bitmaps[1] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[1], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);
                    bitmaps[2] = getFacebookPhoto(numbers[2], context);
                    bitmaps[2] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[2], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);
                    bitmaps[3] = getFacebookPhoto(numbers[3], context);
                    bitmaps[3] = Bitmap.createBitmap(Bitmap.createScaledBitmap(bitmaps[3], GROUP_RES, GROUP_RES, false), GROUP_RES/4, GROUP_RES/4, GROUP_RES/2, GROUP_RES/2);

                    image = Bitmap.createBitmap(GROUP_RES, GROUP_RES, Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(image);
                    canvas.drawBitmap(bitmaps[0], 0, 0, null);
                    canvas.drawBitmap(bitmaps[1], GROUP_RES/2, 0, null);
                    canvas.drawBitmap(bitmaps[2], GROUP_RES/2, GROUP_RES/2, null);
                    canvas.drawBitmap(bitmaps[3], 0, GROUP_RES/2, null);

                    linePaint = new Paint();
                    linePaint.setStrokeWidth(1f);
                    linePaint.setColor(context.getResources().getColor(R.color.shadow));

                    canvas.drawLine(GROUP_RES/2, 0, GROUP_RES/2, GROUP_RES, linePaint);
                    canvas.drawLine(0, GROUP_RES/2, GROUP_RES, GROUP_RES/2, linePaint);
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

    public static boolean doesContactExist(String number, Context context) {
        try {
            String origin = number;

            Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));

            Cursor phonesCursor;

            try {
                phonesCursor = context.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");
            } catch (Exception e) {
                return false;
            }

            try {
                if (phonesCursor != null && phonesCursor.moveToFirst()) {
                    return true;
                } else {
                    return false;
                }
            } finally {
                if (phonesCursor != null) {
                    phonesCursor.close();
                }
            }
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static Uri getContactId(Context context, String number) throws Exception {
        ContentResolver contentResolver = context.getContentResolver();

        if (!number.contains("@")) {
            String id;
            Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));

            Cursor cursor =
                    contentResolver.query(
                            uri,
                            new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID},
                            null,
                            null,
                            null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID));
                } while (cursor.moveToNext());
                cursor.close();
            } else {
                throw new Exception("no phone number");
            }

            return Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, String.valueOf(id));
        } else {
            ContentResolver cr = context.getContentResolver();
            String[] PROJECTION = new String[] { ContactsContract.RawContacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.PHOTO_ID,
                    ContactsContract.CommonDataKinds.Email.DATA,
                    ContactsContract.CommonDataKinds.Photo.CONTACT_ID,
                    ContactsContract.Contacts.LOOKUP_KEY};
            String order = "CASE WHEN "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + " NOT LIKE '%@%' THEN 1 ELSE 2 END, "
                    + ContactsContract.Contacts.DISPLAY_NAME
                    + ", "
                    + ContactsContract.CommonDataKinds.Email.DATA
                    + " COLLATE NOCASE";
            String filter = ContactsContract.CommonDataKinds.Email.DATA + " NOT LIKE ''";
            Cursor cur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, PROJECTION, filter, null, order);

            if (cur.moveToFirst()) {
                Log.v("email_search", "moved to first");
                Log.v("email_search", "length of cursor: " + cur.getCount());

                do {
                    String email = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                    Log.v("email_search", "found address: " + email);
                    if (email.equals(number)) {
                        Log.v("email_search", "found contact with name " + cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
                        String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                        Log.v("email_search", "lookup key: " + lookupKey);
                        Uri lookupUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookupKey);
                        Uri uri = ContactsContract.Contacts.lookupContact(cr, lookupUri);
                        Log.v("email_search", "uri: " + uri.toString());
                        cur.close();
                        return uri;
                    }
                } while (cur.moveToNext());
            }

            throw new Exception("not found");
        }
    }

    public static void showContactDialog(final Context context, final String number, View base) {
        try {
            Uri uri = getContactId(context, number);
            ContactsContract.QuickContact.showQuickContact(context, base, uri, ContactsContract.QuickContact.MODE_LARGE, null);
        } catch (Exception e) {
            if (!number.contains("@")) {
                new AlertDialog.Builder(context)
                        .setItems(MessageUtil.parseNumber(number).equals(MessageUtil.parseNumber(number)) ? R.array.contactOptionsSave : R.array.contactOptions, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setData(Uri.parse("tel:" + number));
                                        callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        context.startActivity(callIntent);
                                        break;
                                    case 1:
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Address", number);
                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                                        context.startActivity(intent);
                                }
                            }
                        })
                        .show();
            } else {
                new AlertDialog.Builder(context)
                        .setItems(R.array.contactOptionsSaveEmail, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case 0:
                                        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Address", number);
                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Intent intent = new Intent(Intent.ACTION_INSERT);
                                        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
                                        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, number);
                                        context.startActivity(intent);
                                        break;
                                }
                            }
                        })
                        .show();
            }
        }
    }

}
