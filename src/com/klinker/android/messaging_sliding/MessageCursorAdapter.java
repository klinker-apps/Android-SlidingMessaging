package com.klinker.android.messaging_sliding;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Html;
import android.text.Spannable;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.*;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.mms.MmsReceiverService;
import com.klinker.android.send_message.StripAccents;

import java.io.*;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCursorAdapter extends CursorAdapter {

    private final Activity context;
    private final String myId;
    private final String inboxNumbers;
    private final int threadPosition;
    private final long threadIds;
    private final Bitmap contactImage;
    public static Bitmap myImage;
    private SharedPreferences sharedPrefs;
    private ContentResolver contentResolver;
    private Cursor mCursor;
    private Paint paint;
    private Typeface font;
    private final LayoutInflater mInflater;
    private Resources resources;

    private boolean touchwiz = false;
    private final boolean lookForVoice;

    public MessageCursorAdapter(Activity context, String myId, String inboxNumbers, long ids, Cursor query, int threadPosition) {
        super(context, query, 0);
        this.context = context;
        this.myId = myId;
        this.inboxNumbers = inboxNumbers;
        this.threadPosition = threadPosition;
        this.threadIds = ids;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.contentResolver = context.getContentResolver();
        this.mInflater = LayoutInflater.from(context);
        this.mCursor = query;

        this.resources = context.getResources();

        if (MainActivity.settings.runAs == null) {
            MainActivity.settings = AppSettings.init(context);
        }

        Bitmap input;

        if (MainActivity.settings.voiceAccount != null) {
            lookForVoice = true;
        } else {
            lookForVoice = false;
        }

        try {
            input = ContactUtil.getFacebookPhoto(inboxNumbers, context);
        } catch (NumberFormatException e) {
            input = null;
        }

        if (input == null) {
            if (MainActivity.settings.darkContactImage) {
                input = ContactUtil.drawableToBitmap(resources.getDrawable(R.drawable.default_avatar_dark), context);
            } else {
                input = ContactUtil.drawableToBitmap(resources.getDrawable(R.drawable.default_avatar), context);
            }
        }

        contactImage = input;

        if (myImage == null) {
            InputStream input2;

            try {
                input2 = ContactUtil.openDisplayPhoto(Long.parseLong(this.myId), context);
            } catch (NumberFormatException e) {
                input2 = null;
            }

            if (input2 == null) {
                if (MainActivity.settings.darkContactImage) {
                    input2 = resources.openRawResource(R.drawable.default_avatar_dark);
                } else {
                    input2 = resources.openRawResource(R.drawable.default_avatar);
                }
            }

            Bitmap im;

            try {
                im = BitmapFactory.decodeStream(input2);
            } catch (Exception e) {
                if (MainActivity.settings.darkContactImage) {
                    im = ContactUtil.drawableToBitmap(resources.getDrawable(R.drawable.default_avatar_dark), context);
                } else {
                    im = ContactUtil.drawableToBitmap(resources.getDrawable(R.drawable.default_avatar), context);
                }
            }

            myImage = im;
        }

        paint = new Paint();
        float densityMultiplier = resources.getDisplayMetrics().density;
        float scaledPx = Integer.parseInt(MainActivity.settings.textSize) * densityMultiplier;
        paint.setTextSize(scaledPx);
        font = null;

        if (MainActivity.settings.customFont) {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
            paint.setTypeface(font);
        }

        // check if user is using touchwiz by looking for the launcher on their device
        try {
            PackageManager pm = context.getPackageManager();
            pm.getPackageInfo("com.sec.android.smando.launcher", PackageManager.GET_ACTIVITIES);
            touchwiz = true;
        } catch (Exception e) {

        }
    }

    private int getItemViewType(Cursor query) {
        try {
            String s = query.getString(query.getColumnIndex("msg_box"));

            if (s != null) {
                if (query.getInt(query.getColumnIndex("msg_box")) == 4) {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 5) {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 1) {
                    return 0;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 2) {
                    return 1;
                }
            } else {
                String type = query.getString(query.getColumnIndex("type"));

                if (type.equals("2") || type.equals("4") || type.equals("5") || type.equals("6")) {
                    return 1;
                } else {
                    return 0;
                }

            }
        } catch (Exception e) {
            return 0;
        }

        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(getCount() - 1 - position);
        return getItemViewType(cursor);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public void bindView(final View view, Context mContext, final Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();
        holder.media.setVisibility(View.GONE);

        boolean sent = false;
        boolean mms = false;
        String image = null;
        String body = "";
        String date = "0";
        String id = "";
        boolean sending = false;
        boolean error = false;
        boolean group = false;
        String sender = "";
        String status = "-1";
        boolean locked = false;
        boolean voice = false;
        String subject = null;

        String dateType = "date";

        if (MainActivity.settings.showOriginalTimestamp) {
            dateType = "date_sent";
        }

        try {
            String s = cursor.getString(cursor.getColumnIndex("msg_box"));

            if (s != null) {
                id = cursor.getString(cursor.getColumnIndex("_id"));
                mms = true;
                body = "";

                date = Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))) * 1000 + "";
                subject = cursor.getString(cursor.getColumnIndex("sub"));

                String number = getAddressNumber(Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")))).trim();

                String[] numbers = number.split(" ");

                if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 4) {
                    sending = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 5) {
                    error = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 1) {
                    sent = false;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 2) {
                    sent = true;
                }

                if (numbers.length > 2) {
                    group = true;
                    sender = numbers[0];
                }

                if (cursor.getInt(cursor.getColumnIndex("read")) == 0) {
                    final String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ContentValues values = new ContentValues();
                            values.put("read", true);
                            contentResolver.update(Uri.parse("content://mms/inbox"), values, "_id=" + SmsMessageId, null);
                        }
                    }).start();
                }

                if (cursor.getInt(cursor.getColumnIndex("locked")) == 1) {
                    locked = true;
                }

                final String selectionPart = "mid=" + cursor.getString(cursor.getColumnIndex("_id"));

                if (!group) {
                    holder.media.setVisibility(View.VISIBLE);

                    final String idF = id;

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(250);
                            } catch (Exception e) { }

                            String body = "";
                            String image = null;
                            String video = null;
                            String audio = null;

                            Uri uri = Uri.parse("content://mms/part");
                            Cursor query = contentResolver.query(uri, null, selectionPart, null, null);

                            if (query.moveToFirst()) {
                                do {
                                    String partId = query.getString(query.getColumnIndex("_id"));
                                    String type = query.getString(query.getColumnIndex("ct"));
                                    String body2 = "";

                                    if ("text/plain".equals(type)) {
                                        String data = query.getString(query.getColumnIndex("_data"));
                                        if (data != null) {
                                            body2 = getMmsText(partId, context);
                                            body += body2;
                                        } else {
                                            body2 = query.getString(query.getColumnIndex("text"));
                                            body += body2;
                                        }
                                    }

                                    if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                            "image/gif".equals(type) || "image/jpg".equals(type) ||
                                            "image/png".equals(type)) {
                                        if (image == null)
                                        {
                                            image = "content://mms/part/" + partId;
                                        } else
                                        {
                                            image += " content://mms/part/" + partId;
                                        }
                                    }

                                    if ("video/mpeg".equals(type) || "video/3gpp".equals(type) || "video/mp4".equals(type))
                                    {
                                        video = "content://mms/part/" + partId;
                                    }

                                    if (type.startsWith("audio/")) {
                                        audio = "content://mms/part/" + partId;
                                    }
                                } while (query.moveToNext());
                            }

                            query.close();

                            if (image == null && video == null && audio == null && body.equals("")) {
                                context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                    @Override
                                    public void run() {
                                        downloadableMessage(holder, idF);
                                    }
                                });
                            } else {
                                String images[];
                                Bitmap picture;

                                try {
                                    holder.imageUri = Uri.parse(image);
                                    images = image.trim().split(" ");
                                    picture = SendUtil.getThumbnail(context, Uri.parse(images[0]));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    images = null;
                                    picture = null;
                                } catch (Error e) {
                                    try {
                                        holder.imageUri = Uri.parse(image);
                                        images = image.trim().split(" ");
                                        picture = SendUtil.getThumbnail(context, Uri.parse(images[0]));
                                    } catch (Exception f) {
                                        images = null;
                                        picture = null;
                                    }
                                }

                                final String text = body;
                                final String imageUri = image;
                                final String[] imagesF = images;
                                final Bitmap pictureF = picture;
                                final String videoF = video;
                                final String audioF = audio;

                                if (holder.text.getText().toString().equals("")) {
                                    // view is empty and has not been recycled, so show the images
                                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                        @Override
                                        public void run() {
                                            setMessageText(holder.text, text, context);

                                            if (imageUri == null && videoF == null && audioF == null) {
                                                holder.media.setVisibility(View.GONE);
                                                holder.media.setImageResource(android.R.color.transparent);
                                            } else if (imageUri != null) {
                                                holder.media.setVisibility(View.VISIBLE);

                                                try {
                                                    if (pictureF == null) {
                                                        holder.media.setImageURI(Uri.parse(imagesF[0]));
                                                    } else {
                                                        holder.media.setImageBitmap(pictureF);
                                                    }
                                                } catch (Throwable e) {
                                                    holder.media.setVisibility(View.GONE);
                                                }

                                                holder.media.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        if (imagesF.length == 1) {
                                                            Intent intent = new Intent();
                                                            intent.setAction(Intent.ACTION_VIEW);
                                                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                            intent.putExtra("SingleItemOnly", true);
                                                            intent.setDataAndType(Uri.parse(imageUri), "image/*");
                                                            context.startActivity(intent);
                                                        } else {
                                                            Intent intent = new Intent();
                                                            intent.setClass(context, ImageViewer.class);
                                                            Bundle b = new Bundle();
                                                            b.putString("image", imageUri);
                                                            intent.putExtra("bundle", b);
                                                            context.startActivity(intent);
                                                        }
                                                    }
                                                });

                                                try {
                                                    if (imagesF.length > 1) {
                                                        holder.date.setText(holder.date.getText().toString() + " - Multiple Attachments");
                                                    }
                                                } catch (NullPointerException e) {
                                                    e.printStackTrace();
                                                }
                                            } else if (videoF != null) {
                                                holder.media.setVisibility(View.VISIBLE);
                                                holder.media.setImageResource(R.drawable.ic_video_play);
                                                holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                                holder.media.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(Intent.ACTION_VIEW);
                                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                                        intent.setDataAndType(Uri.parse(videoF), "video/*");
                                                        context.startActivity(intent);
                                                    }
                                                });
                                            } else if (audioF != null) {
                                                holder.media.setVisibility(View.VISIBLE);
                                                holder.media.setImageResource(R.drawable.ic_video_play);
                                                holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                                holder.media.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        Intent intent = new Intent();
                                                        intent.setAction(Intent.ACTION_VIEW);
                                                        intent.setDataAndType(Uri.parse(audioF), "audio/*");
                                                        context.startActivity(intent);
                                                    }
                                                });
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }).start();
                } else {
                    body = "";
                    image = null;
                    String video = null;
                    String audio = null;

                    Uri uri = Uri.parse("content://mms/part");
                    Cursor query = contentResolver.query(uri, null, selectionPart, null, null);

                    if (query.moveToFirst()) {
                        do {
                            String partId = query.getString(query.getColumnIndex("_id"));
                            String type = query.getString(query.getColumnIndex("ct"));
                            String body2 = "";

                            if ("text/plain".equals(type)) {
                                String data = query.getString(query.getColumnIndex("_data"));
                                if (data != null) {
                                    body2 = getMmsText(partId, context);
                                    body += body2;
                                } else {
                                    body2 = query.getString(query.getColumnIndex("text"));
                                    body += body2;
                                }
                            }

                            if ("image/jpeg".equals(type) || "image/bmp".equals(type) ||
                                    "image/gif".equals(type) || "image/jpg".equals(type) ||
                                    "image/png".equals(type)) {
                                if (image == null)
                                {
                                    image = "content://mms/part/" + partId;
                                } else
                                {
                                    image += " content://mms/part/" + partId;
                                }
                            }

                            if ("video/mpeg".equals(type) || "video/3gpp".equals(type) || "video/mp4".equals(type))
                            {
                                video = "content://mms/part/" + partId;
                            }

                            if (type.startsWith("audio/")) {
                                audio = "content://mms/part/" + partId;
                            }
                        } while (query.moveToNext());
                    }

                    query.close();

                    if (image == null && video == null && audio == null && body.equals("")) {
                        downloadableMessage(holder, id);
                    } else {
                        String images[];

                        try {
                            holder.imageUri = Uri.parse(image);
                            images = image.trim().split(" ");
                        } catch (Exception e) {
                            images = null;
                        }

                        final String text = body;
                        final String imageUri = images[0];
                        final String[] imagesF = images;
                        final String videoF = video;
                        final String audioF = audio;

                        setMessageText(holder.text, text, context);

                        if (imageUri == null && videoF == null && audioF == null) {
                            holder.media.setVisibility(View.GONE);
                            holder.media.setImageResource(android.R.color.transparent);
                        } else if (imageUri != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageURI(Uri.parse(imageUri));
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    if (imagesF.length == 1) {
                                        Intent intent = new Intent();
                                        intent.setAction(Intent.ACTION_VIEW);
                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                        intent.putExtra("SingleItemOnly", true);
                                        intent.setDataAndType(Uri.parse(imageUri), "image/*");
                                        context.startActivity(intent);
                                    } else {
                                        Intent intent = new Intent();
                                        intent.setClass(context, ImageViewer.class);
                                        Bundle b = new Bundle();
                                        b.putString("image", imageUri);
                                        intent.putExtra("bundle", b);
                                        context.startActivity(intent);
                                    }
                                }
                            });

                            if (imagesF.length > 1) {
                                holder.date.setText(holder.date.getText().toString() + " - Multiple Attachments");
                            }
                        } else if (videoF != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageResource(R.drawable.ic_video_play);
                            holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    intent.setDataAndType(Uri.parse(videoF), "video/*");
                                    context.startActivity(intent);
                                }
                            });
                        } else if (audioF != null) {
                            holder.media.setVisibility(View.VISIBLE);
                            holder.media.setImageResource(R.drawable.ic_video_play);
                            holder.media.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                            holder.media.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(audioF), "audio/*");
                                    context.startActivity(intent);
                                }
                            });
                        }
                    }
                }

            } else {
                String type = cursor.getString(cursor.getColumnIndex("type"));

                if (cursor.getInt(cursor.getColumnIndex("locked")) == 1) {
                    locked = true;
                }

                if (type.equals("1"))
                {
                    sent = false;

                    try
                    {
                        body = cursor.getString(cursor.getColumnIndex("body"));
                    } catch (Exception e)
                    {
                        body = "";
                    }

                    date = cursor.getString(cursor.getColumnIndex(dateType));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;

                    if (lookForVoice) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                         if (status.equals("2")) {
                            voice = true;
                        }
                    }

                    if (cursor.getInt(cursor.getColumnIndex("read")) == 0)
                    {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        contentResolver.update(Uri.parse("content://sms/inbox"), values, "_id=" + SmsMessageId, null);
                    }
                } else if (type.equals("2"))
                {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;

                    if (MainActivity.settings.deliveryReports || lookForVoice) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                        if (status.equals("64") || status.equals("128"))
                        {
                            error = true;
                        } else if (status.equals("2")) {
                            voice = true;
                        }
                    }

                    if (cursor.getInt(cursor.getColumnIndex("read")) == 0)
                    {
                        String SmsMessageId = cursor.getString(cursor.getColumnIndex("_id"));
                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        contentResolver.update(Uri.parse("content://mms/inbox"), values, "_id=" + SmsMessageId, null);
                    }
                } else if (type.equals("5"))
                {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;
                    error = true;

                    if (lookForVoice) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                        if (status.equals("2")) {
                            voice = true;
                        }
                    }
                } else if (type.equals("4") || type.equals("6"))
                {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;
                    sending = true;

                    if (lookForVoice) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                        if (status.equals("2")) {
                            voice = true;
                        }
                    }
                } else
                {
                    sent = false;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex(dateType));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;

                    if (lookForVoice) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                        if (status.equals("2")) {
                            voice = true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (group && !sent) {
            final String sentFrom = sender;
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    final Bitmap picture = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(sentFrom, context), MainActivity.contactWidth, MainActivity.contactWidth, true);

                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            holder.image.setImageBitmap(picture);
                            holder.image.assignContactFromPhone(sentFrom, true);
                        }
                    });
                }

            }).start();
        }

        Date date2;

        try {
            date2 = new Date(Long.parseLong(date));
        } catch (Exception e) {
            date2 = new Date(0);
        }

        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date(cal.getTimeInMillis());

        if (getZeroTimeDate(date2).equals(getZeroTimeDate(currentDate))) {
            if (MainActivity.settings.hourFormat) {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + (
                        subject != null ? " - " + subject : ""
                        ));
            } else {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + (
                        subject != null ? " - " + subject : ""
                ));
            }
        } else {
            if (MainActivity.settings.hourFormat) {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2) + (
                        subject != null ? " - " + subject : ""
                ));
            } else {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2) + (
                        subject != null ? " - " + subject : ""
                ));
            }
        }

        if (sending) {
            holder.date.setVisibility(View.GONE);

            try {
                holder.ellipsis.setVisibility(View.VISIBLE);
                holder.ellipsis.setBackgroundResource(R.drawable.ellipsis);
                holder.ellipsis.setColorFilter(MainActivity.settings.ctSentTextColor);
                AnimationDrawable ellipsis = (AnimationDrawable) holder.ellipsis.getBackground();
                ellipsis.start();
            } catch (Exception e) {

            }
        } else {
            holder.date.setVisibility(View.VISIBLE);

            try {
                holder.ellipsis.setVisibility(View.GONE);
            } catch (Exception e) { }

            if (sent && MainActivity.settings.deliveryReports && !error && status.equals("0")) {
                String text = "<html><body><img src=\"ic_sent.png\"/> " + holder.date.getText().toString() + "</body></html>";
                holder.date.setText(Html.fromHtml(text, imgGetterSent, null));
            } else if (error) {
                String text = "<html><body><img src=\"ic_error.png\"/> ERROR</body></html>";
                holder.date.setText(Html.fromHtml(text, imgGetterFail, null));
            }

            if (voice && !error) {
                String text = "<html><body><img src=\"voice_enabled.png\"/> " + holder.date.getText().toString() + "</body></html>";
                holder.date.setText(Html.fromHtml(text, imgGetterVoice, null));
            }
        }

        if (locked) {
            String text = "<html><body><img src=\"" + (sent ? "sent" : "received") + "\"/> " + holder.date.getText().toString() + "</body></html>";
            holder.date.setText(Html.fromHtml(text, imgGetterLocked, null));
        }

        if (group && !sent) {
            final String senderF = sender;

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(senderF.replaceAll("-", "")));
                    final Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            if(phonesCursor != null && phonesCursor.moveToFirst()) {
                                holder.date.setText(holder.date.getText() + " - " + phonesCursor.getString(0));
                            } else
                            {
                                holder.date.setText(holder.date.getText() + " - " + senderF);
                            }

                            phonesCursor.close();
                        }

                    });
                }
            }).start();
        }

        setMessageText(holder.text, body, context);

        if (cursor.getPosition() == 0) {
            if (MainActivity.settings.runAs.equals("hangout")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, resources.getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, resources.getDisplayMetrics());
                int scale4 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, resources.getDisplayMetrics());
                
                if (sent) {
                    view.setPadding(scale4, scale2, scale, scale3);
                } else {
                    view.setPadding(scale, scale2, scale4, scale3);
                }
            } else if (MainActivity.settings.runAs.equals("card2") || MainActivity.settings.runAs.equals("card+")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, resources.getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, resources.getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
                view.setPadding(scale, scale2, scale, scale3);
            } else {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
                view.setPadding(0, 0, 0, scale);
            }
        } else {
            if (MainActivity.settings.runAs.equals("hangout")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, resources.getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
                int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, resources.getDisplayMetrics());
            
                if (sent) {
                    view.setPadding(scale3, scale2, scale, scale2);
                } else {
                    view.setPadding(scale, scale2, scale3, scale2);
                }
            } else if (MainActivity.settings.runAs.equals("card2") || MainActivity.settings.runAs.equals("card+")) {
                int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, resources.getDisplayMetrics());
                int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, resources.getDisplayMetrics());
                view.setPadding(scale, scale2, scale, 0);
            } else {
                view.setPadding(0, 0, 0, 0);
            }
        }

        final String dateT = date;
        int size2 = 0;

        try {
            size2 = image.split(" ").length;
        } catch (Exception e) { }

        final View rowViewF = view;

        if (mms) {
            holder.media.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    rowViewF.performLongClick();
                    return true;
                }
            });
        }
        
        holder.text.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                rowViewF.performLongClick();
                return true;
            }
        });

        final String idF = id;
        final boolean mmsF = mms;
        final boolean sentF = sent;
        final boolean lockedF = locked;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mmsF) {
                    Cursor query;
                    String dialogText = "";

                    try {
                        if (!sentF) {
                            query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "date_sent", "type", "address"}, null, null, "date desc limit 1");

                            if (query.moveToFirst()) {
                                String dateSent = query.getString(query.getColumnIndex("date_sent")), dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date1 = new Date(Long.parseLong(dateSent)), date2 = new Date(Long.parseLong(dateReceived));

                                if (MainActivity.settings.hourFormat) {
                                    dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else {
                                    dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = resources.getString(R.string.type) + " Text Message\n" +
                                        resources.getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        resources.getString(R.string.sent) + " " + dateSent + "\n" +
                                        resources.getString(R.string.received) + " " + dateReceived;
                            }
                        } else {
                            query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                            if (query.moveToFirst()) {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (MainActivity.settings.hourFormat) {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = resources.getString(R.string.type) + " Text Message\n" +
                                        resources.getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        resources.getString(R.string.sent) + " " + dateReceived;

                                String status = query.getString(query.getColumnIndex("status"));

                                if (!status.equals("-1")) {
                                    if (status.equals("64") || status.equals("128")) {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Error";
                                    } else if (status.equals("2")) {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Voice";
                                    } else {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Delivered";
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                        if (query.moveToFirst()) {
                            if (sentF) {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (MainActivity.settings.hourFormat) {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = resources.getString(R.string.type) + " Text Message\n" +
                                        resources.getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        resources.getString(R.string.sent) + " " + dateReceived;

                                String status = query.getString(query.getColumnIndex("status"));

                                if (!status.equals("-1")) {
                                    if (status.equals("64") || status.equals("128")) {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Error";
                                    } else if (status.equals("2")) {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Voice";
                                    } else {
                                        dialogText += "\n" + resources.getString(R.string.status) + " Delivered";
                                    }
                                }
                            } else {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (MainActivity.settings.hourFormat) {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = resources.getString(R.string.type) + " Text Message\n" +
                                        resources.getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        resources.getString(R.string.received) + " " + dateReceived;
                            }
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(resources.getString(R.string.message_details));
                    builder.setMessage(dialogText);
                    builder.create().show();
                } else {
                    Uri uri = Uri.parse("content://mms/" + idF);
                    String[] projection = new String[] {"_id", "date"};

                    Cursor query = context.getContentResolver().query(uri, projection, null, null, null);

                    if (query.moveToFirst()) {

                        String address = getAddressNumber(Integer.parseInt(query.getString(query.getColumnIndex("_id"))));
                        String[] addresses = address.split(" ");

                        if (sentF) {
                            addresses[0] = "";
                        } else {
                            for (int i = 0; i < addresses.length; i++) {
                                if (addresses[i].equals(MainActivity.myPhoneNumber) || addresses[i].startsWith(MainActivity.myPhoneNumber) || addresses[i].endsWith(MainActivity.myPhoneNumber)) {
                                    addresses[i] = "";
                                }
                            }
                        }

                        address = "";
                        for (String a : addresses) {
                            if (!a.equals("")) {
                                address += a + ", ";
                            }
                        }

                        if (address.length() > 0) {
                            address = address.substring(0, address.length() - 2);
                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(context.getString(R.string.message_details));

                        String message;

                        String dateReceived;
                        Date date2 = new Date(Long.parseLong(dateT));

                        if (MainActivity.settings.hourFormat) {
                            dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                        } else {
                            dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                        }

                        // TODO add on more information to this
                        if (sentF) {
                            message = resources.getString(R.string.type) + " Multimedia Message\n" +
                                    resources.getString(R.string.to) + " " + address + "\n" +
                                    resources.getString(R.string.received) + " " + dateReceived;
                        } else {
                            message = resources.getString(R.string.type) + " Multimedia Message\n" +
                                    resources.getString(R.string.from) + " " + address + "\n" +
                                    resources.getString(R.string.received) + " " + dateReceived;
                        }

                        builder.setMessage(message);
                        builder.create().show();
                    }
                }
            }
        });

        final int sizeT = size2;
        final boolean errorT = error;
        final boolean voiceF = voice;

        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View arg0) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(25);

                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);

                if (!errorT)
                {
                    if (!mmsF || sizeT > 1)
                    {
                        builder2.setItems(lockedF ? R.array.messageOptionsUnlock : R.array.messageOptions, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which)
                                {
                                    case 0:
                                        TextView tv = (TextView) arg0.findViewById(R.id.textBody);
                                        ClipboardManager clipboard = (ClipboardManager)
                                                context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        MainActivity.mDrawerLayout.openDrawer(Gravity.RIGHT);
                                        TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);
                                        MainActivity.messageEntry2.setText(tv2.getText().toString());

                                        break;
                                    case 2:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage(resources.getString(R.string.delete_message));
                                        builder.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            public void onClick(DialogInterface dialog, int id) {
                                                deleteSMS(context, threadIds, idF);

                                                try {
                                                    ((MainActivity) context).refreshViewPager();
                                                } catch (Exception e) {

                                                }
                                            }

                                            public void deleteSMS(Context context, long threadId, String messageId) {
                                                try {
                                                    context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                                } catch (Exception e) {
                                                }
                                            }});
                                        builder.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog2 = builder.create();

                                        dialog2.show();
                                        break;
                                    case 3:
                                        ContentValues values = new ContentValues();
                                        values.put("locked", !lockedF);
                                        contentResolver.update(Uri.parse("content://sms/inbox"), values, "_id=" + idF, null);
                                        ((MainActivity) context).refreshViewPager();
                                        break;
                                    default:
                                        break;
                                }

                            }

                        });

                        AlertDialog dialog = builder2.create();
                        dialog.show();
                    } else
                    {
                        builder2.setItems(lockedF ? R.array.messageOptions2Unlock : R.array.messageOptions2, new DialogInterface.OnClickListener() {

                            @SuppressWarnings("deprecation")
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which)
                                {
                                    case 0:
                                        TextView tv = (TextView) arg0.findViewById(R.id.textBody);
                                        ClipboardManager clipboard = (ClipboardManager)
                                                context.getSystemService(Context.CLIPBOARD_SERVICE);
                                        ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
                                        clipboard.setPrimaryClip(clip);

                                        Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        try {
                                            IOUtil.saveImage(((BitmapDrawable) holder.media.getDrawable()).getBitmap(), dateT, context);
                                        } catch (Exception e)
                                        {
                                            Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 2:
                                        MainActivity.mDrawerLayout.openDrawer(Gravity.RIGHT);

                                        View newMessageView = MainActivity.newMessageView;
                                        TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);

                                        try
                                        {
                                            ((MainActivity)context).attachedImage2 = holder.imageUri;

                                            ((MainActivity)context).imageAttachBackground2.setBackgroundColor(MainActivity.settings.ctConversationListBackground);
                                            Drawable attachBack = resources.getDrawable(R.drawable.attachment_editor_bg);
                                            attachBack.setColorFilter(MainActivity.settings.ctSentMessageBackground, PorterDuff.Mode.MULTIPLY);
                                            ((MainActivity)context).imageAttach2.setBackgroundDrawable(attachBack);
                                            ((MainActivity)context).imageAttachBackground2.setVisibility(View.VISIBLE);
                                            ((MainActivity)context).imageAttach2.setVisibility(true);
                                        } catch (Exception e)
                                        {

                                        }

                                        try
                                        {
                                            ((MainActivity)context).imageAttach2.setImage("send_image", IOUtil.decodeFile(new File(IOUtil.getPath(holder.imageUri, context))));
                                        } catch (Exception e)
                                        {
                                            ((MainActivity)context).imageAttach2.setVisibility(false);
                                            ((MainActivity)context).imageAttachBackground2.setVisibility(View.GONE);
                                        }

                                        Button viewImage = (Button) newMessageView.findViewById(R.id.view_image_button2);
                                        Button replaceImage = (Button) newMessageView.findViewById(R.id.replace_image_button2);
                                        Button removeImage = (Button) newMessageView.findViewById(R.id.remove_image_button2);

                                        viewImage.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View arg0) {
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, holder.imageUri));

                                            }

                                        });

                                        replaceImage.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setType("image/*");
                                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                                context.startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_picture)), 2);

                                            }

                                        });

                                        removeImage.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                ((MainActivity)context).imageAttach2.setVisibility(false);
                                                ((MainActivity)context).imageAttachBackground2.setVisibility(View.GONE);

                                            }

                                        });

                                        MainActivity.messageEntry2.setText(tv2.getText().toString());

                                        break;
                                    case 3:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage(resources.getString(R.string.delete_message));
                                        builder.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            public void onClick(DialogInterface dialog, int id) {
                                                deleteSMS(context, threadIds, idF);
                                                ((MainActivity) context).refreshViewPager();
                                            }

                                            public void deleteSMS(Context context, long threadId, String messageId) {
                                                try {
                                                    context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                                } catch (Exception e) {
                                                }
                                            }});
                                        builder.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog2 = builder.create();

                                        dialog2.show();
                                        break;
                                    case 4:
                                        ContentValues values = new ContentValues();
                                        values.put("locked", lockedF ? false : true);
                                        contentResolver.update(Uri.parse("content://" + (mmsF ? "mms" : "sms") + "/inbox"), values, "_id=" + idF, null);
                                        ((MainActivity) context).refreshViewPager();
                                        break;
                                    default:
                                        break;
                                }

                            }

                        });

                        AlertDialog dialog = builder2.create();
                        dialog.show();
                    }
                } else
                {
                    builder2.setItems(lockedF ? R.array.messageOptions3Unlock : R.array.messageOptions3, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                    MainActivity.animationOn = true;

                                    String body2 = ((TextView) arg0.findViewById(R.id.textBody)).getText().toString();

                                    final String body = body2;

                                    new Thread(new Runnable() {

                                        @Override
                                        public void run() {
                                            boolean currentVoiceState = sharedPrefs.getBoolean("voice_enabled", false);
                                            Log.v("voice_state", currentVoiceState + " " + voiceF);

                                            if (voiceF != currentVoiceState) {
                                                sharedPrefs.edit().putBoolean("voice_enabled", voiceF).commit();
                                            }

                                            if (mmsF && holder.imageUri != null) {
                                                int size = holder.imageUri.toString().trim().split(" ").length;
                                                Bitmap[] bitmaps = new Bitmap[size];

                                                for (int i = 0; i < size; i++) {
                                                    try {
                                                        bitmaps[i] = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(holder.imageUri.toString().trim().split(" ")[i]));
                                                    } catch (Exception e) {

                                                    }
                                                }

                                                SendUtil.sendMessage(context, inboxNumbers.trim().split(" "), body, bitmaps);
                                            } else {
                                                SendUtil.sendMessage(context, inboxNumbers, body);
                                            }

                                            sharedPrefs.edit().putBoolean("voice_enabled", currentVoiceState).commit();

                                            Cursor deleter = context.getContentResolver().query(Uri.parse("content://sms/failed"), new String[] {"_id"}, null, null, null);

                                            if (deleter.moveToFirst()) {
                                                String id = deleter.getString(deleter.getColumnIndex("_id"));
                                                context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds + "/"), "_id=" + id, null);
                                            }

                                            context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    MainActivity.sentMessage = true;
                                                    ((MainActivity) context).refreshViewPager4(inboxNumbers, StripAccents.stripAccents(body), Calendar.getInstance().getTimeInMillis() + "");
                                                }

                                            });
                                        }

                                    }).start();

                                    break;
                                case 1:
                                    TextView tv = (TextView) arg0.findViewById(R.id.textBody);
                                    ClipboardManager clipboard = (ClipboardManager)
                                            context.getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData clip = ClipData.newPlainText("Copied Message", tv.getText().toString());
                                    clipboard.setPrimaryClip(clip);

                                    Toast.makeText(context, R.string.text_saved, Toast.LENGTH_SHORT).show();
                                    break;
                                case 2:
                                    MainActivity.mDrawerLayout.openDrawer(Gravity.RIGHT);
                                    TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);
                                    MainActivity.messageEntry2.setText(tv2.getText().toString());

                                    break;
                                case 3:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage(resources.getString(R.string.delete_message));
                                    builder.setPositiveButton(resources.getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @SuppressLint("SimpleDateFormat")
                                        public void onClick(DialogInterface dialog, int id) {
                                            deleteSMS(context, threadIds, idF);
                                            ((MainActivity) context).refreshViewPager();
                                        }

                                        public void deleteSMS(Context context, long threadId, String messageId) {
                                            try {
                                                context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                            } catch (Exception e) {
                                            }
                                        }});
                                    builder.setNegativeButton(resources.getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                                    AlertDialog dialog2 = builder.create();

                                    dialog2.show();
                                    break;
                                case 4:
                                    ContentValues values = new ContentValues();
                                    values.put("locked", lockedF ? false : true);
                                    contentResolver.update(Uri.parse("content://" + (mmsF ? "mms" : "sms") + "/inbox"), values, "_id=" + idF, null);
                                    ((MainActivity) context).refreshViewPager();
                                    break;
                                default:
                                    break;
                            }

                        }

                    });

                    AlertDialog dialog = builder2.create();
                    dialog.show();
                }

                return true;
            }

        });

        if (MainActivity.animationOn == true && cursor.getPosition() == 0 && threadPosition == 0)
        {
            if (MainActivity.settings.sendingAnimation.equals("left"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.sendingAnimation.equals("right"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.sendingAnimation.equals("up"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_up);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.sendingAnimation.equals("hangouts")) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.hangouts_in);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            }

            MainActivity.animationOn = false;
        }

        if (MainActivity.animationReceived == 1 && cursor.getPosition() == 0 && MainActivity.animationThread == threadPosition)
        {
            if (MainActivity.settings.receiveAnimation.equals("left"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_right);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.receiveAnimation.equals("right"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_in_left);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.receiveAnimation.equals("up"))
            {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.slide_up);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            } else if (MainActivity.settings.receiveAnimation.equals("hangouts")) {
                Animation anim = AnimationUtils.loadAnimation(context, R.anim.hangouts_in);
                anim.setDuration(MainActivity.settings.animationSpeed);
                view.startAnimation(anim);
            }

            MainActivity.animationReceived = 0;
        }

        if (cursor.getPosition() == 0 && voice) {
            if (!((MainActivity) context).threadsThroughVoice.contains(threadIds)) {
                ((MainActivity) context).threadsThroughVoice.add(threadIds);

                if (((MainActivity) context).firstRun) {
                    ((MainActivity) context).voiceButton.performClick();
                }
            }
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View v;

        int type = getItemViewType(cursor);

        if (type == 1) {
            if (MainActivity.settings.runAs.equals("hangout")) {
                v = mInflater.inflate(R.layout.message_hangout_sent, parent, false);
            } else if (MainActivity.settings.runAs.equals("sliding")) {
                v = mInflater.inflate(R.layout.message_classic_sent, parent, false);
            } else {
                v = mInflater.inflate(R.layout.message_card2_sent, parent, false);
            }

            holder.text = (TextView) v.findViewById(R.id.textBody);
            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            try { holder.ellipsis = (ImageView) v.findViewById(R.id.ellipsis); } catch (Exception e) {  }
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = v.findViewById(R.id.messageBody);

            if (!touchwiz) {
                holder.image.assignContactUri(ContactsContract.Profile.CONTENT_URI);
            }
        } else {
            if (MainActivity.settings.runAs.equals("hangout")) {
                v = mInflater.inflate(R.layout.message_hangout_received, parent, false);
            } else if (MainActivity.settings.runAs.equals("sliding")) {
                v = mInflater.inflate(R.layout.message_classic_received, parent, false);
            } else {
                v = mInflater.inflate(R.layout.message_card2_received, parent, false);
            }

            holder.text = (TextView) v.findViewById(R.id.textBody);
            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            holder.downloadButton = (Button) v.findViewById(R.id.downloadButton);
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = v.findViewById(R.id.messageBody);

            holder.image.assignContactFromPhone(inboxNumbers, true);
        }

        if (MainActivity.settings.runAs.equals("card2") || MainActivity.settings.runAs.equals("card+")) {

            if (MainActivity.settings.themeName.equals("Light Theme") || MainActivity.settings.themeName.equals("Hangouts Theme") || MainActivity.settings.themeName.equals("Light Theme 2.0") || MainActivity.settings.themeName.equals("Light Green Theme") || MainActivity.settings.themeName.equals("Burnt Orange Theme")) {

            } else {
                v.findViewById(R.id.shadow).setVisibility(View.GONE);
            }

            if (type == 1) {
                v.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(MainActivity.settings.ctSentTextColor, "44")));
            } else {
                v.findViewById(R.id.divider).setBackgroundColor(convertToColorInt(convertToARGB(MainActivity.settings.ctRecievedTextColor, "44")));
            }

            if(MainActivity.settings.runAs.equals("card+"))
            {
                v.findViewById(R.id.divider).setVisibility(View.GONE);
                v.findViewById(R.id.shadow).setVisibility(View.GONE);
            }
        }

        if (MainActivity.settings.customFont)
        {
            holder.text.setTypeface(font);
            holder.date.setTypeface(font);
        }

        if (MainActivity.settings.contactPictures)
        {
            if (type == 0)
            {
                holder.image.setImageBitmap(contactImage);
            } else
            {
                holder.image.setImageBitmap(myImage);
            }
        } else
        {
            holder.image.setMaxWidth(0);
            holder.image.setMinimumWidth(0);
        }

        try {
            holder.text.setTextSize(Integer.parseInt(MainActivity.settings.textSize.substring(0,2)));
            holder.date.setTextSize(Integer.parseInt(MainActivity.settings.textSize.substring(0,2)) - 4);
        } catch (Exception e) {
            holder.text.setTextSize(Integer.parseInt(MainActivity.settings.textSize.substring(0,1)));
            holder.date.setTextSize(Integer.parseInt(MainActivity.settings.textSize.substring(0,1)) - 4);
        }

        if (MainActivity.settings.tinyDate)
        {
            holder.date.setTextSize(10);
        }

        holder.text.setText("");
        holder.text.setLinkTextColor(MainActivity.settings.linkColor);
        holder.date.setText("");

        if (type == 0) {
            holder.downloadButton.setVisibility(View.GONE);
        }

        if (type == 1) {
            holder.text.setTextColor(MainActivity.settings.ctSentTextColor);
            holder.date.setTextColor(convertToColorInt(convertToARGB(MainActivity.settings.ctSentTextColor, "55")));
            holder.background.setBackgroundColor(MainActivity.settings.ctSentMessageBackground);
            holder.media.setBackgroundColor(MainActivity.settings.ctSentMessageBackground);
            holder.bubble.setColorFilter(MainActivity.settings.ctSentMessageBackground);
            try { holder.ellipsis.setColorFilter(MainActivity.settings.ctSentTextColor); } catch (Exception e) { }

            if (!MainActivity.settings.customTheme)
            {
                if (MainActivity.settings.sentTextColor.equals("blue"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_blue));
                    holder.date.setTextColor(resources.getColor(R.color.holo_blue));
                } else if (MainActivity.settings.sentTextColor.equals("white"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.white));
                    holder.date.setTextColor(resources.getColor(R.color.white));
                } else if (MainActivity.settings.sentTextColor.equals("green"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_green));
                    holder.date.setTextColor(resources.getColor(R.color.holo_green));
                } else if (MainActivity.settings.sentTextColor.equals("orange"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_orange));
                    holder.date.setTextColor(resources.getColor(R.color.holo_orange));
                } else if (MainActivity.settings.sentTextColor.equals("red"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_red));
                    holder.date.setTextColor(resources.getColor(R.color.holo_red));
                } else if (MainActivity.settings.sentTextColor.equals("purple"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_purple));
                    holder.date.setTextColor(resources.getColor(R.color.holo_purple));
                } else if (MainActivity.settings.sentTextColor.equals("black"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.pitch_black));
                    holder.date.setTextColor(resources.getColor(R.color.pitch_black));
                } else if (MainActivity.settings.sentTextColor.equals("grey"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.grey));
                    holder.date.setTextColor(resources.getColor(R.color.grey));
                }

                holder.background.setBackgroundColor(convertToColorInt(convertToARGB(MainActivity.settings.ctSentMessageBackground, MainActivity.settings.textOpacity + "")));
            }
        } else {
            holder.text.setTextColor(MainActivity.settings.ctRecievedTextColor);
            holder.date.setTextColor(convertToColorInt(convertToARGB(MainActivity.settings.ctRecievedTextColor, "55")));
            holder.background.setBackgroundColor(MainActivity.settings.ctRecievedMessageBackground);
            holder.media.setBackgroundColor(MainActivity.settings.ctRecievedMessageBackground);
            holder.bubble.setColorFilter(MainActivity.settings.ctRecievedMessageBackground);

            if (!MainActivity.settings.customTheme)
            {
                if (MainActivity.settings.receivedTextColor.equals("blue"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_blue));
                    holder.date.setTextColor(resources.getColor(R.color.holo_blue));
                } else if (MainActivity.settings.receivedTextColor.equals("white"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.white));
                    holder.date.setTextColor(resources.getColor(R.color.white));
                } else if (MainActivity.settings.receivedTextColor.equals("green"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_green));
                    holder.date.setTextColor(resources.getColor(R.color.holo_green));
                } else if (MainActivity.settings.receivedTextColor.equals("orange"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_orange));
                    holder.date.setTextColor(resources.getColor(R.color.holo_orange));
                } else if (MainActivity.settings.receivedTextColor.equals("red"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_red));
                    holder.date.setTextColor(resources.getColor(R.color.holo_red));
                } else if (MainActivity.settings.receivedTextColor.equals("purple"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.holo_purple));
                    holder.date.setTextColor(resources.getColor(R.color.holo_purple));
                } else if (MainActivity.settings.receivedTextColor.equals("black"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.pitch_black));
                    holder.date.setTextColor(resources.getColor(R.color.pitch_black));
                } else if (MainActivity.settings.receivedTextColor.equals("grey"))
                {
                    holder.text.setTextColor(resources.getColor(R.color.grey));
                    holder.date.setTextColor(resources.getColor(R.color.grey));
                }

                holder.background.setBackgroundColor(convertToColorInt(convertToARGB(MainActivity.settings.ctRecievedMessageBackground, MainActivity.settings.textOpacity + "")));
            }
        }

        if (!MainActivity.settings.textAlignment.equals("split"))
        {
            if (MainActivity.settings.textAlignment.equals("right"))
            {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            } else
            {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            }
        } else if (!MainActivity.settings.contactPictures) {
            if (type == 0) {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            } else {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            }
        }

        if (MainActivity.settings.runAs.equals("hangout")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 7, resources.getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, resources.getDisplayMetrics());
            int scale3 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 21, resources.getDisplayMetrics());
            
            if (type == 1) {
                v.setPadding(scale3, scale2, scale, scale2);
            } else {
                v.setPadding(scale, scale2, scale3, scale2);
            }
        } else if (MainActivity.settings.runAs.equals("card2") || MainActivity.settings.runAs.equals("card+")) {
            int scale = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 17, resources.getDisplayMetrics());
            int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, resources.getDisplayMetrics());
            v.setPadding(scale, scale2, scale, 0);
        }

        v.setTag(holder);
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (!mCursor.moveToPosition(getCount() - 1 - position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(context, mCursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, mCursor);
        return v;
    }

    static class ViewHolder {
        public TextView text;
        public TextView date;
        public QuickContactBadge image;
        public View background;
        public ImageView media;
        public ImageView ellipsis;
        public Button downloadButton;
        public ImageView bubble;
        public Uri imageUri;
    }

    public static final String patternStr = "\u00a9|\u00ae|[\u203c-\u3299]|[\uD83C\uDC04-\uD83C\uDFf0]|[\uD83D\uDC00-\uD83D\uDEc5]";
    public static Pattern pattern = Pattern.compile(patternStr);

    public static void setMessageText(final TextView textView, final String body, final Activity context) {
        if (textView.getVisibility() == View.GONE) {
            return;
        }

        if (MainActivity.settings.smilies.equals("with"))
        {
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                textView.setText(body);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (MainActivity.settings.emojiType)
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body));
                            }
                        } else
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, body));
                            }
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(text);
                                Linkify.addLinks(textView, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                if (MainActivity.settings.smiliesType) {
                    textView.setText(EmoticonConverter2New.getSmiledText(context, body));
                } else {
                    textView.setText(EmoticonConverter2.getSmiledText(context, body));
                }

                Linkify.addLinks(textView, Linkify.ALL);
            }
        } else if (MainActivity.settings.smilies.equals("without"))
        {
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                textView.setText(body);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (MainActivity.settings.emojiType)
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, body));
                            }
                        } else
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, body));
                            }
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(text);
                                Linkify.addLinks(textView, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                if (MainActivity.settings.smiliesType) {
                    textView.setText(EmoticonConverterNew.getSmiledText(context, body));
                } else {
                    textView.setText(EmoticonConverter.getSmiledText(context, body));
                }

                Linkify.addLinks(textView, Linkify.ALL);
            }
        } else if (MainActivity.settings.smilies.equals("none"))
        {
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                textView.setText(body);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (MainActivity.settings.emojiType)
                        {
                            text = EmojiConverter2.getSmiledText(context, body);
                        } else
                        {
                            text = EmojiConverter.getSmiledText(context, body);
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(text);
                                Linkify.addLinks(textView, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                textView.setText(body);
                Linkify.addLinks(textView, Linkify.ALL);
            }
        } else if (MainActivity.settings.smilies.equals("both"))
        {
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                textView.setText(body);

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (MainActivity.settings.emojiType)
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, body));
                            }
                        } else
                        {
                            if (MainActivity.settings.smiliesType) {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, body));
                            } else {
                                text = EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, body));
                            }
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                textView.setText(text);
                                Linkify.addLinks(textView, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                if (MainActivity.settings.smiliesType) {
                    textView.setText(EmoticonConverter3New.getSmiledText(context, body));
                } else {
                    textView.setText(EmoticonConverter3.getSmiledText(context, body));
                }

                Linkify.addLinks(textView, Linkify.ALL);
            }
        }
    }

    public void downloadableMessage(final ViewHolder holder, String id) {
        Cursor locationQuery = context.getContentResolver().query(Uri.parse("content://mms/"), new String[] {"m_size", "exp", "ct_l", "_id"}, "_id=?", new String[]{id}, null);

        if (locationQuery.moveToFirst()) {
            String exp = "1";
            String size = "1";

            try
            {
                size = locationQuery.getString(locationQuery.getColumnIndex("m_size"));
                exp = locationQuery.getString(locationQuery.getColumnIndex("exp"));
            } catch (Exception f)
            {

            }

            holder.image.setVisibility(View.VISIBLE);
            holder.bubble.setVisibility(View.VISIBLE);
            holder.media.setVisibility(View.GONE);
            holder.text.setText("");
            holder.text.setGravity(Gravity.CENTER);

            holder.text.setTextColor(MainActivity.settings.ctRecievedTextColor);
            holder.date.setTextColor(MainActivity.settings.ctRecievedTextColor);
            holder.background.setBackgroundColor(MainActivity.settings.ctRecievedMessageBackground);
            holder.media.setBackgroundColor(MainActivity.settings.ctRecievedMessageBackground);
            holder.bubble.setColorFilter(MainActivity.settings.ctRecievedMessageBackground);
            holder.date.setText("");

            boolean error2 = false;

            try
            {
                holder.date.setText("Message size: " + (int)(Double.parseDouble(size)/1000) + " KB Expires: " +  DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(Long.parseLong(exp) * 1000)));
                holder.downloadButton.setVisibility(View.VISIBLE);
            } catch (Exception f)
            {
                try {
                    holder.date.setText("Error loading message.");
                    holder.downloadButton.setVisibility(View.GONE);
                } catch (Exception g) {
                    error2 = true;
                }
            }

            holder.date.setGravity(Gravity.LEFT);

            if (!error2) {
                holder.downloadButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        holder.downloadButton.setVisibility(View.INVISIBLE);

                        Intent downloadMessage = new Intent(context, MmsReceiverService.class);
                        context.startService(downloadMessage);

                        context.registerReceiver(new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context context1, Intent intent) {
                                try {
                                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            holder.downloadButton.setVisibility(View.VISIBLE);
                                        }
                                    });

                                    context.unregisterReceiver(this);
                                } catch (Exception e) {

                                }
                            }
                        }, new IntentFilter("com.klinker.android.messaging.SHOW_DOWNLOAD_BUTTON"));
                    }

                });
            }
        }
    }

    public static String getMmsText(String id, Activity context) {
        Uri partURI = Uri.parse("content://mms/part/" + id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (temp != null) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {}
        finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {}
            }
        }
        return sb.toString();
    }

    private String getAddressNumber(int id) {
        String selectionAdd = new String("msg_id=" + id);
        String uriStr = "content://mms/" + id + "/addr";
        Uri uriAddress = Uri.parse(uriStr);
        Cursor cAdd = context.getContentResolver().query(uriAddress, null,
                selectionAdd, null, null);
        String name = "";
        if (cAdd != null)
        {
            if (cAdd.moveToFirst()) {
                do {
                    String number = cAdd.getString(cAdd.getColumnIndex("address"));
                    if (number != null) {
                        try {
                            Long.parseLong(number.replace("-", ""));
                            name += " " + number;
                        } catch (NumberFormatException nfe) {
                            name += " " + number;
                        }
                    }
                } while (cAdd.moveToNext());
            }

            cAdd.close();
        }

        return name.trim();
    }

    public static Date getZeroTimeDate(Date date) {
        Date res;
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        res = cal.getTime();

        return res;
    }


    Html.ImageGetter imgGetterSent = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = resources.getDrawable(R.drawable.ic_sent);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };

    Html.ImageGetter imgGetterFail = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = resources.getDrawable(R.drawable.ic_failed);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };

    Html.ImageGetter imgGetterLocked = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = resources.getDrawable(R.drawable.ic_locked);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            if (source.startsWith("sent")) {
                drawable.setColorFilter(convertToColorInt(convertToARGB(MainActivity.settings.ctSentTextColor, "55")), PorterDuff.Mode.MULTIPLY);
            } else {
                drawable.setColorFilter(convertToColorInt(convertToARGB(MainActivity.settings.ctRecievedTextColor, "55")), PorterDuff.Mode.MULTIPLY);
            }

            return drawable;
        }
    };

    Html.ImageGetter imgGetterVoice = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = resources.getDrawable(R.drawable.voice_message);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            drawable.setColorFilter(convertToColorInt(convertToARGB(MainActivity.settings.ctSentTextColor, "55")), PorterDuff.Mode.MULTIPLY);

            return drawable;
        }
    };

    public static String convertToARGB(int color, String a) {
        String alpha = a;

        if (alpha.length() > 2) {
            alpha = "FF";
        }

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
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }
}
