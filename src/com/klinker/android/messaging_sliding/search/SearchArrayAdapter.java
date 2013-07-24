package com.klinker.android.messaging_sliding.search;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;

public class SearchArrayAdapter  extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String[]> messages;
    public SharedPreferences sharedPrefs;
    public static boolean needMyPicture;
    public static String myContactId = "";
    public static String myPhoneNumber;
    public static String myId;
    public Typeface font;



    static class ViewHolder {
        public TextView date;
        public TextView message;
        public LinearLayout background;
        public ImageView bubble;

        public String number;

        public QuickContactBadge image;
    }

    public SearchArrayAdapter(Activity context, ArrayList<String[]> messages) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.messages = messages;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        needMyPicture = true;
        myPhoneNumber = getMyPhoneNumber();

        font = null;

        if (sharedPrefs.getBoolean("custom_font", false))
        {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (messages.get(position)[3].equals("2") || messages.get(position)[3].equals("3") || messages.get(position)[3].equals("4") || messages.get(position)[3].equals("5") || messages.get(position)[3].equals("6")) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount()
    {
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

        if (sharedPrefs.getBoolean("hour_format", false))
        {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else
        {
            dateString = DateFormat.getDateInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        if (sharedPrefs.getBoolean("hour_format", false))
        {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(sendDate);
        } else
        {
            dateString += " " + DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(sendDate);
        }

        // gets the me contact picture the first time through
        if (needMyPicture)
        {
            String[] mProjection = new String[]
                    {
                            ContactsContract.Profile._ID
                    };

            Cursor mProfileCursor = context.getContentResolver().query(
                    ContactsContract.Profile.CONTENT_URI,
                    mProjection ,
                    null,
                    null,
                    null);

            try
            {
                if (mProfileCursor.moveToFirst())
                {
                    myContactId = mProfileCursor.getString(mProfileCursor.getColumnIndex(ContactsContract.Profile._ID));
                }
            } catch (Exception e)
            {
                myContactId = myPhoneNumber;
            } finally
            {
                mProfileCursor.close();
            }

            myId = myContactId;

            needMyPicture = false;
        }

        if (rowView == null) {
            LayoutInflater inflater = context.getLayoutInflater();

            if (sharedPrefs.getString("run_as", "classic").equals("hangout")) {
                if (sent)
                    rowView = inflater.inflate(R.layout.message_hangout_sent, null);
                else
                    rowView = inflater.inflate(R.layout.message_hangout_received, null);
            } else {
                if (sent)
                    rowView = inflater.inflate(R.layout.message_classic_sent, null);
                else
                    rowView = inflater.inflate(R.layout.message_classic_received, null);
            }

            ViewHolder viewHolder = new ViewHolder();

            viewHolder.date = (TextView) rowView.findViewById(R.id.textDate);
            viewHolder.message = (TextView) rowView.findViewById(R.id.textBody);
            viewHolder.image = (QuickContactBadge) rowView.findViewById(R.id.imageContactPicture);
            viewHolder.background = (LinearLayout) rowView.findViewById(R.id.messageBody);
            viewHolder.bubble = (ImageView) rowView.findViewById(R.id.msgBubble);
            rowView.findViewById(R.id.media).setVisibility(View.GONE);

            try {
                rowView.findViewById(R.id.downloadButton).setVisibility(View.GONE);
            } catch (Exception e) {
                rowView.findViewById(R.id.ellipsis).setVisibility(View.GONE);
            }

            if (sharedPrefs.getBoolean("custom_font", false))
            {
                viewHolder.message.setTypeface(font);
                viewHolder.date.setTypeface(font);
            }

            try {
                viewHolder.message.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
                viewHolder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)) - 4);
            } catch (Exception e) {
                viewHolder.message.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)));
                viewHolder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)) - 4);
            }

            if (sharedPrefs.getBoolean("tiny_date", false))
            {
                viewHolder.date.setTextSize(10);
            }

            viewHolder.date.setAlpha((float) .5);

            if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
                rowView.setPadding(10,5,10,5);
            }

            if (sent) {
                viewHolder.message.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.date.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.background.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
                viewHolder.bubble.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));

                if (!sharedPrefs.getBoolean("custom_theme", false))
                {
                    String color = sharedPrefs.getString("sent_text_color", "default");

                    if (color.equals("blue"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_blue));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_green));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_orange));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_red));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_purple));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.pitch_black));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.grey));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.grey));
                    }
                }
            } else {
                viewHolder.message.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.date.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.background.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
                viewHolder.bubble.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));

                if (!sharedPrefs.getBoolean("custom_theme", false))
                {
                    String color = sharedPrefs.getString("received_text_color", "default");

                    if (color.equals("blue"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_blue));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.white));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_green));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_orange));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_red));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.holo_purple));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.pitch_black));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        viewHolder.message.setTextColor(context.getResources().getColor(R.color.grey));
                        viewHolder.date.setTextColor(context.getResources().getColor(R.color.grey));
                    }
                }
            }

            if (!sharedPrefs.getString("text_alignment", "split").equals("split"))
            {
                if (sharedPrefs.getString("text_alignment", "split").equals("right"))
                {
                    viewHolder.message.setGravity(Gravity.RIGHT);
                    viewHolder.date.setGravity(Gravity.RIGHT);
                } else
                {
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

        if (sharedPrefs.getBoolean("ct_darkContactImage", false))
        {
            holder.image.setImageResource(R.drawable.default_avatar_dark);
        } else
        {
            holder.image.setImageResource(R.drawable.default_avatar);
        }

        final boolean sentF = sent;
        final String dateStringF = dateString;

        new Thread(new Runnable() {

            @Override
            public void run()
            {
                try {
                    Thread.sleep(250);
                } catch (Exception e) {

                }

                if (number.equals(holder.number)) {
                    // view has not been recycled, so not fast scrolling and should post image
                    final String contactName = MainActivity.loadGroupContacts(number, context);

                    if(!sentF)
                    {
                        final Bitmap picture = Bitmap.createScaledBitmap(getFacebookPhoto(number), MainActivity.contactWidth, MainActivity.contactWidth, true);

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.image.setImageBitmap(picture);
                                holder.image.assignContactFromPhone(number, true);

                                if (sentF) {
                                    holder.date.setText(dateStringF);
                                } else {
                                    holder.date.setText(dateStringF + " - " + contactName);
                                }
                            }
                        });
                    } else
                    {
                        InputStream input2;
                        try
                        {
                            input2 = openDisplayPhoto(Long.parseLong(myId));
                        } catch (NumberFormatException e)
                        {
                            input2 = null;
                        }

                        if (input2 == null)
                        {
                            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                            {
                                input2 = context.getResources().openRawResource(R.drawable.default_avatar_dark);
                            } else
                            {
                                input2 = context.getResources().openRawResource(R.drawable.default_avatar);
                            }
                        }

                        Bitmap im;

                        try
                        {
                            im = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input2), MainActivity.contactWidth, MainActivity.contactWidth, true);
                        } catch (Exception e)
                        {
                            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                            {
                                im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true);
                            } else
                            {
                                im = Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar)), MainActivity.contactWidth, MainActivity.contactWidth, true);
                            }
                        }

                        final Bitmap image = im;

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.image.setImageBitmap(image);
                                holder.image.assignContactFromPhone(number, true);

                                if (sentF) {
                                    holder.date.setText(dateStringF);
                                } else {
                                    holder.date.setText(dateStringF + " - " + contactName);
                                }
                            }
                        });
                    }
                }
            }

        }).start();

        holder.message.setText(message);

        return rowView;
    }

    public Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        try
        {
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e)
        {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
            } else
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            }
        }
    }

    public InputStream openDisplayPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = context.getContentResolver().query(photoUri,
                new String[] {ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
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

    private String getMyPhoneNumber(){
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    public Bitmap getFacebookPhoto(String phoneNumber) {
        try
        {
            Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
            Uri photoUri = null;
            ContentResolver cr = context.getContentResolver();
            Cursor contact = cr.query(phoneUri,
                    new String[] { ContactsContract.Contacts._ID }, null, null, null);

            try
            {
                if (contact.moveToFirst()) {
                    long userId = contact.getLong(contact.getColumnIndex(ContactsContract.Contacts._ID));
                    photoUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, userId);
                    contact.close();
                }
                else {
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                    {
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

                    if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);

                if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e)
            {
                if (sharedPrefs.getBoolean("ct_darkContactImage", false))
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
                } else
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
                }
            }
        } catch (Exception e)
        {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar_dark);
            } else
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar);
            }
        }
    }
}
