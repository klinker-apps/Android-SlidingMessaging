package com.klinker.android.messaging_sliding.search;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ConversationArrayAdapter extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String[]> messages;
    public SharedPreferences sharedPrefs;
    public static boolean needMyPicture;
    public static String myContactId = "";
    public static String myPhoneNumber;
    public static String myId;
    public Typeface font;
    public Bitmap myImage;
    public Bitmap contactImage;
    public String searchQuery;


    static class ViewHolder {
        public TextView date;
        public TextView message;
        public View background;
        public ImageView bubble;

        public String number;

        public QuickContactBadge image;
    }

    public ConversationArrayAdapter(Activity context, ArrayList<String[]> messages, String text) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.messages = messages;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.searchQuery = text;
        needMyPicture = true;
        myPhoneNumber = getMyPhoneNumber();

        font = null;

        if (sharedPrefs.getBoolean("custom_font", false)) {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
        }

        String[] mProjection = new String[]
                {
                        ContactsContract.Profile._ID
                };

        Cursor mProfileCursor = context.getContentResolver().query(
                ContactsContract.Profile.CONTENT_URI,
                mProjection,
                null,
                null,
                null);

        try {
            if (mProfileCursor.moveToFirst()) {
                myContactId = mProfileCursor.getString(mProfileCursor.getColumnIndex(ContactsContract.Profile._ID));
            }
        } catch (Exception e) {
            myContactId = myPhoneNumber;
        } finally {
            mProfileCursor.close();
        }

        myId = myContactId;

        InputStream input2;
        try {
            input2 = openDisplayPhoto(Long.parseLong(myId));
        } catch (NumberFormatException e) {
            input2 = null;
        }

        if (input2 == null) {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                input2 = context.getResources().openRawResource(R.drawable.default_avatar_dark);
            } else {
                input2 = context.getResources().openRawResource(R.drawable.default_avatar);
            }
        }

        Bitmap im;

        try {
            im = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input2), MainActivity.contactWidth, MainActivity.contactWidth, true);
        } catch (Exception e) {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true);
            } else {
                im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar)), MainActivity.contactWidth, MainActivity.contactWidth, true);
            }
        }

        myImage = im;

        int i = 0;

        while (messages.get(i)[3].equals("1"))
            i++;

        contactImage = Bitmap.createScaledBitmap(getFacebookPhoto(messages.get(i)[0]), MainActivity.contactWidth, MainActivity.contactWidth, true);
    }

    @Override
    public int getItemViewType(int position) {
        try {
            if (messages.get(position)[3].equals("2") || messages.get(position)[3].equals("3") || messages.get(position)[3].equals("4") || messages.get(position)[3].equals("5") || messages.get(position)[3].equals("6")) {
                return 1;
            } else {
                return 0;
            }
        } catch (Exception e) {

        }

        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        // Info from array list that was sent in
        final String number = messages.get(position)[0];
        String date = messages.get(position)[2];
        String message = messages.get(position)[1];
        final String type = messages.get(position)[3];
        boolean sent;

        if (message.toUpperCase().contains(searchQuery.toUpperCase()))
            message = changeTextColor(message, searchQuery);

        if (getItemViewType(position) == 1) {
            sent = true;
        } else {
            sent = false;
        }

        // Sets up the date
        Date sendDate;

        try {
            sendDate = new Date(Long.parseLong(date));
        } catch (Exception e) {
            sendDate = new Date(0);
        }

        String dateString;

        if (sharedPrefs.getBoolean("hour_format", false)) {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        if (sharedPrefs.getBoolean("hour_format", false)) {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();

            if (sharedPrefs.getString("run_as", "classic").equals("hangout")) {
                if (sent)
                    rowView = inflater.inflate(R.layout.message_hangout_sent, null);
                else
                    rowView = inflater.inflate(R.layout.message_hangout_received, null);
            } else if (sharedPrefs.getString("run_as", "sliding").equals("sliding")) {
                if (sent)
                    rowView = inflater.inflate(R.layout.message_classic_sent, null);
                else
                    rowView = inflater.inflate(R.layout.message_classic_received, null);
            } else {
                if (sent)
                    rowView = inflater.inflate(R.layout.message_card2_sent, null);
                else
                    rowView = inflater.inflate(R.layout.message_card2_received, null);
            }

            if (sharedPrefs.getString("run_as", "sliding").equals("card2")) {
                String themeName = sharedPrefs.getString("ct_theme_name", "Light Theme");

                if (themeName.equals("Light Theme") || themeName.equals("Hangouts Theme") || themeName.equals("Light Theme 2.0") || themeName.equals("Light Green Theme") || themeName.equals("Burnt Orange Theme")) {

                } else {
                    rowView.findViewById(R.id.shadow).setVisibility(View.GONE);
                }

                if (sent) {
                    rowView.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)), "44")));
                } else {
                    rowView.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)), "44")));
                }
            }

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.date = (TextView) rowView.findViewById(R.id.textDate);
            viewHolder.message = (TextView) rowView.findViewById(R.id.textBody);
            viewHolder.image = (QuickContactBadge) rowView.findViewById(R.id.imageContactPicture);
            viewHolder.background = rowView.findViewById(R.id.messageBody);
            viewHolder.bubble = (ImageView) rowView.findViewById(R.id.msgBubble);
            rowView.findViewById(R.id.media).setVisibility(View.GONE);

            if (sharedPrefs.getString("run_as", "classic").equals("classic")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 64, context.getResources().getDisplayMetrics());
                viewHolder.image.setMaxWidth(scale);
                viewHolder.image.setMaxHeight(scale);
                viewHolder.image.setMinimumHeight(scale);
                viewHolder.image.setMinimumWidth(scale);
            }

            try {
                rowView.findViewById(R.id.downloadButton).setVisibility(View.GONE);
            } catch (Exception e) {
                rowView.findViewById(R.id.ellipsis).setVisibility(View.GONE);
            }

            if (sharedPrefs.getBoolean("custom_font", false)) {
                viewHolder.message.setTypeface(font);
                viewHolder.date.setTypeface(font);
            }

            try {
                viewHolder.message.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
                viewHolder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)) - 4);
            } catch (Exception e) {
                viewHolder.message.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)));
                viewHolder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)) - 4);
            }

            if (sharedPrefs.getBoolean("tiny_date", false)) {
                viewHolder.date.setTextSize(10);
            }

            viewHolder.date.setAlpha((float) .5);

            if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
                rowView.setPadding(10, 5, 10, 5);
            }

            if (sent) {
                viewHolder.message.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.date.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.background.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
                viewHolder.bubble.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));

                if (!sharedPrefs.getBoolean("custom_theme", false)) {
                    String color = sharedPrefs.getString("sent_text_color", "default");

                    if (color.equals("blue")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_blue));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.white));
                    } else if (color.equals("green")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_green));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_orange));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_red));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_purple));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.pitch_black));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.grey));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.grey));
                    }
                }
            } else {
                viewHolder.message.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.date.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.background.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
                viewHolder.bubble.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));

                if (!sharedPrefs.getBoolean("custom_theme", false)) {
                    String color = sharedPrefs.getString("received_text_color", "default");

                    if (color.equals("blue")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_blue));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.white));
                    } else if (color.equals("green")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_green));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_orange));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_red));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_purple));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.pitch_black));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey")) {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.grey));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.grey));
                    }
                }
            }

            if (!sharedPrefs.getString("text_alignment", "split").equals("split")) {
                if (sharedPrefs.getString("text_alignment", "split").equals("right")) {
                    viewHolder.message.setGravity(Gravity.RIGHT);
                    viewHolder.date.setGravity(Gravity.RIGHT);
                } else {
                    viewHolder.message.setGravity(Gravity.LEFT);
                    viewHolder.date.setGravity(Gravity.LEFT);
                }
            } else if (!sharedPrefs.getBoolean("contact_pictures", true)) {
                if (!sent) {
                    viewHolder.message.setGravity(Gravity.LEFT);
                    viewHolder.date.setGravity(Gravity.LEFT);
                } else {
                    viewHolder.message.setGravity(Gravity.RIGHT);
                    viewHolder.date.setGravity(Gravity.RIGHT);
                }
            }

            rowView.setTag(viewHolder);
        }

        final ViewHolder holder = (ViewHolder) rowView.getTag();
        holder.number = number;

        if (!sent) {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                holder.image.setImageResource(R.drawable.default_avatar_dark);
            } else {
                holder.image.setImageResource(R.drawable.default_avatar);
            }
        }

        final boolean sentF = sent;
        final String dateStringF = dateString;

        // needs replaced
        if (!sent) {

            holder.image.setImageBitmap(contactImage);
            holder.image.assignContactFromPhone(number, true);
            holder.date.setText(dateStringF);

        } else {
            holder.image.setImageBitmap(myImage);
            holder.image.assignContactUri(ContactsContract.Profile.CONTENT_URI);
            holder.date.setText(dateStringF);
        }

        holder.message.setText(Html.fromHtml(message));

        if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, context.getResources().getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics());
            int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, context.getResources().getDisplayMetrics());

            if (sent) {
                rowView.setPadding(scale3, scale2, scale, scale2);
            } else {
                rowView.setPadding(scale, scale2, scale3, scale2);
            }
        } else if (sharedPrefs.getString("run_as", "sliding").equals("card2")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, context.getResources().getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, context.getResources().getDisplayMetrics());
            rowView.setPadding(scale, scale2, scale, 0);
        } else {
            rowView.setPadding(0, 0, 0, 0);
        }

        return rowView;
    }

    public String changeTextColor(String text, String originalSearch) {
        int index = text.toUpperCase().indexOf(originalSearch.toUpperCase());

        String first = text.substring(0, index);
        String last = text.substring(index + originalSearch.length(), text.length());

        String mySearch = text.substring(index, index + originalSearch.length());

        mySearch = "<font color=#FF0000>" + mySearch + "</font>";

        first = first + mySearch;

        if (last.toUpperCase().contains(originalSearch.toUpperCase()))
            last = changeTextColor(last, originalSearch);

        return first + last;
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
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
            if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
            } else {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            }
        }
    }

    public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return new ByteArrayInputStream(data);
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    private String getMyPhoneNumber() {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public Bitmap getFacebookPhoto(String phoneNumber) {
        try {
            Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Uri photoUri = null;
            ContentResolver cr = context.getContentResolver();
            Cursor contact = cr.query(phoneUri,
                    new String[]{ContactsContract.Contacts._ID}, null, null, null);

            try {
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
                    contact.close();
                } else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
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
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);

                if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e) {
                if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                } else {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                }
            }
        } catch (Exception e) {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false)) {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
            } else {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            }
        }
    }

    public static String convertToARGB(int color, String a) {
        String alpha = a;
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    public static int convertToColorInt(String argb) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        } else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
}
