package com.klinker.android.messaging_sliding.batch_delete;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.PhoneLookup;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.QuickContactBadge;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class BatchDeleteAllArrayAdapter extends ArrayAdapter<String> {
    public static ArrayList<Integer> itemsToDelete = new ArrayList<Integer>();;
    public static boolean checkedAll = false;

    private final Activity context;
    private final ArrayList<String> body;
    private final ArrayList<String> numbers;
    private final ArrayList<String> group;
    private SharedPreferences sharedPrefs;

    public boolean customFont;
    public String customFontPath;
    public boolean customTheme;
    public int ctSummaryTextColor;
    public String textSize2;
    public boolean contactPictures2;
    public boolean ctDarkContactPics;
    public boolean hideMessageCounter;
    public int ctMessageCounterColor;
    public String smilies;
    public boolean emojiType;
    public boolean smiliesType;
    public boolean hourFormat;
    public boolean customBackground;
    public int ctUnreadConversationColor;
    public int ctConversationListBackground;

    static class ViewHolder {
        public TextView text;
        public TextView text2;
        public TextView text3;
        public TextView text4;
        public QuickContactBadge image;
        public View background;
    }

	  public BatchDeleteAllArrayAdapter(Activity context, ArrayList<String> body, ArrayList<String> numbers, ArrayList<String> group) {
          super(context, R.layout.contact_body, body);

          this.context = context;
          this.body = body;
          this.numbers = numbers;
          this.group = group;
          this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

          // shared prefs again!
          customFont = sharedPrefs.getBoolean("custom_font", false);
          customFontPath = sharedPrefs.getString("custom_font_path", null);
          customTheme = sharedPrefs.getBoolean("custom_theme", false);
          ctSummaryTextColor = sharedPrefs.getInt("ct_summaryTextColor", context.getResources().getColor(R.color.black));
          textSize2 = sharedPrefs.getString("text_size2", 14 + "");
          contactPictures2 = sharedPrefs.getBoolean("contact_pictures2", true);
          ctDarkContactPics = sharedPrefs.getBoolean("ct_darkContactImage", false);
          hideMessageCounter = sharedPrefs.getBoolean("hide_message_counter", false);
          ctMessageCounterColor = sharedPrefs.getInt("ct_messageCounterColor", context.getResources().getColor(R.color.messageCounterLight));
          smilies = sharedPrefs.getString("smilies", "with");
          emojiType = sharedPrefs.getBoolean("emoji_type", true);
          smiliesType = sharedPrefs.getBoolean("smiliesType", true);
          hourFormat = sharedPrefs.getBoolean("hour_format", false);
          customBackground =sharedPrefs.getBoolean("custom_background", false);
          ctUnreadConversationColor = sharedPrefs.getInt("ct_unreadConversationColor", sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
          ctConversationListBackground = sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver));
      }
	  
	  @Override
	  public int getCount()
	  {
		return numbers.size();
	  }

    @SuppressLint("SimpleDateFormat")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View contactView = convertView;

        if (contactView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            contactView = inflater.inflate(R.layout.contact_body, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.text = (TextView) contactView.findViewById(R.id.contactName);
            viewHolder.text2 = (TextView) contactView.findViewById(R.id.contactBody);
            viewHolder.text3 = (TextView) contactView.findViewById(R.id.contactDate);
            viewHolder.text4 = (TextView) contactView.findViewById(R.id.contactDate2);
            viewHolder.image = (QuickContactBadge) contactView.findViewById(R.id.quickContactBadge3);
            viewHolder.background = contactView.findViewById(R.id.background);

            if (customFont)
            {
                viewHolder.text.setTypeface(Typeface.createFromFile(customFontPath));
                viewHolder.text2.setTypeface(Typeface.createFromFile(customFontPath));
                viewHolder.text3.setTypeface(Typeface.createFromFile(customFontPath));
                viewHolder.text4.setTypeface(Typeface.createFromFile(customFontPath));
            }

            if (!customTheme)
            {
                String color = sharedPrefs.getString("menu_text_color", "default");

                if (color.equals("blue"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_blue));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_blue));
                } else if (color.equals("white"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.white));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.white));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.white));
                } else if (color.equals("green"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_green));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_green));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_green));
                } else if (color.equals("orange"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_orange));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_orange));
                } else if (color.equals("red"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_red));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_red));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_red));
                } else if (color.equals("purple"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.holo_purple));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.holo_purple));
                } else if (color.equals("black"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.pitch_black));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.pitch_black));
                } else if (color.equals("grey"))
                {
                    viewHolder.text2.setTextColor(context.getResources().getColor(R.color.grey));
                    viewHolder.text3.setTextColor(context.getResources().getColor(R.color.grey));
                    viewHolder.text4.setTextColor(context.getResources().getColor(R.color.grey));
                }  else
                {
                    viewHolder.text2.setTextColor(ctSummaryTextColor);
                    viewHolder.text3.setTextColor(ctSummaryTextColor);
                    viewHolder.text4.setTextColor(ctSummaryTextColor);
                }

                color = sharedPrefs.getString("name_text_color", "default");

                if (color.equals("blue"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
                } else if (color.equals("white"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.white));
                } else if (color.equals("green"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
                } else if (color.equals("orange"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
                } else if (color.equals("red"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
                } else if (color.equals("purple"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
                } else if (color.equals("black"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
                } else if (color.equals("grey"))
                {
                    viewHolder.text.setTextColor(context.getResources().getColor(R.color.grey));
                }  else
                {
                    viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
                }
            } else
            {
                viewHolder.text.setTextColor(sharedPrefs.getInt("ct_nameTextColor", context.getResources().getColor(R.color.black)));
                viewHolder.text2.setTextColor(ctSummaryTextColor);
                viewHolder.text3.setTextColor(ctSummaryTextColor);
                viewHolder.text4.setTextColor(ctSummaryTextColor);
            }

            viewHolder.text.setTextSize((float)Integer.parseInt(textSize2));
            viewHolder.text2.setTextSize((float)Integer.parseInt(textSize2));
            viewHolder.text3.setTextSize((float)(Integer.parseInt(textSize2) - 2));
            viewHolder.text4.setTextSize((float)(Integer.parseInt(textSize2) - 2));

            viewHolder.text3.setText("");
            viewHolder.text4.setText("");

            if (!contactPictures2)
            {
                viewHolder.image.setVisibility(View.GONE);
                RelativeLayout.LayoutParams params1 = (RelativeLayout.LayoutParams) viewHolder.text.getLayoutParams();
                RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) viewHolder.text2.getLayoutParams();
                params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                viewHolder.text.setLayoutParams(params1);
                viewHolder.text2.setLayoutParams(params2);
            }

            contactView.setTag(viewHolder);
        }

        final ViewHolder holder = (ViewHolder) contactView.getTag();

        if (ctDarkContactPics)
        {
            holder.image.setImageResource(R.drawable.ic_contact_dark);
        } else {
            holder.image.setImageResource(R.drawable.ic_contact_picture);
        }

        new Thread(new Runnable() {

            @Override
            public void run() {
                final String number = ContactUtil.findContactNumber(numbers.get(position), context);
                final Bitmap image = Bitmap.createScaledBitmap(getFacebookPhoto(number), MainActivity.contactWidth, MainActivity.contactWidth, true);

                Spanned text;
                String names = "";

                if (!hideMessageCounter)
                {
                    if (group.get(position).equals("yes"))
                    {
                        text = Html.fromHtml("Group MMS");
                        names = ContactUtil.loadGroupContacts(number, context);
                    } else
                    {
                        text = Html.fromHtml(ContactUtil.findContactName(number, context));
                    }
                } else
                {
                    if (group.get(position).equals("yes"))
                    {
                        text = Html.fromHtml("Group MMS");
                        names = ContactUtil.loadGroupContacts(number, context);
                    } else
                    {
                        text = Html.fromHtml(ContactUtil.findContactName(number, context));
                    }
                }

                final Spanned textF = text;
                final String namesF = names;

                context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                    @Override
                    public void run() {
                        holder.image.assignContactFromPhone(number, true);

                        if (contactPictures2)
                        {
                            if (group.get(position).equals("no"))
                            {
                                try
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(image, MainActivity.contactWidth, MainActivity.contactWidth, true));
                                } catch (Exception e)
                                {
                                    if (ctDarkContactPics)
                                    {
                                        holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                    } else
                                    {
                                        holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                    }
                                }
                            } else
                            {
                                if (ctDarkContactPics)
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_dark)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                } else
                                {
                                    holder.image.setImageBitmap(Bitmap.createScaledBitmap(drawableToBitmap(context.getResources().getDrawable(R.drawable.ic_contact_picture)), MainActivity.contactWidth, MainActivity.contactWidth, true));
                                }
                            }
                        } else
                        {
                            holder.text2.setPadding(10, 0, 0, 15);
                        }

                        holder.text.setText(textF);

                        if (group.get(position).equals("yes"))
                        {
                            holder.text2.setText(namesF);
                        }
                    }

                });
            }

        }).start();

        holder.text2.setText(body.get(position));

        if (!itemsToDelete.contains(position)) {
            holder.background.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
        } else {
            holder.background.setBackgroundColor(context.getResources().getColor(R.color.holo_blue));
        }

        contactView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                boolean flag = false;
                int pos = 0;

                for (int i = 0; i < itemsToDelete.size(); i++) {
                    if (itemsToDelete.get(i) == position) {
                        flag = true;
                        pos = i;
                        break;
                    }
                }

                if (!flag) {
                    itemsToDelete.add(position);
                    holder.background.setBackgroundColor(context.getResources().getColor(R.color.holo_blue));
                } else {
                    itemsToDelete.remove(pos);
                    holder.background.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
                }

            }

        });

        return contactView;
    }
	  
	  public InputStream openDisplayPhoto(long contactId) {
		  Uri contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId);
		     Uri photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.CONTENT_DIRECTORY);
		     Cursor cursor = context.getContentResolver().query(photoUri,
		          new String[] {Contacts.Photo.PHOTO}, null, null, null);
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

    public Bitmap getFacebookPhoto(String phoneNumber) {
        try
        {
            Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
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
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (ctDarkContactPics)
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
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
                    Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                    if (ctDarkContactPics)
                    {
                        defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                    }

                    contact.close();
                    return defaultPhoto;
                }
                Bitmap defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);

                if (ctDarkContactPics)
                {
                    defaultPhoto = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                }

                contact.close();
                return defaultPhoto;
            } catch (Exception e)
            {
                if (ctDarkContactPics)
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
                } else
                {
                    contact.close();
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
                }
            }
        } catch (Exception e)
        {
            if (ctDarkContactPics)
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_dark);
            } else
            {
                return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
            }
        }
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

            Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e)
        {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
        }
    }

}
