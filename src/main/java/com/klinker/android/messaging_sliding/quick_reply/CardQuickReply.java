package com.klinker.android.messaging_sliding.quick_reply;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.emojis.*;
import com.klinker.android.messaging_sliding.notifications.NotificationMessage;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CardQuickReply extends FragmentActivity {

    public SectionsPagerAdapter mSectionsPagerAdapter;
    public ViewPager mViewPager;

    public ArrayList<String> ids, inboxBody, inboxDate, inboxNumber;
    public static SharedPreferences sharedPrefs;

    public static Typeface font;

    public BroadcastReceiver receiver;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPrefs.getBoolean("show_keyboard_popup", true)) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        if (sharedPrefs.getBoolean("unlock_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        setContentView(R.layout.card_popup);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle extras = intent.getExtras();

                String body = "";
                String address = "";
                String date = "";

                if (extras != null) {
                    Object[] smsExtra = (Object[]) extras.get("pdus");

                    for (int i = 0; i < smsExtra.length; ++i) {
                        SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

                        body += sms.getMessageBody().toString();
                        address = sms.getOriginatingAddress();
                        date = sms.getTimestampMillis() + "";
                    }
                }

                Calendar cal = Calendar.getInstance();
                ContentValues values = new ContentValues();
                values.put("address", address);
                values.put("body", body);
                values.put("date", cal.getTimeInMillis() + "");
                values.put("read", false);
                values.put("date_sent", date);
                getContentResolver().insert(Uri.parse("content://sms/inbox"), values);

                if (sharedPrefs.getBoolean("notifications", true)) {
                    AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

                    switch (am.getRingerMode()) {
                        case AudioManager.RINGER_MODE_SILENT:
                            break;
                        case AudioManager.RINGER_MODE_VIBRATE:
                            if (sharedPrefs.getBoolean("vibrate", true)) {
                                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

                                if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false)) {
                                    String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");

                                    if (vibPat.equals("short")) {
                                        long[] pattern = {0L, 400L};
                                        vibrator.vibrate(pattern, -1);
                                    } else if (vibPat.equals("long")) {
                                        long[] pattern = {0L, 800L};
                                        vibrator.vibrate(pattern, -1);
                                    } else if (vibPat.equals("2short")) {
                                        long[] pattern = {0L, 400L, 100L, 400L};
                                        vibrator.vibrate(pattern, -1);
                                    } else if (vibPat.equals("2long")) {
                                        long[] pattern = {0L, 800L, 200L, 800L};
                                        vibrator.vibrate(pattern, -1);
                                    } else if (vibPat.equals("3short")) {
                                        long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
                                        vibrator.vibrate(pattern, -1);
                                    } else if (vibPat.equals("3long")) {
                                        long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
                                        vibrator.vibrate(pattern, -1);
                                    }
                                } else {
                                    try {
                                        String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
                                        long[] pattern = new long[vibPat.length];

                                        for (int i = 0; i < vibPat.length; i++) {
                                            pattern[i] = Long.parseLong(vibPat[i]);
                                        }

                                        vibrator.vibrate(pattern, -1);
                                    } catch (Exception e) {

                                    }
                                }
                            }

                            break;
                        case AudioManager.RINGER_MODE_NORMAL:
                            try {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

                                try {
                                    notification = (Uri.parse(sharedPrefs.getString("ringtone", "null")));
                                } catch (Exception e) {
                                    notification = (RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                                }

                                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                                r.play();

                                if (sharedPrefs.getBoolean("vibrate", true)) {
                                    Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

                                    if (!sharedPrefs.getBoolean("custom_vibrate_pattern", false)) {
                                        String vibPat = sharedPrefs.getString("vibrate_pattern", "2short");

                                        if (vibPat.equals("short")) {
                                            long[] pattern = {0L, 400L};
                                            vibrator.vibrate(pattern, -1);
                                        } else if (vibPat.equals("long")) {
                                            long[] pattern = {0L, 800L};
                                            vibrator.vibrate(pattern, -1);
                                        } else if (vibPat.equals("2short")) {
                                            long[] pattern = {0L, 400L, 100L, 400L};
                                            vibrator.vibrate(pattern, -1);
                                        } else if (vibPat.equals("2long")) {
                                            long[] pattern = {0L, 800L, 200L, 800L};
                                            vibrator.vibrate(pattern, -1);
                                        } else if (vibPat.equals("3short")) {
                                            long[] pattern = {0L, 400L, 100L, 400L, 100L, 400L};
                                            vibrator.vibrate(pattern, -1);
                                        } else if (vibPat.equals("3long")) {
                                            long[] pattern = {0L, 800L, 200L, 800L, 200L, 800L};
                                            vibrator.vibrate(pattern, -1);
                                        }
                                    } else {
                                        try {
                                            String[] vibPat = sharedPrefs.getString("set_custom_vibrate_pattern", "0, 100, 100, 100").split(", ");
                                            long[] pattern = new long[vibPat.length];

                                            for (int i = 0; i < vibPat.length; i++) {
                                                pattern[i] = Long.parseLong(vibPat[i]);
                                            }

                                            vibrator.vibrate(pattern, -1);
                                        } catch (Exception e) {

                                        }
                                    }
                                }
                            } catch (Exception e) {

                            }

                            break;
                    }
                }


                boolean flag = false;
                int pos = 0;
                int pos2 = mViewPager.getCurrentItem();

                for (int i = 0; i < inboxNumber.size(); i++) {
                    if (inboxNumber.get(i).equals(address)) {
                        flag = true;
                        pos = i;
                        break;
                    }
                }

                if (!flag) {
                    inboxNumber.add(0, address);
                    inboxDate.add(0, date);
                    inboxBody.add(0, body);

                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/inbox/"), new String[]{"_id", "date"}, null, null, "date desc limit 1");

                    if (query.moveToFirst()) {
                        String id = query.getString(query.getColumnIndex("_id"));
                        ids.add(0, id);
                    }
                } else {
                    inboxBody.set(pos, inboxBody.get(pos) + "\n\n" + body);

                    Cursor query = context.getContentResolver().query(Uri.parse("content://sms/inbox/"), new String[]{"_id", "date"}, null, null, "date desc limit 1");
                    query.moveToFirst();
                    String id = query.getString(query.getColumnIndex("_id"));

                    ids.set(pos, ids.get(pos) + ", " + id);
                }

                mSectionsPagerAdapter.notifyDataSetChanged();

                mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

                mViewPager = (ViewPager) findViewById(R.id.messagePager);
                mViewPager.setAdapter(mSectionsPagerAdapter);
                mViewPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / -18);
                mViewPager.setOffscreenPageLimit(2);

                if (!flag) {
                    mViewPager.setCurrentItem(pos2 + 1);
                } else {
                    mViewPager.setCurrentItem(pos2);
                }

                Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                context.sendBroadcast(updateWidget);

                abortBroadcast();
            }
        };

        ColorDrawable background = new ColorDrawable();
        background.setColor(getResources().getColor(R.color.black));
        background.setAlpha(150);
        getWindow().setBackgroundDrawable(background);
    }

    public void removePage(int page) {
        ids.remove(page);
        inboxNumber.remove(page);
        inboxBody.remove(page);
        inboxDate.remove(page);
        mSectionsPagerAdapter.notifyDataSetChanged();

        if (ids.size() == 0) {
            if (sharedPrefs.getBoolean("voice_enabled", false)) {
                registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        unregisterReceiver(this);
                        finish();
                    }
                }, new IntentFilter(Transaction.REFRESH));
            } else {
                finish();
            }
        } else {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

            mViewPager = (ViewPager) findViewById(R.id.messagePager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / -18);
            mViewPager.setOffscreenPageLimit(2);
        }

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);

        Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
        sendBroadcast(intent);

        Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
        PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pStopRepeating);

        Intent floatingNotifications = new Intent();
        floatingNotifications.setAction("robj.floating.notifications.dismiss");
        floatingNotifications.putExtra("package", getPackageName());
        sendBroadcast(floatingNotifications);

        Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
        sendBroadcast(updateWidget);

        IOUtil.writeNotifications(new ArrayList<NotificationMessage>(), this);
        IOUtil.writeNewMessages(new ArrayList<String>(), this);
    }

    @Override
    public void onResume() {
        super.onResume();

        ids = new ArrayList<String>();
        inboxBody = new ArrayList<String>();
        inboxDate = new ArrayList<String>();
        inboxNumber = new ArrayList<String>();

        try {
            inboxBody.add(getIntent().getStringExtra("body"));
            inboxDate.add(getIntent().getStringExtra("date"));
            inboxNumber.add(getIntent().getStringExtra("address").replace("-", "").replace(")", "").replace("(", "").replace(" ", ""));
            ids.add("0");
        } catch (Exception e) {

        }

        String[] projection = new String[]{"_id", "date", "address", "body", "read"};
        Uri uri = Uri.parse("content://sms/inbox/");
        Cursor query = getContentResolver().query(uri, projection, "read=?", new String[]{"0"}, "date desc");

        if (query.moveToFirst()) {
            do {
                boolean alreadyExists = false;
                int alreadyExistsPos = 0;

                String number;

                try {
                    number = query.getString(query.getColumnIndex("address")).replace("-", "").replace(")", "").replace("(", "").replace(" ", "");
                } catch (Exception e) {
                    number = "";
                }

                for (int i = 0; i < inboxNumber.size(); i++) {
                    if (number.equals(inboxNumber.get(i))) {
                        alreadyExists = true;
                        alreadyExistsPos = i;
                        break;
                    }
                }

                if (!alreadyExists) {
                    inboxBody.add(query.getString(query.getColumnIndex("body")));
                    inboxDate.add(query.getString(query.getColumnIndex("date")));
                    inboxNumber.add(number);
                    ids.add(query.getString(query.getColumnIndex("_id")));
                } else {
                    if (!query.getString(query.getColumnIndex("body")).equals(inboxBody.get(0))) {
                        inboxBody.set(alreadyExistsPos, query.getString(query.getColumnIndex("body")) + "\n\n" + inboxBody.get(alreadyExistsPos));
                        ids.set(alreadyExistsPos, ids.get(alreadyExistsPos) + ", " + query.getString(query.getColumnIndex("_id")));
                    }
                }
            } while (query.moveToNext());
        }

        query.close();

        if (sharedPrefs.getBoolean("custom_font", false)) {
            font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));
        }

        RelativeLayout expandedOptions = (RelativeLayout) findViewById(R.id.expandedOptions);
        RelativeLayout sendBar = (RelativeLayout) findViewById(R.id.sendBar);
        Button viewConversation = (Button) findViewById(R.id.viewConversationButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);
        ImageButton readButton = (ImageButton) findViewById(R.id.readButton);
        ImageButton emojiButton = (ImageButton) findViewById(R.id.emojiButton);
        final ImageButton sendButton = (ImageButton) findViewById(R.id.sendButton);
        final EditText messageEntry = (EditText) findViewById(R.id.messageEntry);
        final TextView charsRemaining = (TextView) findViewById(R.id.charsRemaining);

        if (sharedPrefs.getString("cp_theme_name", "Light Theme").equals("Light Theme")) {
            expandedOptions.setBackgroundResource(R.drawable.card_background);
            sendBar.setBackgroundResource(R.drawable.card_background);
        } else if (sharedPrefs.getString("cp_theme_name", "Light Theme").equals("Dark Theme")) {
            expandedOptions.setBackgroundResource(R.drawable.card_background_dark);
            sendBar.setBackgroundResource(R.drawable.card_background_dark);
        } else {
            expandedOptions.setBackgroundColor(sharedPrefs.getInt("cp_sendBarBackground", getResources().getColor(R.color.white)));
            sendBar.setBackgroundColor(sharedPrefs.getInt("cp_sendBarBackground", getResources().getColor(R.color.white)));
        }

        viewConversation.setTextColor(sharedPrefs.getInt("cp_buttonColor", getResources().getColor(R.color.card_message_text_body)));
        deleteButton.setColorFilter(sharedPrefs.getInt("cp_buttonColor", getResources().getColor(R.color.card_message_text_body)));
        readButton.setColorFilter(sharedPrefs.getInt("cp_buttonColor", getResources().getColor(R.color.card_message_text_body)));
        sendButton.setColorFilter(sharedPrefs.getInt("cp_buttonColor", getResources().getColor(R.color.card_message_text_body)));
        charsRemaining.setTextColor(sharedPrefs.getInt("cp_buttonColor", getResources().getColor(R.color.card_message_text_body)));
        emojiButton.setColorFilter(sharedPrefs.getInt("cp_emojiButtonColor", getResources().getColor(R.color.emoji_button)));
        messageEntry.setTextColor(sharedPrefs.getInt("cp_draftTextColor", getResources().getColor(R.color.card_message_text_body)));

        if (sharedPrefs.getBoolean("custom_font", false)) {
            viewConversation.setTypeface(font);
            messageEntry.setTypeface(font);
            charsRemaining.setTypeface(font);
        }

        if (!sharedPrefs.getBoolean("keyboard_type", true)) {
            messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        if (!sharedPrefs.getBoolean("emoji", false)) {
            emojiButton.setVisibility(View.GONE);
            LayoutParams params = (RelativeLayout.LayoutParams) messageEntry.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            messageEntry.setLayoutParams(params);
        } else {
            final Context context = this;

            if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
                emojiButton.setImageResource(R.drawable.ic_emoji_dark);
            }

            emojiButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context, android.R.style.Theme_Holo_Dialog);
                    builder.setTitle("Insert Emojis");
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    View frame = inflater.inflate(R.layout.emoji_frame, null);

                    final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                    ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                    ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                    ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                    ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                    ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);

                    final GridView emojiGrid = (GridView) frame.findViewById(R.id.emojiGrid);
                    Button okButton = (Button) frame.findViewById(R.id.emoji_ok);

                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        emojiGrid.setAdapter(new EmojiAdapter2(context));
                        emojiGrid.setOnItemClickListener(new OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(0);
                            }
                        });

                        objectsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + (2 * 7));
                            }
                        });

                        natureButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + (3 * 7));
                            }
                        });

                        placesButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                            }
                        });

                        symbolsButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                            }
                        });
                    } else {
                        emojiGrid.setAdapter(new EmojiAdapter(context));
                        emojiGrid.setOnItemClickListener(new OnItemClickListener() {

                            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                                editText.setText(EmojiConverter.getSmiledText(context, editText.getText().toString() + EmojiAdapter.mEmojiTexts[position]));
                                editText.setSelection(editText.getText().length());
                            }
                        });

                        peopleButton.setMaxHeight(0);
                        objectsButton.setMaxHeight(0);
                        natureButton.setMaxHeight(0);
                        placesButton.setMaxHeight(0);
                        symbolsButton.setMaxHeight(0);

                        LinearLayout buttons = (LinearLayout) frame.findViewById(R.id.linearLayout);
                        buttons.setMinimumHeight(0);
                        buttons.setVisibility(View.GONE);
                    }

                    builder.setView(frame);
                    final AlertDialog dialog = builder.create();
                    dialog.show();

                    okButton.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (sharedPrefs.getBoolean("emoji_type", true)) {
                                messageEntry.setText(EmojiConverter2.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
                                messageEntry.setSelection(messageEntry.getText().length());
                            } else {
                                messageEntry.setText(EmojiConverter.getSmiledText(context, messageEntry.getText().toString() + editText.getText().toString()));
                                messageEntry.setSelection(messageEntry.getText().length());
                            }

                            dialog.dismiss();
                        }

                    });
                }

            });
        }

        if (!sharedPrefs.getBoolean("enable_view_conversation", false)) {
            expandedOptions.setVisibility(View.GONE);
        } else {
            final Context context = this;

            viewConversation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("com.klinker.android.OPEN_THREAD", inboxNumber.get(mViewPager.getCurrentItem()));
                    intent.putExtra("com.klinker.android.CURRENT_TEXT", messageEntry.getText().toString());
                    startActivity(intent);

                    removePage(mViewPager.getCurrentItem());
                }

            });

            readButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    if (ids.size() == 0) {
                        finish();
                        return;
                    }

                    final String id = ids.get(mViewPager.getCurrentItem());
                    final String date = inboxDate.get(mViewPager.getCurrentItem());

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            String[] msgIds = id.split(", ");

                            for (int i = 0; i < msgIds.length; i++) {
                                try {
                                    if (msgIds[i].equals("0")) {
                                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"_id", "date"}, "date=?", new String[]{date}, null);
                                        query.moveToFirst();
                                        msgIds[i] = query.getString(query.getColumnIndex("_id"));
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put("read", true);
                                    getContentResolver().update(Uri.parse("content://sms/" + msgIds[i].replace(",", "").replace(" ", "") + "/"), values, null, null);
                                } catch (Exception e) {

                                }
                            }

                        }

                    }).start();

                    removePage(mViewPager.getCurrentItem());
                }

            });

            deleteButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    final String id = ids.get(mViewPager.getCurrentItem());
                    final String date = inboxDate.get(mViewPager.getCurrentItem());

                    new Thread(new Runnable() {

                        @Override
                        public void run() {
                            String[] msgIds = id.split(", ");

                            for (int i = 0; i < msgIds.length; i++) {
                                try {
                                    if (msgIds[i].equals("0")) {
                                        Cursor query = context.getContentResolver().query(Uri.parse("content://sms/inbox"), new String[]{"_id", "date"}, "date=?", new String[]{date}, null);
                                        query.moveToFirst();
                                        msgIds[i] = query.getString(query.getColumnIndex("_id"));
                                    }

                                    ContentValues values = new ContentValues();
                                    values.put("read", true);
                                    getContentResolver().update(Uri.parse("content://sms/" + msgIds[i].replace(",", "").replace(" ", "") + "/"), values, null, null);
                                    context.getContentResolver().delete(Uri.parse("content://sms/" + msgIds[i].replace(",", "").replace(" ", "") + "/"), null, null);
                                } catch (Exception e) {

                                }
                            }

                        }

                    }).start();

                    removePage(mViewPager.getCurrentItem());
                }
            });
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && sharedPrefs.getBoolean("show_keyboard_popup", true)) {
            messageEntry.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(messageEntry, 0);
                }
            }, 500);
        }

        charsRemaining.setVisibility(View.GONE);

        messageEntry.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int length = Integer.parseInt(String.valueOf(s.length()));

                if (!sharedPrefs.getString("signature", "").equals("")) {
                    length += ("\n" + sharedPrefs.getString("signature", "")).length();
                }

                String patternStr = "[^" + Utils.GSM_CHARACTERS_REGEX + "]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(s);

                int size = 160;

                if (matcher.find() && !sharedPrefs.getBoolean("strip_unicode", false)) {
                    size = 70;
                }

                int pages = 1;

                while (length > size) {
                    length -= size;
                    pages++;
                }

                charsRemaining.setText(pages + "/" + (size - length));

                if ((pages == 1 && (size - length) <= 30) || pages != 1) {
                    charsRemaining.setVisibility(View.VISIBLE);
                }

                if ((pages + "/" + (size - length)).equals("1/31")) {
                    charsRemaining.setVisibility(View.GONE);
                }

                if ((pages + "/" + (size - length)).equals("1/160")) {
                    charsRemaining.setVisibility(View.GONE);
                }

                if (sharedPrefs.getBoolean("send_with_return", false)) {
                    if (messageEntry.getText().toString().endsWith("\n")) {
                        messageEntry.setText(messageEntry.getText().toString().substring(0, messageEntry.getText().toString().length() - 1));
                        sendButton.performClick();
                    }
                }
            }

            public void afterTextChanged(Editable s) {
            }
        });

        final Context context = this;

        sendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                final String number = inboxNumber.get(mViewPager.getCurrentItem());
                final String body = messageEntry.getText().toString();

                if (!body.equals("")) {
                    SendUtil.sendMessage(context, number, body);

                    messageEntry.setText("");
                    removePage(mViewPager.getCurrentItem());
                } else {
                    Toast.makeText(context, "ERROR: No message to send.", Toast.LENGTH_SHORT);
                }

            }

        });

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = (ViewPager) findViewById(R.id.messagePager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setPageMargin(getResources().getDisplayMetrics().widthPixels / -18);
        mViewPager.setOffscreenPageLimit(2);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        filter.setPriority(3);
        registerReceiver(receiver, filter);
    }

    @Override
    public void finish() {
        try {
            unregisterReceiver(receiver);
        } catch (Exception e) {

        }

        if (sharedPrefs.getBoolean("cache_conversations", false)) {
            Intent cacheService = new Intent(this, CacheService.class);
            this.startService(cacheService);
        }

        super.finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);

        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new PagerFragment();
            Bundle args = new Bundle();
            args.putInt("position", position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return inboxNumber.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "";
        }
    }

    public class PagerFragment extends Fragment {

        public int position;
        public Context context;

        public PagerFragment() {

        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            context = activity;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Bundle args = getArguments();

            position = args.getInt("position");
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View message = inflater.inflate(R.layout.card_popup_message,
                    container, false);

            View background = message.findViewById(R.id.view1);
            View divider1 = message.findViewById(R.id.contactLine);
            View divider2 = message.findViewById(R.id.messageDivider1);
            View divider3 = message.findViewById(R.id.messageDivider2);

            TextView name = (TextView) message.findViewById(R.id.contactName);
            TextView number = (TextView) message.findViewById(R.id.contactNumber);
            TextView body = (TextView) message.findViewById(R.id.body);
            TextView date = (TextView) message.findViewById(R.id.date);
            QuickContactBadge photo = (QuickContactBadge) message.findViewById(R.id.contactPicture);

            ImageButton close = (ImageButton) message.findViewById(R.id.closeButton);

            body.setMovementMethod(new ScrollingMovementMethod());

            if (sharedPrefs.getString("cp_theme_name", "Light Theme").equals("Light Theme")) {
                background.setBackgroundResource(R.drawable.card_background);
            } else if (sharedPrefs.getString("cp_theme_name", "Light Theme").equals("Dark Theme")) {
                background.setBackgroundResource(R.drawable.card_background_dark);
            } else {
                background.setBackgroundColor(sharedPrefs.getInt("cp_messageBackground", getResources().getColor(R.color.white)));
            }

            divider1.setBackgroundColor(sharedPrefs.getInt("cp_dividerColor", context.getResources().getColor(R.color.card_conversation_divider)));
            divider2.setBackgroundColor(sharedPrefs.getInt("cp_dividerColor", context.getResources().getColor(R.color.card_conversation_divider)));
            divider3.setBackgroundColor(sharedPrefs.getInt("cp_dividerColor", context.getResources().getColor(R.color.card_conversation_divider)));

            close.setColorFilter(sharedPrefs.getInt("cp_buttonColor", context.getResources().getColor(R.color.card_message_text_body)));

            name.setTextColor(sharedPrefs.getInt("cp_nameTextColor", context.getResources().getColor(R.color.card_conversation_name)));
            number.setTextColor(sharedPrefs.getInt("cp_numberTextColor", context.getResources().getColor(R.color.card_conversation_summary)));
            date.setTextColor(sharedPrefs.getInt("cp_dateTextColor", context.getResources().getColor(R.color.card_message_text_date_2)));
            body.setTextColor(sharedPrefs.getInt("cp_messageTextColor", context.getResources().getColor(R.color.card_message_text_body)));

            if (sharedPrefs.getBoolean("custom_font", false)) {
                body.setTypeface(font);
                date.setTypeface(font);
                name.setTypeface(font);
                number.setTypeface(font);
            }

            if (sharedPrefs.getString("text_alignment2", "center").equals("right")) {
                body.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            } else if (sharedPrefs.getString("text_alignment2", "center").equals("left")) {
                body.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            } else {
                body.setGravity(Gravity.CENTER);
            }

            try {
                body.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
                date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)) - 4);
            } catch (Exception e) {
                body.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)));
                date.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)) - 4);
            }

            name.setTextSize((float) Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")));
            number.setTextSize((float) Integer.parseInt(sharedPrefs.getString("text_size2", 14 + "")) - 2);

            String contactName = ContactUtil.findContactName(inboxNumber.get(position), context);
            name.setText(contactName);

            Locale sCachedLocale = Locale.getDefault();
            int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
            Editable editable = new SpannableStringBuilder(inboxNumber.get(position));
            PhoneNumberUtils.formatNumber(editable, sFormatType);
            final String contactNumber = editable.toString();

            if (!contactNumber.equals(contactName)) {
                number.setText(contactNumber);
            } else {
                number.setText("");
            }

            body.setText(inboxBody.get(position));

            if (sharedPrefs.getString("smilies", "with").equals("with")) {
                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(inboxBody.get(position));

                if (matcher.find()) {
                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter2.getSmiledText(context, inboxBody.get(position))));
                        }
                    } else {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2New.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverter2.getSmiledText(context, inboxBody.get(position))));
                        }
                    }
                } else {
                    if (sharedPrefs.getBoolean("smiliesType", true)) {
                        body.setText(EmoticonConverter2New.getSmiledText(context, inboxBody.get(position)));
                    } else {
                        body.setText(EmoticonConverter2.getSmiledText(context, inboxBody.get(position)));
                    }
                }
            } else if (sharedPrefs.getString("smilies", "with").equals("without")) {
                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(inboxBody.get(position));

                if (matcher.find()) {
                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter.getSmiledText(context, inboxBody.get(position))));
                        }
                    } else {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverterNew.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverter.getSmiledText(context, inboxBody.get(position))));
                        }
                    }
                } else {
                    if (sharedPrefs.getBoolean("smiliesType", true)) {
                        body.setText(EmoticonConverterNew.getSmiledText(context, inboxBody.get(position)));
                    } else {
                        body.setText(EmoticonConverter.getSmiledText(context, inboxBody.get(position)));
                    }
                }
            } else if (sharedPrefs.getString("smilies", "with").equals("none")) {
                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(inboxBody.get(position));

                if (matcher.find()) {
                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        body.setText(EmojiConverter2.getSmiledText(context, inboxBody.get(position)));
                    } else {
                        body.setText(EmojiConverter.getSmiledText(context, inboxBody.get(position)));
                    }
                } else {
                    body.setText(inboxBody.get(position));
                }
            } else if (sharedPrefs.getString("smilies", "with").equals("both")) {
                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(inboxBody.get(position));

                if (matcher.find()) {
                    if (sharedPrefs.getBoolean("emoji_type", true)) {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter2.getSmiledText(context, EmoticonConverter3.getSmiledText(context, inboxBody.get(position))));
                        }
                    } else {
                        if (sharedPrefs.getBoolean("smiliesType", true)) {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3New.getSmiledText(context, inboxBody.get(position))));
                        } else {
                            body.setText(EmojiConverter.getSmiledText(context, EmoticonConverter3.getSmiledText(context, inboxBody.get(position))));
                        }
                    }
                } else {
                    if (sharedPrefs.getBoolean("smiliesType", true)) {
                        body.setText(EmoticonConverter3New.getSmiledText(context, inboxBody.get(position)));
                    } else {
                        body.setText(EmoticonConverter3.getSmiledText(context, inboxBody.get(position)));
                    }
                }
            }

            Date date2;

            try {
                date2 = new Date(Long.parseLong(inboxDate.get(position)));
            } catch (Exception e) {
                date2 = new Date(0);
            }

            Calendar cal = Calendar.getInstance();
            Date currentDate = new Date(cal.getTimeInMillis());

            if (getZeroTimeDate(date2).equals(getZeroTimeDate(currentDate))) {
                if (sharedPrefs.getBoolean("hour_format", false)) {
                    date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
                } else {
                    date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
                }
            } else {
                if (sharedPrefs.getBoolean("hour_format", false)) {
                    date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
                } else {
                    date.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2) + ", " + DateFormat.getDateInstance(DateFormat.MEDIUM).format(date2));
                }
            }

            photo.assignContactFromPhone(inboxNumber.get(position), true);
            photo.setImageBitmap(ContactUtil.getFacebookPhoto(inboxNumber.get(position), context));

            final Context context = getActivity();

            close.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    NotificationManager mNotificationManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(1);

                    Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                    context.sendBroadcast(intent);

                    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                    PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pStopRepeating);

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            readSMS(context);
                        }
                    }).start();

                    getActivity().finish();

                }

            });

            return message;
        }

        public void readSMS(Context context) {
            try {
                Uri uriSms = Uri.parse("content://sms/inbox");
                Cursor c = context.getContentResolver().query(uriSms,
                        new String[]{"_id", "thread_id", "address",
                                "person", "date", "body", "read"}, null, null, "date DESC LIMIT 10");

                if (c != null && c.moveToFirst()) {
                    do {
                        String id = c.getString(0);

                        ContentValues values = new ContentValues();
                        values.put("read", true);
                        context.getContentResolver().update(Uri.parse("content://sms/inbox"), values, "_id=" + id, null);
                    } while (c.moveToNext());
                }
                c.close();
            } catch (Exception e) {

            }
        }
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
}