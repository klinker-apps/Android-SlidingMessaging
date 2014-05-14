package com.klinker.android.messaging_sliding.quick_reply;

import android.annotation.SuppressLint;
import android.app.*;
import android.app.KeyguardManager.KeyguardLock;
import android.content.*;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
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
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.send_message.Transaction;
import com.klinker.android.send_message.Utils;

import java.io.InputStream;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("deprecation")
public class QuickReply extends Activity {

    public EditText messageEntry;

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.setFinishOnTouchOutside(false);

        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!sharedPrefs.getBoolean("show_keyboard_popup", true)) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        if (sharedPrefs.getBoolean("unlock_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        }

        if (sharedPrefs.getBoolean("dark_theme_quick_reply", true)) {
            setTheme(android.R.style.Theme_Holo_Dialog);
        } else {
            setTheme(android.R.style.Theme_Holo_Light_Dialog);
        }

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.popup);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND,
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        QuickContactBadge contactBadge = (QuickContactBadge) findViewById(R.id.popupBadge);
        TextView contactName = (TextView) findViewById(R.id.popupName);
        TextView contactDate = (TextView) findViewById(R.id.popupDate);
        TextView contactBody = (TextView) findViewById(R.id.popupBody);
        messageEntry = (EditText) findViewById(R.id.popupEntry);
        final TextView charsRemaining = (TextView) findViewById(R.id.popupChars);
        ImageButton sendButton = (ImageButton) findViewById(R.id.popupSend);
        Button viewConversation = (Button) findViewById(R.id.viewConversation);
        ImageButton readButton = (ImageButton) findViewById(R.id.readButton);
        ImageButton deleteButton = (ImageButton) findViewById(R.id.deleteButton);

        viewConversation.setText("View Conversation");

        if (sharedPrefs.getBoolean("dark_theme_quick_reply", true)) {
            sendButton.setBackgroundResource(R.drawable.dark_send_button_popup);
            sendButton.setImageResource(R.drawable.ic_action_send_white);
            readButton.setImageResource(R.drawable.ic_menu_done_holo_dark);
            readButton.setBackgroundResource(R.drawable.dark_send_button_popup);
            deleteButton.setImageResource(R.drawable.ic_menu_delete);
            deleteButton.setBackgroundResource(R.drawable.dark_send_button_popup);
            charsRemaining.setTextColor(getResources().getColor(R.color.dark_grey));
            contactName.setTextColor(this.getResources().getColor(R.color.dark_grey));
            contactDate.setTextColor(this.getResources().getColor(R.color.dark_grey));
            contactBody.setTextColor(this.getResources().getColor(R.color.dark_grey));
        } else {
            sendButton.setBackgroundResource(R.drawable.light_send_button_popup);
            sendButton.setImageResource(R.drawable.ic_action_send_black);
            readButton.setImageResource(R.drawable.ic_menu_done_holo_light);
            readButton.setBackgroundResource(R.drawable.light_send_button_popup);
            deleteButton.setImageResource(R.drawable.ic_menu_delete_light);
            deleteButton.setBackgroundResource(R.drawable.light_send_button_popup);
            charsRemaining.setTextColor(getResources().getColor(R.color.light_grey));
            contactName.setTextColor(this.getResources().getColor(R.color.light_grey));
            contactDate.setTextColor(this.getResources().getColor(R.color.light_grey));
            contactBody.setTextColor(this.getResources().getColor(R.color.light_grey));
        }

        if (sharedPrefs.getBoolean("custom_font", false)) {
            Typeface font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));

            contactName.setTypeface(font);
            contactDate.setTypeface(font);
            contactBody.setTypeface(font);
            messageEntry.setTypeface(font);
            charsRemaining.setTypeface(font);
        }

        if (sharedPrefs.getString("text_alignment2", "center").equals("right")) {
            contactBody.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else if (sharedPrefs.getString("text_alignment2", "center").equals("left")) {
            contactBody.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        } else {
            contactBody.setGravity(Gravity.CENTER);
        }

        if (!sharedPrefs.getBoolean("keyboard_type", true)) {
            messageEntry.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
            messageEntry.setImeOptions(EditorInfo.IME_ACTION_NONE);
        }

        String body = "";
        String number = "";
        String date = "";
        String name = "";
        String id = "";

        try {
            Bundle b = this.getIntent().getBundleExtra("bundle");

            if (b != null) {
                body = b.getString("body", "placeholder");
                number = b.getString("address", "placeholder");
                date = b.getString("date", "0");

                try {
                    if (b.getString("notification").equals("true")) {
                        KeyguardManager keyguardManager = (KeyguardManager) this.getSystemService(Context.KEYGUARD_SERVICE);
                        KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
                        keyguardLock.disableKeyguard();
                    }
                } catch (Exception e) {

                }
            } else {
                Uri uri = Uri.parse("content://sms/inbox");
                Cursor c = getContentResolver().query(uri, null, null, null, null);

                try {
                    if (c.moveToFirst()) {
                        body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                        number = c.getString(c.getColumnIndexOrThrow("address")).toString().replaceAll("[^0-9\\+]", "");
                        date = c.getString(c.getColumnIndexOrThrow("date")).toString();

                        if (number.length() == 11)
                            number = number.substring(1, 11);
                    }
                } finally {
                    c.close();
                }
            }
        } catch (Exception e) {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor c = getContentResolver().query(uri, null, null, null, null);

            try {
                if (c.moveToFirst()) {
                    body = c.getString(c.getColumnIndexOrThrow("body")).toString();
                    number = c.getString(c.getColumnIndexOrThrow("address")).toString().replaceAll("[^0-9\\+]", "");
                    date = c.getString(c.getColumnIndexOrThrow("date")).toString();

                    if (number.length() == 11)
                        number = number.substring(1, 11);
                }
            } finally {
                c.close();
            }
        }

        Date date2;

        try {
            date2 = new Date(Long.parseLong(date));
        } catch (Exception e) {
            Calendar cal = Calendar.getInstance();
            date2 = new Date(cal.getTimeInMillis());
        }

        if (sharedPrefs.getBoolean("hour_format", false)) {
            contactDate.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.GERMAN).format(date2));
        } else {
            contactDate.setText(DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US).format(date2));
        }

        if (sharedPrefs.getString("smilies", "with").equals("with")) {
            String patternStr = "[^\\x20-\\x7E]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
            } else {
                contactBody.setText(EmoticonConverter2.getSmiledText(this, body));
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("without")) {
            String patternStr = "[^\\x20-\\x7E]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter.getSmiledText(this, body)));
            } else {
                contactBody.setText(EmoticonConverter.getSmiledText(this, body));
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("none")) {
            String patternStr = "[^\\x20-\\x7E]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                if (sharedPrefs.getBoolean("emoji_type", true)) {
                    contactBody.setText(EmojiConverter2.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
                } else {
                    contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
                }
            } else {
                contactBody.setText(body);
            }
        } else if (sharedPrefs.getString("smilies", "with").equals("both")) {
            String patternStr = "[^\\x20-\\x7E]";
            Pattern pattern = Pattern.compile(patternStr);
            Matcher matcher = pattern.matcher(body);

            if (matcher.find()) {
                if (sharedPrefs.getBoolean("emoji_type", true)) {
                    contactBody.setText(EmojiConverter2.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
                } else {
                    contactBody.setText(EmojiConverter.getSmiledText(this, EmoticonConverter2.getSmiledText(this, body)));
                }
            } else {
                contactBody.setText(EmoticonConverter3.getSmiledText(this, body));
            }
        }

        try {
            contactBody.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
        } catch (Exception e) {
            contactBody.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)));
        }

        try {
            Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
            Cursor phonesCursor = this.getContentResolver().query(phoneUri, new String[]{ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID}, null, null, null);

            if (phonesCursor != null && phonesCursor.moveToFirst()) {
                name = phonesCursor.getString(0);
                id = phonesCursor.getString(1);
            } else {
                Locale sCachedLocale = Locale.getDefault();
                int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
                Editable editable = new SpannableStringBuilder(number);
                PhoneNumberUtils.formatNumber(editable, sFormatType);
                name = editable.toString();

                id = "0";
            }

            phonesCursor.close();
        } catch (IllegalArgumentException e) {
            name = number;
            id = "0";
        } catch (NullPointerException e) {
            name = number;
            id = "0";
        }

        contactName.setText(name);

        InputStream input = ContactUtil.openDisplayPhoto(Long.parseLong(id), this);

        if (input == null) {
            if (sharedPrefs.getBoolean("ct_darkContctImage", false)) {
                input = this.getResources().openRawResource(R.drawable.default_avatar_dark);
            } else {
                input = this.getResources().openRawResource(R.drawable.default_avatar);
            }
        }

        Bitmap contactImage = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), 120, 120, true);

        contactBadge.assignContactFromPhone(number, false);
        contactBadge.setMode(ContactsContract.QuickContact.MODE_LARGE);
        contactBadge.setImageBitmap(contactImage);
        final Context context = this;
        final String sendTo = number;

        sendButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (messageEntry.getText().toString().equals("")) {
                    Toast.makeText(context, "ERROR: Nothing to send", Toast.LENGTH_SHORT).show();
                } else {
                    SendUtil.sendMessage(context, sendTo, messageEntry.getText().toString());

                    if (sharedPrefs.getBoolean("cache_conversations", false)) {
                        Intent cacheService = new Intent(context, CacheService.class);
                        context.startService(cacheService);
                    }

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

                    NotificationManager mNotificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(1);

                    IOUtil.writeNotifications(new ArrayList<String>(), context);

                    Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                    context.sendBroadcast(intent);

                    Toast.makeText(context, "Sending Message...", Toast.LENGTH_SHORT).show();

                    Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                    PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                    AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                    alarm.cancel(pStopRepeating);

                    Intent floatingNotifications = new Intent();
                    floatingNotifications.setAction("robj.floating.notifications.dismiss");
                    floatingNotifications.putExtra("package", getPackageName());
                    sendBroadcast(floatingNotifications);

                    Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                    context.sendBroadcast(updateWidget);

                    MainActivity.messageRecieved = true;
                }

            }

        });

        readButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mrIntent = new Intent();
                mrIntent.setClass(context, QmMarkRead.class);
                startService(mrIntent);

                ((Activity) context).finish();

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);

                IOUtil.writeNotifications(new ArrayList<String>(), context);

                Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                context.sendBroadcast(intent);

                Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pStopRepeating);

                Intent floatingNotifications = new Intent();
                floatingNotifications.setAction("robj.floating.notifications.dismiss");
                floatingNotifications.putExtra("package", getPackageName());
                sendBroadcast(floatingNotifications);

                Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                context.sendBroadcast(updateWidget);

                MainActivity.messageRecieved = true;

            }

        });

        deleteButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent deleteIntent = new Intent();
                deleteIntent.setClass(context, QmDelete.class);
                startService(deleteIntent);

                ((Activity) context).finish();

                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);

                IOUtil.writeNotifications(new ArrayList<String>(), context);

                Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                context.sendBroadcast(intent);

                Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pStopRepeating);

                Intent floatingNotifications = new Intent();
                floatingNotifications.setAction("robj.floating.notifications.dismiss");
                floatingNotifications.putExtra("package", getPackageName());
                sendBroadcast(floatingNotifications);

                Intent updateWidget = new Intent("com.klinker.android.messaging.UPDATE_WIDGET");
                context.sendBroadcast(updateWidget);

                MainActivity.messageRecieved = true;

            }

        });

        try {
            messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 2)));
        } catch (Exception e) {
            messageEntry.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0, 1)));
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT && sharedPrefs.getBoolean("show_keyboard_popup", true)) {
            messageEntry.postDelayed(new Runnable() {
                @Override
                public void run() {
                    InputMethodManager keyboard = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(messageEntry, 0);
                }
            }, 200);
        }

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
            }

            public void afterTextChanged(Editable s) {
            }
        });

        ImageButton emojiButton = (ImageButton) findViewById(R.id.display_emoji2);

        if (!sharedPrefs.getBoolean("emoji", false)) {
            emojiButton.setVisibility(View.GONE);
            LayoutParams params = (RelativeLayout.LayoutParams) messageEntry.getLayoutParams();
            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            messageEntry.setLayoutParams(params);
        } else {
            if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
                emojiButton.setImageResource(R.drawable.ic_emoji_dark);
                emojiButton.setColorFilter(context.getResources().getColor(R.color.holo_green));
            }

            emojiButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
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

        final String number2 = number;

        if (sharedPrefs.getBoolean("enable_view_conversation", false)) {
            viewConversation.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View arg0) {

                    Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number2));
                    intent.setClass(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setAction(Intent.ACTION_SENDTO);
                    intent.putExtra("com.klinker.android.OPEN", number2);
                    startActivity(intent);
                }

            });
        } else {
            viewConversation.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
