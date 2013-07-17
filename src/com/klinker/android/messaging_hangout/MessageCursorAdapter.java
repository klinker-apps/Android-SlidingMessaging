package com.klinker.android.messaging_hangout;

import android.annotation.SuppressLint;
import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SqliteWrapper;
import android.graphics.*;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.telephony.SmsManager;
import android.text.Html;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.android.mms.transaction.HttpUtils;
import com.android.mms.util.SendingProgressTokenManager;
import com.google.android.mms.APN;
import com.google.android.mms.APNHelper;
import com.google.android.mms.pdu.PduParser;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.RetrieveConf;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.StripAccents;
import com.klinker.android.messaging_donate.receivers.DisconnectWifi;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.receivers.NotificationReceiver;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageCursorAdapter extends CursorAdapter {

    private final Activity context;
    private final String myId;
    private final String inboxNumbers;
    private final int threadPosition;
    private final String threadIds;
    private final Bitmap contactImage;
    private final Bitmap myImage;
    private SharedPreferences sharedPrefs;
    private ContentResolver contentResolver;
    private Cursor cursor;
    private Paint paint;
    private Typeface font;
    private final LayoutInflater mInflater;

    public DisconnectWifi discon;
    public WifiInfo currentWifi;
    public boolean currentWifiState;
    public boolean currentDataState;

    public MessageCursorAdapter(Activity context, String myId, String inboxNumbers, String ids, Cursor query, int threadPosition) {
        super(context, query, 0);
        this.context = context;
        this.myId = myId;
        this.inboxNumbers = inboxNumbers;
        this.threadPosition = threadPosition;
        this.threadIds = ids;
        this.sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.contentResolver = context.getContentResolver();
        this.mInflater = LayoutInflater.from(context);
        this.cursor = query;

        Bitmap input;

        try
        {
            input = getFacebookPhoto(inboxNumbers);
        } catch (NumberFormatException e)
        {
            input = null;
        }

        if (input == null)
        {
            if (sharedPrefs.getBoolean("ct_darkContactImage", false))
            {
                input = drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar_dark));
            } else
            {
                input = drawableToBitmap(context.getResources().getDrawable(R.drawable.default_avatar));
            }
        }

        contactImage = Bitmap.createScaledBitmap(input, MainActivity.contactWidth, MainActivity.contactWidth, true);

        InputStream input2;

        try
        {
            input2 = openDisplayPhoto(Long.parseLong(this.myId));
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

        myImage = im;

        paint = new Paint();
        float densityMultiplier = context.getResources().getDisplayMetrics().density;
        float scaledPx = Integer.parseInt(sharedPrefs.getString("text_size", "14")) * densityMultiplier;
        paint.setTextSize(scaledPx);
        font = null;

        if (sharedPrefs.getBoolean("custom_font", false))
        {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
            paint.setTypeface(font);
        }
    }

    private int getItemViewType(Cursor query) {
        try
        {
            String s = query.getString(query.getColumnIndex("msg_box"));

            if (s != null) {
                if (query.getInt(query.getColumnIndex("msg_box")) == 4)
                {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 5)
                {
                    return 1;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 1)
                {
                    return 0;
                } else if (query.getInt(query.getColumnIndex("msg_box")) == 2)
                {
                    return 1;
                }
            } else
            {
                String type = query.getString(query.getColumnIndex("type"));

                if (type.equals("2") || type.equals("4") || type.equals("5") || type.equals("6"))
                {
                    return 1;
                } else
                {
                    return 0;
                }

            }
        } catch (Exception e)
        {
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
    public void bindView(View view, Context mContext, Cursor cursor) {
        final ViewHolder holder = (ViewHolder) view.getTag();

        boolean sent = false;
        boolean mms;
        String image;
        String video = "";
        String body;
        String date;
        String id;
        boolean sending = false;
        boolean error = false;
        boolean group = false;
        String sender = "";
        String status = "-1";
        String location;

        String dateType = "date";

        if (sharedPrefs.getBoolean("show_original_timestamp", false))
        {
            dateType = "date_sent";
        }

        try
        {
            String s = cursor.getString(cursor.getColumnIndex("msg_box"));

            if (s != null) {
                id = cursor.getString(cursor.getColumnIndex("_id"));
                mms = true;
                body = "";
                image = null;
                video = null;

                date = Long.parseLong(cursor.getString(cursor.getColumnIndex("date"))) * 1000 + "";

                String number = getAddressNumber(Integer.parseInt(cursor.getString(cursor.getColumnIndex("_id")))).trim();

                String[] numbers = number.split(" ");

                if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 4)
                {
                    sending = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 5)
                {
                    error = true;
                    sent = true;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 1)
                {
                    sent = false;
                } else if (cursor.getInt(cursor.getColumnIndex("msg_box")) == 2)
                {
                    sent = true;
                }

                if (numbers.length > 2)
                {
                    group = true;
                    sender = numbers[0];
                }

                if (cursor.getInt(cursor.getColumnIndex("read")) == 0)
                {
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
            } else {
                String type = cursor.getString(cursor.getColumnIndex("type"));

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

                    if (sharedPrefs.getBoolean("delivery_reports", false)) {
                        status = cursor.getString(cursor.getColumnIndex("status"));

                        if (status.equals("64") || status.equals("128"))
                        {
                            error = true;
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
                } else if (type.equals("4") || type.equals("6"))
                {
                    sent = true;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex("date"));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;
                    sending = true;
                } else
                {
                    sent = false;
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    date = cursor.getString(cursor.getColumnIndex(dateType));
                    id = cursor.getString(cursor.getColumnIndex("_id"));
                    mms = false;
                    image = null;
                }
            }
        } catch (Exception e)
        {
            e.printStackTrace();

            id = cursor.getString(cursor.getColumnIndex("_id"));
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

                location = locationQuery.getString(locationQuery.getColumnIndex("ct_l"));

                holder.image.setVisibility(View.VISIBLE);
                holder.bubble.setVisibility(View.VISIBLE);
                holder.media.setVisibility(View.GONE);
                holder.text.setText("");
                holder.text.setGravity(Gravity.CENTER);

                holder.text.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                holder.date.setTextColor(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                holder.background.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
                holder.media.setBackgroundColor(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
                holder.bubble.setColorFilter(sharedPrefs.getInt("ct_receivedMessageBackground", context.getResources().getColor(R.color.white)));
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

                final String downloadLocation = location;
                final String msgId = id;

                if (!error2) {
                    holder.downloadButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            holder.downloadButton.setVisibility(View.INVISIBLE);

                            if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                            {
                                WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                currentWifi = wifi.getConnectionInfo();
                                currentWifiState = wifi.isWifiEnabled();
                                wifi.disconnect();
                                discon = new DisconnectWifi();
                                context.registerReceiver(discon, new IntentFilter(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION));
                                currentDataState = MainActivity.isMobileDataEnabled(context);
                                MainActivity.setMobileDataEnabled(context, true);
                            }

                            ConnectivityManager mConnMgr =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            final int result = mConnMgr.startUsingNetworkFeature(ConnectivityManager.TYPE_MOBILE, "enableMMS");

                            if (result != 0)
                            {
                                IntentFilter filter = new IntentFilter();
                                filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
                                BroadcastReceiver receiver = new BroadcastReceiver() {

                                    @Override
                                    public void onReceive(final Context context, Intent intent) {
                                        String action = intent.getAction();

                                        if (!action.equals(ConnectivityManager.CONNECTIVITY_ACTION))
                                        {
                                            return;
                                        }

                                        @SuppressWarnings("deprecation")
                                        NetworkInfo mNetworkInfo = (NetworkInfo) intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);

                                        if ((mNetworkInfo == null) || (mNetworkInfo.getType() != ConnectivityManager.TYPE_MOBILE))
                                        {
                                            return;
                                        }

                                        if (!mNetworkInfo.isConnected())
                                        {
                                            return;
                                        } else
                                        {
                                            new Thread(new Runnable() {

                                                @Override
                                                public void run() {
                                                    List<APN> apns = new ArrayList<APN>();

                                                    try
                                                    {
                                                        APNHelper helper = new APNHelper(context);
                                                        apns = helper.getMMSApns();

                                                    } catch (Exception e)
                                                    {
                                                        APN apn = new APN(sharedPrefs.getString("mmsc_url", ""), sharedPrefs.getString("mms_port", ""), sharedPrefs.getString("mms_proxy", ""));
                                                        apns.add(apn);

                                                        String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
                                                        apns.get(0).MMSCenterUrl = mmscUrl;

                                                        try
                                                        {
                                                            if (sharedPrefs.getBoolean("apn_username_password", false))
                                                            {
                                                                if (!sharedPrefs.getString("apn_username", "").equals("") && !sharedPrefs.getString("apn_username", "").equals(""))
                                                                {
                                                                    String mmsc = apns.get(0).MMSCenterUrl;
                                                                    String[] parts = mmsc.split("://");
                                                                    String newMmsc = parts[0] + "://";

                                                                    newMmsc += sharedPrefs.getString("apn_username", "") + ":" + sharedPrefs.getString("apn_password", "") + "@";

                                                                    for (int i = 1; i < parts.length; i++)
                                                                    {
                                                                        newMmsc += parts[i];
                                                                    }

                                                                    apns.set(0, new APN(newMmsc, apns.get(0).MMSPort, apns.get(0).MMSProxy));
                                                                }
                                                            }
                                                        } catch (Exception f)
                                                        {
                                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                                @Override
                                                                public void run() {
                                                                    Toast.makeText(context, "There may be an error in your username and password settings.", Toast.LENGTH_LONG).show();
                                                                }
                                                            });
                                                        }
                                                    }

                                                    try {
                                                        byte[] resp = HttpUtils.httpConnection(
                                                                context, SendingProgressTokenManager.NO_TOKEN,
                                                                downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                                                                !TextUtils.isEmpty(apns.get(0).MMSProxy),
                                                                apns.get(0).MMSProxy,
                                                                Integer.parseInt(apns.get(0).MMSPort));

                                                        boolean groupMMS = false;

                                                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false))
                                                        {
                                                            groupMMS = true;
                                                        }

                                                        RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
                                                        PduPersister persister = PduPersister.getPduPersister(context);
                                                        Uri msgUri = persister.persist(retrieveConf, Telephony.Mms.Inbox.CONTENT_URI, true,
                                                                groupMMS, null);

                                                        ContentValues values = new ContentValues(1);
                                                        values.put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000L);
                                                        SqliteWrapper.update(context, context.getContentResolver(),
                                                                msgUri, values, null, null);
                                                        SqliteWrapper.delete(context, context.getContentResolver(),
                                                                Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadIds, msgId});

                                                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                ((MainActivity) context).refreshViewPager3();
                                                            }
                                                        });
                                                    } catch (Exception e) {
                                                        e.printStackTrace();

                                                        ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                            @Override
                                                            public void run() {
                                                                holder.downloadButton.setVisibility(View.VISIBLE);
                                                            }
                                                        });
                                                    }

                                                    if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                                                    {
                                                        try {
                                                            context.unregisterReceiver(discon);
                                                        } catch (Exception e) {

                                                        }

                                                        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                                        wifi.setWifiEnabled(false);
                                                        wifi.setWifiEnabled(currentWifiState);
                                                        Log.v("Reconnect", "" + wifi.reconnect());
                                                        MainActivity.setMobileDataEnabled(context, currentDataState);
                                                    }

                                                }

                                            }).start();

                                            context.unregisterReceiver(this);
                                        }

                                    }

                                };

                                context.registerReceiver(receiver, filter);
                            } else
                            {
                                new Thread(new Runnable() {

                                    @Override
                                    public void run() {
                                        List<APN> apns = new ArrayList<APN>();

                                        try
                                        {
                                            APNHelper helper = new APNHelper(context);
                                            apns = helper.getMMSApns();

                                        } catch (Exception e)
                                        {
                                            APN apn = new APN(sharedPrefs.getString("mmsc_url", ""), sharedPrefs.getString("mms_port", ""), sharedPrefs.getString("mms_proxy", ""));
                                            apns.add(apn);

                                            String mmscUrl = apns.get(0).MMSCenterUrl != null ? apns.get(0).MMSCenterUrl.trim() : null;
                                            apns.get(0).MMSCenterUrl = mmscUrl;

                                            try
                                            {
                                                if (sharedPrefs.getBoolean("apn_username_password", false))
                                                {
                                                    if (!sharedPrefs.getString("apn_username", "").equals("") && !sharedPrefs.getString("apn_username", "").equals(""))
                                                    {
                                                        String mmsc = apns.get(0).MMSCenterUrl;
                                                        String[] parts = mmsc.split("://");
                                                        String newMmsc = parts[0] + "://";

                                                        newMmsc += sharedPrefs.getString("apn_username", "") + ":" + sharedPrefs.getString("apn_password", "") + "@";

                                                        for (int i = 1; i < parts.length; i++)
                                                        {
                                                            newMmsc += parts[i];
                                                        }

                                                        apns.set(0, new APN(newMmsc, apns.get(0).MMSPort, apns.get(0).MMSProxy));
                                                    }
                                                }
                                            } catch (Exception f)
                                            {
                                                ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        Toast.makeText(context, "There may be an error in your username and password settings.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        }

                                        try {
                                            byte[] resp = HttpUtils.httpConnection(
                                                    context, SendingProgressTokenManager.NO_TOKEN,
                                                    downloadLocation, null, HttpUtils.HTTP_GET_METHOD,
                                                    !TextUtils.isEmpty(apns.get(0).MMSProxy),
                                                    apns.get(0).MMSProxy,
                                                    Integer.parseInt(apns.get(0).MMSPort));

                                            boolean groupMMS = false;

                                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1 && sharedPrefs.getBoolean("group_message", false))
                                            {
                                                groupMMS = true;
                                            }

                                            RetrieveConf retrieveConf = (RetrieveConf) new PduParser(resp).parse();
                                            PduPersister persister = PduPersister.getPduPersister(context);
                                            Uri msgUri = persister.persist(retrieveConf, Telephony.Mms.Inbox.CONTENT_URI, true,
                                                    groupMMS, null);

                                            ContentValues values = new ContentValues(1);
                                            values.put(Telephony.Mms.DATE, System.currentTimeMillis() / 1000L);
                                            SqliteWrapper.update(context, context.getContentResolver(),
                                                    msgUri, values, null, null);
                                            SqliteWrapper.delete(context, context.getContentResolver(),
                                                    Uri.parse("content://mms/"), "thread_id=? and _id=?", new String[] {threadIds, msgId});

                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    ((MainActivity) context).refreshViewPager3();
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();

                                            ((Activity) context).getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                @Override
                                                public void run() {
                                                    holder.downloadButton.setVisibility(View.VISIBLE);
                                                }
                                            });
                                        }

                                        if (sharedPrefs.getBoolean("wifi_mms_fix", true))
                                        {
                                            try {
                                                context.unregisterReceiver(discon);
                                            } catch (Exception e) {

                                            }

                                            WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                                            wifi.setWifiEnabled(false);
                                            wifi.setWifiEnabled(currentWifiState);
                                            Log.v("Reconnect", "" + wifi.reconnect());
                                            MainActivity.setMobileDataEnabled(context, currentDataState);
                                        }

                                    }

                                }).start();
                            }

                        }

                    });
                }

                view.setPadding(10,5,10,5);

                if (cursor.getPosition() == getCount() - 1)
                {
                    view.setPadding(10, 5, 10, 7);
                }
            }

            return;
        }

        if (group && !sent)
        {
            final String sentFrom = sender;
            new Thread(new Runnable() {

                @Override
                public void run()
                {
                    final Bitmap picture = Bitmap.createScaledBitmap(getFacebookPhoto(sentFrom), MainActivity.contactWidth, MainActivity.contactWidth, true);

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

        try
        {
            date2 = new Date(Long.parseLong(date));
        } catch (Exception e)
        {
            date2 = new Date(0);
        }

        Calendar cal = Calendar.getInstance();
        Date currentDate = new Date(cal.getTimeInMillis());

        if (getZeroTimeDate(date2).equals(getZeroTimeDate(currentDate)))
        {
            if (sharedPrefs.getBoolean("hour_format", false))
            {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
            } else
            {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
            }
        } else
        {
            if (sharedPrefs.getBoolean("hour_format", false))
            {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
            } else
            {
                holder.date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
            }
        }

        if (sending == true)
        {
            holder.date.setVisibility(View.GONE);

            try
            {
                holder.ellipsis.setVisibility(View.VISIBLE);
                holder.ellipsis.setBackgroundResource(R.drawable.ellipsis);
                holder.ellipsis.setColorFilter(sharedPrefs.getInt("ct_receivedTextColor", context.getResources().getColor(R.color.black)));
                AnimationDrawable ellipsis = (AnimationDrawable) holder.ellipsis.getBackground();
                ellipsis.start();
            } catch (Exception e)
            {

            }
        } else
        {
            holder.date.setVisibility(View.VISIBLE);

            try
            {
                holder.ellipsis.setVisibility(View.GONE);
            } catch (Exception e)
            {

            }

            if (sent == true && sharedPrefs.getBoolean("delivery_reports", false) && error == false && status.equals("0"))
            {
                String text = "<html><body><img src=\"ic_sent.png\"/> " + holder.date.getText().toString() + "</body></html>";
                holder.date.setText(Html.fromHtml(text, imgGetterSent, null));
            }
        }

        if (group == true && sent == false)
        {
            final String senderF = sender;
            // TODO see if this is really necessary, or if the holder can just be used instead... hopefully holder is fine
            final TextView dateView = (TextView) view.findViewById(R.id.textDate);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Uri phoneUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(senderF.replaceAll("-", "")));
                    final Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                        @Override
                        public void run() {
                            if(phonesCursor != null && phonesCursor.moveToFirst()) {
                                dateView.setText(holder.date.getText() + " - " + phonesCursor.getString(0));
                            } else
                            {
                                dateView.setText(holder.date.getText() + " - " + senderF);
                            }

                            phonesCursor.close();
                        }

                    });
                }
            }).start();
        }

        // TODO make this into a function by sending the holder into it, that way it can be easily used for MMS text as well without rewriting
        if (sharedPrefs.getString("smilies", "with").equals("with"))
        {
            String patternStr = "[^\\x20-\\x7E\\n]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                final String bodyF = body;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (sharedPrefs.getBoolean("emoji_type", true))
                        {
                            text = EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, bodyF));
                        } else
                        {
                            text = EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, bodyF));
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.text.setText(text);
                                Linkify.addLinks(holder.text, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                holder.text.setText(EmoticonConverter2.getSmiledText(context, body));
                Linkify.addLinks(holder.text, Linkify.ALL);
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("without"))
        {
            String patternStr = "[^\\x20-\\x7E\\n]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                final String bodyF = body;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (sharedPrefs.getBoolean("emoji_type", true))
                        {
                            text = EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, bodyF));
                        } else
                        {
                            text = EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, bodyF));
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.text.setText(text);
                                Linkify.addLinks(holder.text, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                holder.text.setText(EmoticonConverter.getSmiledText(context, body));
                Linkify.addLinks(holder.text, Linkify.ALL);
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("none"))
        {
            String patternStr = "[^\\x20-\\x7E\\n]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                final String bodyF = body;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (sharedPrefs.getBoolean("emoji_type", true))
                        {
                            text = EmojiConverter2.getSmiledText(context, bodyF);
                        } else
                        {
                            text = EmojiConverter.getSmiledText(context, bodyF);
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.text.setText(text);
                                Linkify.addLinks(holder.text, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                holder.text.setText(body);
                Linkify.addLinks(holder.text, Linkify.ALL);
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("both"))
        {
            String patternStr = "[^\\x20-\\x7E\\n]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find())
            {
                final String bodyF = body;

                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        final Spannable text;

                        if (sharedPrefs.getBoolean("emoji_type", true))
                        {
                            text = EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, bodyF));
                        } else
                        {
                            text = EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, bodyF));
                        }

                        context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                            @Override
                            public void run() {
                                holder.text.setText(text);
                                Linkify.addLinks(holder.text, Linkify.ALL);
                            }

                        });
                    }

                }).start();
            } else
            {
                holder.text.setText(EmoticonConverter3.getSmiledText(context, body));
                Linkify.addLinks(holder.text, Linkify.ALL);
            }
        }

        if (cursor.getPosition() == getCount() - 1)
        {
            view.setPadding(10,5,10,7);
        }

        final boolean mmsT = mms;
        final String imageT = image;
        final String dateT = date;
        final String idT = id;
        int size2 = 0;

        try
        {
            size2 = image.split(" ").length;
        } catch (Exception e)
        {

        }

        if (mms) {
            final View rowViewF = view;

            holder.media.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    rowViewF.performLongClick();
                    return true;
                }
            });
        }

        final String idF = id;
        final boolean mmsF = mms;
        final boolean sentF = sent;

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mmsF)
                {
                    Cursor query;
                    String dialogText = "";

                    try
                    {
                        if (!sentF)
                        {
                            query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "date_sent", "type", "address"}, null, null, "date desc limit 1");

                            if (query.moveToFirst())
                            {
                                String dateSent = query.getString(query.getColumnIndex("date_sent")), dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date1 = new Date(Long.parseLong(dateSent)), date2 = new Date(Long.parseLong(dateReceived));

                                if (sharedPrefs.getBoolean("hour_format", false))
                                {
                                    dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else
                                {
                                    dateSent = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date1) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date1);
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                        context.getResources().getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        context.getResources().getString(R.string.sent) + " " + dateSent + "\n" +
                                        context.getResources().getString(R.string.received) + " " + dateReceived;
                            }
                        } else
                        {
                            query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                            if (query.moveToFirst())
                            {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (sharedPrefs.getBoolean("hour_format", false))
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                        context.getResources().getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        context.getResources().getString(R.string.sent) + " " + dateReceived;

                                String status = query.getString(query.getColumnIndex("status"));

                                if (!status.equals("-1"))
                                {
                                    if (status.equals("64") || status.equals("128"))
                                    {
                                        dialogText += "\n" + context.getResources().getString(R.string.status) + " Error";
                                    } else
                                    {
                                        dialogText += "\n" + context.getResources().getString(R.string.status) + " Delivered";
                                    }
                                }
                            }
                        }
                    } catch (Exception e)
                    {
                        query = contentResolver.query(Uri.parse("content://sms/" + idF + "/"), new String[] {"date", "status", "type", "address"}, null, null, "date desc limit 1");

                        if (query.moveToFirst())
                        {
                            if (sentF)
                            {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (sharedPrefs.getBoolean("hour_format", false))
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                        context.getResources().getString(R.string.to) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        context.getResources().getString(R.string.sent) + " " + dateReceived;

                                String status = query.getString(query.getColumnIndex("status"));

                                if (!status.equals("-1"))
                                {
                                    if (status.equals("64") || status.equals("128"))
                                    {
                                        dialogText += "\n" + context.getResources().getString(R.string.status) + " Error";
                                    } else
                                    {
                                        dialogText += "\n" + context.getResources().getString(R.string.status) + " Delivered";
                                    }
                                }
                            } else
                            {
                                String dateReceived = query.getString(query.getColumnIndex("date"));
                                Date date2 = new Date(Long.parseLong(dateReceived));

                                if (sharedPrefs.getBoolean("hour_format", false))
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                } else
                                {
                                    dateReceived = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2);
                                }

                                dialogText = context.getResources().getString(R.string.type) + " Text Message\n" +
                                        context.getResources().getString(R.string.from) + " " + query.getString(query.getColumnIndex("address")) + "\n" +
                                        context.getResources().getString(R.string.received) + " " + dateReceived;
                            }
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(context.getResources().getString(R.string.message_details));
                    builder.setMessage(dialogText);
                    builder.create().show();
                }
            }
        });

        final int sizeT = size2;
        final boolean errorT = error;

        view.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(final View arg0) {
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(25);

                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);

                if (!errorT)
                {
                    if (!mmsT || sizeT > 1)
                    {
                        builder2.setItems(R.array.messageOptions, new DialogInterface.OnClickListener() {

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
                                        MainActivity.menu.showSecondaryMenu();

                                        View newMessageView = MainActivity.menu.getSecondaryMenu();

                                        EditText body = (EditText) newMessageView.findViewById(R.id.messageEntry2);
                                        TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);

                                        body.setText(tv2.getText().toString());

                                        break;
                                    case 2:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage(context.getResources().getString(R.string.delete_message));
                                        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            public void onClick(DialogInterface dialog, int id) {
                                                String threadId = threadIds;

                                                deleteSMS(context, threadId, idT);
                                                ((MainActivity) context).refreshViewPager(true);
                                            }

                                            public void deleteSMS(Context context, String threadId, String messageId) {
                                                try {
                                                    context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                                } catch (Exception e) {
                                                }
                                            }});
                                        builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog2 = builder.create();

                                        dialog2.show();
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
                        builder2.setItems(R.array.messageOptions2, new DialogInterface.OnClickListener() {

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
                                            saveImage(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(imageT)), dateT);
                                        } catch (FileNotFoundException e1) {
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                            e1.printStackTrace();
                                        } catch (IOException e1) {
                                            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
                                            e1.printStackTrace();
                                        } catch (Exception e)
                                        {
                                            Toast.makeText(context, "Nothing to Save", Toast.LENGTH_SHORT).show();
                                        }
                                        break;
                                    case 2:
                                        MainActivity.menu.showSecondaryMenu();

                                        View newMessageView = MainActivity.menu.getSecondaryMenu();

                                        EditText body = (EditText) newMessageView.findViewById(R.id.messageEntry2);
                                        TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);

                                        try
                                        {
                                            ((MainActivity)context).attachedImage2 = Uri.parse(imageT);

                                            ((MainActivity)context).imageAttachBackground2.setBackgroundColor(sharedPrefs.getInt("ct_conversationListBackground", context.getResources().getColor(R.color.light_silver)));
                                            Drawable attachBack = context.getResources().getDrawable(R.drawable.attachment_editor_bg);
                                            attachBack.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)), PorterDuff.Mode.MULTIPLY);
                                            ((MainActivity)context).imageAttach2.setBackgroundDrawable(attachBack);
                                            ((MainActivity)context).imageAttachBackground2.setVisibility(View.VISIBLE);
                                            ((MainActivity)context).imageAttach2.setVisibility(true);
                                        } catch (Exception e)
                                        {

                                        }

                                        try
                                        {
                                            ((MainActivity)context).imageAttach2.setImage("send_image", decodeFile(new File(getPath(Uri.parse(imageT)))));
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
                                                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(imageT)));

                                            }

                                        });

                                        replaceImage.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                Intent intent = new Intent();
                                                intent.setType("image/*");
                                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                                context.startActivityForResult(Intent.createChooser(intent, context.getResources().getString(R.string.select_picture)), 2);

                                            }

                                        });

                                        removeImage.setOnClickListener(new View.OnClickListener() {

                                            @Override
                                            public void onClick(View v) {
                                                ((MainActivity)context).imageAttach2.setVisibility(false);
                                                ((MainActivity)context).imageAttachBackground2.setVisibility(View.GONE);

                                            }

                                        });

                                        body.setText(tv2.getText().toString());

                                        try
                                        {

                                        } catch (Exception e)
                                        {

                                        }

                                        break;
                                    case 3:
                                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                        builder.setMessage(context.getResources().getString(R.string.delete_message));
                                        builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                            @SuppressLint("SimpleDateFormat")
                                            public void onClick(DialogInterface dialog, int id) {
                                                String threadId = threadIds;

                                                deleteSMS(context, threadId, idT);
                                                ((MainActivity) context).refreshViewPager(true);
                                            }

                                            public void deleteSMS(Context context, String threadId, String messageId) {
                                                try {
                                                    context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                                } catch (Exception e) {
                                                }
                                            }});
                                        builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                dialog.dismiss();
                                            }
                                        });
                                        AlertDialog dialog2 = builder.create();

                                        dialog2.show();
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
                    builder2.setItems(R.array.messageOptions3, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case 0:
                                    if (!mmsT)
                                    {
                                        MainActivity.animationOn = true;

                                        String body2 = ((TextView) arg0.findViewById(R.id.textBody)).getText().toString();

                                        if (!sharedPrefs.getString("signature", "").equals(""))
                                        {
                                            body2 += "\n" + sharedPrefs.getString("signature", "");
                                        }

                                        final String body = body2;

                                        new Thread(new Runnable() {

                                            @Override
                                            public void run() {

                                                if (sharedPrefs.getBoolean("delivery_reports", false))
                                                {
                                                    if (inboxNumbers.replaceAll("[^0-9]", "").equals(""))
                                                    {
                                                        String SENT = "SMS_SENT";
                                                        String DELIVERED = "SMS_DELIVERED";

                                                        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                                                                new Intent(SENT), 0);

                                                        PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                                                                new Intent(DELIVERED), 0);

                                                        //---when the SMS has been sent---
                                                        context.registerReceiver(new BroadcastReceiver(){
                                                            @Override
                                                            public void onReceive(Context arg0, Intent arg1) {
                                                                try {
                                                                    switch (getResultCode())
                                                                    {
                                                                        case Activity.RESULT_OK:
                                                                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "2");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            NotificationCompat.Builder mBuilder =
                                                                                    new NotificationCompat.Builder(context)
                                                                                            .setSmallIcon(R.drawable.ic_alert)
                                                                                            .setContentTitle("Error")
                                                                                            .setContentText("Could not send message");

                                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                                            stackBuilder.addParentStack(MainActivity.class);
                                                                            stackBuilder.addNextIntent(resultIntent);
                                                                            PendingIntent resultPendingIntent =
                                                                                    stackBuilder.getPendingIntent(
                                                                                            0,
                                                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                                                    );

                                                                            mBuilder.setContentIntent(resultPendingIntent);
                                                                            mBuilder.setAutoCancel(true);
                                                                            long[] pattern = {0L, 400L, 100L, 400L};
                                                                            mBuilder.setVibrate(pattern);
                                                                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                                            try
                                                                            {
                                                                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                                            } catch(Exception e)
                                                                            {
                                                                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                                            }

                                                                            NotificationManager mNotificationManager =
                                                                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                                            Notification notification = mBuilder.build();
                                                                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                                            mNotificationManager.notify(1, notification);
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "No service",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "Null PDU",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "Radio off",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                    }

                                                                    context.unregisterReceiver(this);
                                                                } catch (Exception e) {

                                                                }
                                                            }
                                                        }, new IntentFilter(SENT));

                                                        //---when the SMS has been delivered---
                                                        context.registerReceiver(new BroadcastReceiver(){
                                                            @Override
                                                            public void onReceive(Context arg0, Intent arg1) {
                                                                try {
                                                                    if (sharedPrefs.getString("delivery_options", "2").equals("1"))
                                                                    {
                                                                        switch (getResultCode())
                                                                        {
                                                                            case Activity.RESULT_OK:
                                                                                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                                                                builder.setMessage(R.string.message_delivered)
                                                                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        });

                                                                                builder.create().show();

                                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                                if (query.moveToFirst())
                                                                                {
                                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                                    ContentValues values = new ContentValues();
                                                                                    values.put("status", "0");
                                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }
                                                                                break;
                                                                            case Activity.RESULT_CANCELED:
                                                                                AlertDialog.Builder builder2 = new AlertDialog.Builder(context);
                                                                                builder2.setMessage(R.string.message_not_delivered)
                                                                                        .setPositiveButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                                                                                            public void onClick(DialogInterface dialog, int id) {
                                                                                                dialog.dismiss();
                                                                                            }
                                                                                        });

                                                                                builder2.create().show();

                                                                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                                if (query2.moveToFirst())
                                                                                {
                                                                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                                                                    ContentValues values = new ContentValues();
                                                                                    values.put("status", "64");
                                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }
                                                                                break;
                                                                        }
                                                                    } else
                                                                    {
                                                                        switch (getResultCode())
                                                                        {
                                                                            case Activity.RESULT_OK:
                                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                                {
                                                                                    Toast.makeText(context, R.string.message_delivered, Toast.LENGTH_LONG).show();
                                                                                }

                                                                                Cursor query = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                                if (query.moveToFirst())
                                                                                {
                                                                                    String id = query.getString(query.getColumnIndex("_id"));
                                                                                    ContentValues values = new ContentValues();
                                                                                    values.put("status", "0");
                                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }

                                                                                break;
                                                                            case Activity.RESULT_CANCELED:
                                                                                if (sharedPrefs.getString("delivery_options", "2").equals("2"))
                                                                                {
                                                                                    Toast.makeText(context, R.string.message_not_delivered, Toast.LENGTH_LONG).show();
                                                                                }

                                                                                Cursor query2 = context.getContentResolver().query(Uri.parse("content://sms/sent"), null, null, null, "date desc");

                                                                                if (query2.moveToFirst())
                                                                                {
                                                                                    String id = query2.getString(query2.getColumnIndex("_id"));
                                                                                    ContentValues values = new ContentValues();
                                                                                    values.put("status", "64");
                                                                                    context.getContentResolver().update(Uri.parse("content://sms/sent"), values, "_id=" + id, null);
                                                                                    ((MainActivity) context).refreshViewPager3();
                                                                                }
                                                                                break;
                                                                        }
                                                                    }

                                                                    context.unregisterReceiver(this);
                                                                } catch (Exception e) {

                                                                }
                                                            }
                                                        }, new IntentFilter(DELIVERED));

                                                        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
                                                        ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();

                                                        String body2 = body;

                                                        if (sharedPrefs.getBoolean("strip_unicode", false))
                                                        {
                                                            body2 = StripAccents.stripAccents(body2);
                                                        }

                                                        SmsManager smsManager = SmsManager.getDefault();
                                                        ArrayList<String> parts = smsManager.divideMessage(body2);

                                                        for (int i = 0; i < parts.size(); i++)
                                                        {
                                                            sPI.add(sentPI);
                                                            dPI.add(deliveredPI);
                                                        }

                                                        smsManager.sendMultipartTextMessage(inboxNumbers, null, parts, sPI, dPI);
                                                    } else
                                                    {
                                                    }
                                                } else
                                                {
                                                    if (!inboxNumbers.replaceAll("[^0-9]", "").equals(""))
                                                    {
                                                        String SENT = "SMS_SENT";

                                                        PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                                                                new Intent(SENT), 0);

                                                        //---when the SMS has been sent---
                                                        context.registerReceiver(new BroadcastReceiver(){
                                                            @Override
                                                            public void onReceive(Context arg0, Intent arg1) {
                                                                try {
                                                                    switch (getResultCode())
                                                                    {
                                                                        case Activity.RESULT_OK:
                                                                            Cursor query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "2");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            NotificationCompat.Builder mBuilder =
                                                                                    new NotificationCompat.Builder(context)
                                                                                            .setSmallIcon(R.drawable.ic_alert)
                                                                                            .setContentTitle("Error")
                                                                                            .setContentText("Could not send message");

                                                                            Intent resultIntent = new Intent(context, MainActivity.class);

                                                                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                                                                            stackBuilder.addParentStack(MainActivity.class);
                                                                            stackBuilder.addNextIntent(resultIntent);
                                                                            PendingIntent resultPendingIntent =
                                                                                    stackBuilder.getPendingIntent(
                                                                                            0,
                                                                                            PendingIntent.FLAG_UPDATE_CURRENT
                                                                                    );

                                                                            mBuilder.setContentIntent(resultPendingIntent);
                                                                            mBuilder.setAutoCancel(true);
                                                                            long[] pattern = {0L, 400L, 100L, 400L};
                                                                            mBuilder.setVibrate(pattern);
                                                                            mBuilder.setLights(0xFFffffff, 1000, 2000);

                                                                            try
                                                                            {
                                                                                mBuilder.setSound(Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                                                            } catch(Exception e)
                                                                            {
                                                                                mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                                                            }

                                                                            NotificationManager mNotificationManager =
                                                                                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                                                            Notification notification = mBuilder.build();
                                                                            Intent deleteIntent = new Intent(context, NotificationReceiver.class);
                                                                            notification.deleteIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
                                                                            mNotificationManager.notify(1, notification);
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_NO_SERVICE:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "No service",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_NULL_PDU:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "Null PDU",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                        case SmsManager.RESULT_ERROR_RADIO_OFF:
                                                                            query = context.getContentResolver().query(Uri.parse("content://sms/outbox"), null, null, null, null);

                                                                            if (query.moveToFirst())
                                                                            {
                                                                                String id = query.getString(query.getColumnIndex("_id"));
                                                                                ContentValues values = new ContentValues();
                                                                                values.put("type", "5");
                                                                                context.getContentResolver().update(Uri.parse("content://sms/outbox"), values, "_id=" + id, null);
                                                                                ((MainActivity) context).refreshViewPager3();
                                                                            }

                                                                            Toast.makeText(context, "Radio off",
                                                                                    Toast.LENGTH_SHORT).show();
                                                                            break;
                                                                    }

                                                                    context.unregisterReceiver(this);
                                                                } catch (Exception e) {

                                                                }
                                                            }
                                                        }, new IntentFilter(SENT));

                                                        ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();

                                                        String body2 = body;

                                                        if (sharedPrefs.getBoolean("strip_unicode", false))
                                                        {
                                                            body2 = StripAccents.stripAccents(body2);
                                                        }

                                                        SmsManager smsManager = SmsManager.getDefault();
                                                        ArrayList<String> parts = smsManager.divideMessage(body2);

                                                        for (int i = 0; i < parts.size(); i++)
                                                        {
                                                            sPI.add(sentPI);
                                                        }

                                                        smsManager.sendMultipartTextMessage(inboxNumbers, null, parts, sPI, null);
                                                    } else
                                                    {
                                                    }
                                                }

                                                String address = inboxNumbers;

                                                if (!address.replaceAll("[^0-9]", "").equals(""))
                                                {
                                                    final Calendar cal = Calendar.getInstance();
                                                    ContentValues values = new ContentValues();
                                                    values.put("address", address);
                                                    values.put("body", StripAccents.stripAccents(body));
                                                    values.put("date", cal.getTimeInMillis() + "");
                                                    values.put("thread_id", threadIds);
                                                    context.getContentResolver().insert(Uri.parse("content://sms/outbox"), values);

                                                    Cursor deleter = context.getContentResolver().query(Uri.parse("content://sms/failed"), null, null, null, null);

                                                    if (deleter.moveToFirst())
                                                    {
                                                        String id = deleter.getString(deleter.getColumnIndex("_id"));

                                                        context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadIds + "/"), "_id=" + id, null);
                                                    }

                                                    final String address2 = address;

                                                    context.getWindow().getDecorView().findViewById(android.R.id.content).post(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            MainActivity.sentMessage = true;
                                                            ((MainActivity) context).refreshViewPager4(address2, StripAccents.stripAccents(body), cal.getTimeInMillis() + "");
                                                        }

                                                    });
                                                }
                                            }

                                        }).start();
                                    } else
                                    {
                                        Toast.makeText(context, "Cannot resend MMS, try making a new message", Toast.LENGTH_LONG).show();
                                    }

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
                                    MainActivity.menu.showSecondaryMenu();

                                    View newMessageView = MainActivity.menu.getSecondaryMenu();

                                    EditText body3 = (EditText) newMessageView.findViewById(R.id.messageEntry2);
                                    TextView tv2 = (TextView) arg0.findViewById(R.id.textBody);

                                    body3.setText(tv2.getText().toString());

                                    break;
                                case 3:
                                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                                    builder.setMessage(context.getResources().getString(R.string.delete_message));
                                    builder.setPositiveButton(context.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                                        @SuppressLint("SimpleDateFormat")
                                        public void onClick(DialogInterface dialog, int id) {
                                            String threadId = threadIds;

                                            deleteSMS(context, threadId, idT);
                                            ((MainActivity) context).refreshViewPager(true);
                                        }

                                        public void deleteSMS(Context context, String threadId, String messageId) {
                                            try {
                                                context.getContentResolver().delete(Uri.parse("content://mms-sms/conversations/" + threadId + "/"), "_id=" + messageId, null);
                                            } catch (Exception e) {
                                            }
                                        }});
                                    builder.setNegativeButton(context.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.dismiss();
                                        }
                                    });
                                    AlertDialog dialog2 = builder.create();

                                    dialog2.show();
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
        
        // TODO add in animations to view (same as in array adapter)
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View v = null;

        int type = getItemViewType(cursor);

        if (type == 1) {
            v = mInflater.inflate(R.layout.message_hangout_sent, parent, false);
            holder.text = (TextView) v.findViewById(R.id.textBody);

            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            holder.ellipsis = (ImageView) v.findViewById(R.id.ellipsis);
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = (LinearLayout) v.findViewById(R.id.messageBody);

            holder.image.assignContactUri(ContactsContract.Profile.CONTENT_URI);
        } else {
            v = mInflater.inflate(R.layout.message_hangout_received, parent, false);
            holder.text = (TextView) v.findViewById(R.id.textBody);
            holder.date = (TextView) v.findViewById(R.id.textDate);
            holder.media = (ImageView) v.findViewById(R.id.media);
            holder.image = (QuickContactBadge) v.findViewById(R.id.imageContactPicture);
            holder.downloadButton = (Button) v.findViewById(R.id.downloadButton);
            holder.bubble = (ImageView) v.findViewById(R.id.msgBubble);
            holder.background = (LinearLayout) v.findViewById(R.id.messageBody);

            holder.image.assignContactFromPhone(inboxNumbers, true);
        }

        if (sharedPrefs.getBoolean("custom_font", false))
        {
            holder.text.setTypeface(font);
            holder.date.setTypeface(font);
        }

        if (sharedPrefs.getBoolean("contact_pictures", true))
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
            holder.text.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
            holder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)) - 4);
        } catch (Exception e) {
            holder.text.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)));
            holder.date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,1)) - 4);
        }

        if (sharedPrefs.getBoolean("tiny_date", false))
        {
            holder.date.setTextSize(10);
        }

        holder.text.setText("");
        holder.date.setText("");

        holder.media.setVisibility(View.GONE);

        if (type == 0) {
            holder.downloadButton.setVisibility(View.GONE);
        }

        holder.date.setAlpha((float) .5);

        holder.text.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
        holder.date.setTextColor(sharedPrefs.getInt("ct_sentTextColor", context.getResources().getColor(R.color.black)));
        holder.background.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
        holder.media.setBackgroundColor(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));
        holder.bubble.setColorFilter(sharedPrefs.getInt("ct_sentMessageBackground", context.getResources().getColor(R.color.white)));

        if (!sharedPrefs.getBoolean("custom_theme", false))
        {
            String color = sharedPrefs.getString("sent_text_color", "default");

            if (color.equals("blue"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.holo_blue));
                holder.date.setTextColor(context.getResources().getColor(R.color.holo_blue));
            } else if (color.equals("white"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.white));
                holder.date.setTextColor(context.getResources().getColor(R.color.white));
            } else if (color.equals("green"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.holo_green));
                holder.date.setTextColor(context.getResources().getColor(R.color.holo_green));
            } else if (color.equals("orange"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.holo_orange));
                holder.date.setTextColor(context.getResources().getColor(R.color.holo_orange));
            } else if (color.equals("red"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.holo_red));
                holder.date.setTextColor(context.getResources().getColor(R.color.holo_red));
            } else if (color.equals("purple"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.holo_purple));
                holder.date.setTextColor(context.getResources().getColor(R.color.holo_purple));
            } else if (color.equals("black"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.pitch_black));
                holder.date.setTextColor(context.getResources().getColor(R.color.pitch_black));
            } else if (color.equals("grey"))
            {
                holder.text.setTextColor(context.getResources().getColor(R.color.grey));
                holder.date.setTextColor(context.getResources().getColor(R.color.grey));
            }
        }

        if (!sharedPrefs.getString("text_alignment", "split").equals("split"))
        {
            if (sharedPrefs.getString("text_alignment", "split").equals("right"))
            {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            } else
            {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            }
        } else if (!sharedPrefs.getBoolean("contact_pictures", true)) {
            if (type == 0) {
                holder.text.setGravity(Gravity.LEFT);
                holder.date.setGravity(Gravity.LEFT);
            } else {
                holder.text.setGravity(Gravity.RIGHT);
                holder.date.setGravity(Gravity.RIGHT);
            }
        }

        v.setPadding(10,5,10,5);

        v.setTag(holder);
        return v;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (!cursor.moveToPosition(getCount() - 1 - position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
        View v;
        if (convertView == null) {
            v = newView(context, cursor, parent);
        } else {
            v = convertView;
        }
        bindView(v, context, cursor);
        return v;
    }

    static class ViewHolder {
        public TextView text;
        public TextView date;
        public QuickContactBadge image;
        public LinearLayout background;
        public ImageView media;
        public ImageView ellipsis;
        public Button downloadButton;
        public ImageView bubble;
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

    @SuppressWarnings("deprecation")
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.managedQuery(uri, projection, null, null, null);
        context.startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
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

    private static String getMmsText(String id, Activity context) {
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

    private void saveImage(Bitmap finalBitmap, String d) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Download");
        myDir.mkdirs();
        String fname = d + ".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        Toast.makeText(context, context.getResources().getString(R.string.save_image), Toast.LENGTH_SHORT).show();
    }

    private Bitmap decodeFile(File f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=200;

            //Find the correct scale value. It should be the power of 2.
            int scale=1;
            while(o.outWidth/scale/2>=REQUIRED_SIZE && o.outHeight/scale/2>=REQUIRED_SIZE)
                scale*=2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    public static Date getZeroTimeDate(Date fecha) {
        Date res = fecha;
        Calendar cal = Calendar.getInstance();

        cal.setTime( fecha );
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        res = (Date) cal.getTime();

        return res;
    }


    Html.ImageGetter imgGetterSent = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;

            drawable = context.getResources().getDrawable(R.drawable.ic_sent);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };

    Html.ImageGetter imgGetterFail = new Html.ImageGetter() {
        @Override
        public Drawable getDrawable(String source) {
            Drawable drawable = null;

            drawable = context.getResources().getDrawable(R.drawable.ic_failed);

            drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable
                    .getIntrinsicHeight());

            return drawable;
        }
    };

}
