package com.klinker.android.messaging_sliding.search;

import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.QuickContactBadge;
import android.widget.TextView;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;

public class SearchArrayAdapter  extends ArrayAdapter<String> {
    private final Activity context;
    private final ArrayList<String[]> messages;
    public SharedPreferences sharedPrefs;

    static class ViewHolder {
        public TextView date;
        public TextView message;

        public Button downloadButton;

        public QuickContactBadge image;

        public ImageView media;
        public ImageView ellipsis;
    }

    public SearchArrayAdapter(Activity context, ArrayList<String[]> messages) {
        super(context, R.layout.custom_scheduled);
        this.context = context;
        this.messages = messages;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount()
    {
        return messages.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        final String number = messages.get(position)[0];
        String date = messages.get(position)[2];
        String message = messages.get(position)[1];
        String type = messages.get(position)[3];
        String picture = messages.get(position)[4];

        String contactName = MainActivity.loadGroupContacts(number, context);

        Date sendDate;

        //try {
            //sendDate = new Date(Long.parseLong(text.get(position)[1]));
        //} catch (Exception e) {
            //sendDate = new Date(0);
        //}
        LayoutInflater inflater = context.getLayoutInflater();

        if (type.equals("2")) // sent
            rowView = inflater.inflate(R.layout.message_hangout_sent, null);
        else
            rowView = inflater.inflate(R.layout.message_hangout_received, null);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.date = (TextView) rowView.findViewById(R.id.textDate);
        viewHolder.message = (TextView) rowView.findViewById(R.id.textBody);
        viewHolder.media = (ImageView) rowView.findViewById(R.id.media);
        viewHolder.image = (QuickContactBadge) rowView.findViewById(R.id.imageContactPicture);

        if (type.equals("2")) // sent
            viewHolder.ellipsis = (ImageView) rowView.findViewById(R.id.ellipsis);
        else
            viewHolder.downloadButton = (Button) rowView.findViewById(R.id.downloadButton);

        rowView.setTag(viewHolder);

        final ViewHolder holder = (ViewHolder) rowView.getTag();

        //String contactName = MainActivity.loadGroupContacts(text.get(position)[0].replaceAll(";", ""), context);
        String dateString;

        /*
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
        */
        new Thread(new Runnable() {

            @Override
            public void run()
            {
                final Bitmap picture = Bitmap.createScaledBitmap(getFacebookPhoto(number), MainActivity.contactWidth, MainActivity.contactWidth, true);

                context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        holder.image.setImageBitmap(picture);
                        holder.image.assignContactFromPhone(number, true);
                    }
                });
            }

        }).start();

        holder.message.setText(message);


        if (type.equals("2")) // sent
        {
            holder.ellipsis.setVisibility(View.GONE);
            holder.date.setText(date);
        } else // sent
        {
            holder.downloadButton.setVisibility(View.GONE);
            holder.date.setText(date + " - " + contactName);

        }

        if (picture.equals("false"))
            holder.media.setVisibility(View.GONE);

        return rowView;
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
