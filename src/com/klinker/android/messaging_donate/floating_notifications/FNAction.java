package com.klinker.android.messaging_donate.floating_notifications;

import android.app.*;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.StripAccents;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import robj.floating.notifications.Extension;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FNAction extends BroadcastReceiver {

    public  SharedPreferences sharedPrefs;

	@Override 
        public void onReceive(final Context context, Intent intent) {
		final long id = intent.getLongExtra(Extension.ID, -1);
		int action = intent.getIntExtra(Extension.ACTION, -1);
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (intent.getAction().equals(Extension.INTENT)) {
            switch (action) {

                case 0:
                    // start main activity popup
                    Intent popup = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);
                    context.startActivity(popup);
                    Extension.remove(id, context);
                    FNReceiver.messages.remove(id);
                    break;

                case 1:
                    // create new reply overlay
                    final String editTextHint = context.getResources().getString(R.string.reply_to) + " " + MainActivity.findContactName(id + "", context);
                    final String previousText = FNReceiver.messages.get(id);
                    final Bitmap image = MainActivity.getFacebookPhoto(id + "", context);
                    final Extension.onClickListener imageOnClick = new Extension.onClickListener() {
                        @Override
                        public void onClick() {
                            Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
                            intent.putExtra("com.klinker.android.OPEN_THREAD", id + "");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            FNReceiver.messages.remove(id);
                            Extension.remove(id, context);
                        }
                    };

                    final Extension.onClickListener sendOnClick = new Extension.onClickListener() {
                        @Override
                        public void onClick(String str) {
                            sendMessage(context, id + "", str);
                            Extension.remove(id, context);
                            FNReceiver.messages.remove(id);
                        }
                    };

                    final Bitmap extraButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.emo_im_smirk);

                    Extension.onClickListener extraOnClick = new Extension.onClickListener() {
                        @Override
                        public void onClick(final String str) {
                            // TODO turn dialog into new activity with dialog theme
                            AlertDialog.Builder builder = new AlertDialog.Builder(context);
                            builder.setTitle("Insert Emojis");
                            View frame = View.inflate(context, R.layout.emoji_frame, null);

                            final EditText editText = (EditText) frame.findViewById(R.id.emoji_text);
                            ImageButton peopleButton = (ImageButton) frame.findViewById(R.id.peopleButton);
                            ImageButton objectsButton = (ImageButton) frame.findViewById(R.id.objectsButton);
                            ImageButton natureButton = (ImageButton) frame.findViewById(R.id.natureButton);
                            ImageButton placesButton = (ImageButton) frame.findViewById(R.id.placesButton);
                            ImageButton symbolsButton = (ImageButton) frame.findViewById(R.id.symbolsButton);

                            final StickyGridHeadersGridView emojiGrid = (StickyGridHeadersGridView) frame.findViewById(R.id.emojiGrid);
                            Button okButton = (Button) frame.findViewById(R.id.emoji_ok);

                            if (sharedPrefs.getBoolean("emoji_type", true))
                            {
                                emojiGrid.setAdapter(new EmojiAdapter2(context));
                                emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                                    {
                                        editText.setText(EmojiConverter2.getSmiledText(context, editText.getText().toString() + EmojiAdapter2.mEmojiTexts[position]));
                                        editText.setSelection(editText.getText().length());
                                    }
                                });

                                peopleButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        emojiGrid.setSelection(0);
                                    }
                                });

                                objectsButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        emojiGrid.setSelection(153 + (2 * 7));
                                    }
                                });

                                natureButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        emojiGrid.setSelection(153 + 162 + (3 * 7));
                                    }
                                });

                                placesButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        emojiGrid.setSelection(153 + 162 + 178 + (5 * 7));
                                    }
                                });

                                symbolsButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        emojiGrid.setSelection(153 + 162 + 178 + 122 + (7 * 7));
                                    }
                                });
                            } else
                            {
                                emojiGrid.setAdapter(new EmojiAdapter(context));
                                emojiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                                    public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                                    {
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

                            final Extension.onClickListener extraOnClick2 = this;

                            okButton.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    Extension.replyOverlay(editTextHint, previousText, image, imageOnClick, sendOnClick, extraOnClick2, true, extraButton, context, false, "");

                                    dialog.dismiss();
                                }

                            });
                        }
                    };

                    Extension.replyOverlay(editTextHint, previousText, image, imageOnClick, sendOnClick, extraOnClick, true, extraButton, context, false, "");

                    break;

                case 2:
                    // start call intent
                    String address = id + "";
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + address));
                    callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(callIntent);
                    Extension.remove(id, context);
                    FNReceiver.messages.remove(id);
                    break;

                case 3:
                    // start mark read service
                    context.startService(new Intent(context, com.klinker.android.messaging_sliding.quick_reply.QmMarkRead2.class));
                    Extension.remove(id, context);
                    FNReceiver.messages.remove(id);
                    break;

            }
        }
	}

    public void sendMessage(final Context context, String number, String body)
    {
        if (sharedPrefs.getBoolean("delivery_reports", false))
        {
            String SENT = "SMS_SENT";
            String DELIVERED = "SMS_DELIVERED";

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(SENT), 0);

            PendingIntent deliveredPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(DELIVERED), 0);

            ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();
            ArrayList<PendingIntent> dPI = new ArrayList<PendingIntent>();

            String body2 = body;

            if (sharedPrefs.getBoolean("strip_unicode", false))
            {
                body2 = StripAccents.stripAccents(body2);
            }

            if (!sharedPrefs.getString("signature", "").equals(""))
            {
                body2 += "\n" + sharedPrefs.getString("signature", "");
            }

            SmsManager smsManager = SmsManager.getDefault();

            if (sharedPrefs.getBoolean("split_sms", false))
            {
                int length = 160;

                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(body2);

                boolean counter = false;

                if (sharedPrefs.getBoolean("split_counter", false)) {
                    counter = true;
                    length -= 7;
                }

                String[] textToSend = com.klinker.android.messaging_card.MainActivity.splitByLength(body2, length, counter);

                for (int i = 0; i < textToSend.length; i++)
                {
                    ArrayList<String> parts = smsManager.divideMessage(textToSend[i]);

                    for (int j = 0; j < parts.size(); j++)
                    {
                        sPI.add(sentPI);
                        dPI.add(deliveredPI);
                    }

                    smsManager.sendMultipartTextMessage(number, null, parts, sPI, dPI);
                }
            } else
            {
                ArrayList<String> parts = smsManager.divideMessage(body2);

                for (int i = 0; i < parts.size(); i++)
                {
                    sPI.add(sentPI);
                    dPI.add(deliveredPI);
                }

                smsManager.sendMultipartTextMessage(number, null, parts, sPI, dPI);
            }
        } else
        {
            String SENT = "SMS_SENT";

            PendingIntent sentPI = PendingIntent.getBroadcast(context, 0,
                    new Intent(SENT), 0);

            ArrayList<PendingIntent> sPI = new ArrayList<PendingIntent>();

            String body2 = body;

            if (sharedPrefs.getBoolean("strip_unicode", false))
            {
                body2 = StripAccents.stripAccents(body2);
            }

            if (!sharedPrefs.getString("signature", "").equals(""))
            {
                body2 += "\n" + sharedPrefs.getString("signature", "");
            }

            SmsManager smsManager = SmsManager.getDefault();

            if (sharedPrefs.getBoolean("split_sms", false))
            {
                int length = 160;

                String patternStr = "[^\\x20-\\x7E]";
                Pattern pattern = Pattern.compile(patternStr);
                Matcher matcher = pattern.matcher(body2);

                if (matcher.find())
                {
                    length = 70;
                }

                boolean counter = false;

                if (sharedPrefs.getBoolean("split_counter", false)) {
                    counter = true;
                    length -= 7;
                }

                String[] textToSend = com.klinker.android.messaging_card.MainActivity.splitByLength(body2, length, counter);

                for (int i = 0; i < textToSend.length; i++)
                {
                    ArrayList<String> parts = smsManager.divideMessage(textToSend[i]);

                    for (int j = 0; j < parts.size(); j++)
                    {
                        sPI.add(sentPI);
                    }

                    smsManager.sendMultipartTextMessage(number, null, parts, sPI, null);
                }
            } else
            {
                ArrayList<String> parts = smsManager.divideMessage(body2);

                for (int i = 0; i < parts.size(); i++)
                {
                    sPI.add(sentPI);
                }

                smsManager.sendMultipartTextMessage(number, null, parts, sPI, null);
            }
        }

        String address = number;
        String body2 = body;

        if (!sharedPrefs.getString("signature", "").equals(""))
        {
            body2 += "\n" + sharedPrefs.getString("signature", "");
        }

        Calendar cal = Calendar.getInstance();
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("body", StripAccents.stripAccents(body2));
        values.put("date", cal.getTimeInMillis() + "");
        values.put("read", 1);
        context.getContentResolver().insert(Uri.parse("content://sms/sent"), values);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead2.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", cal.getTimeInMillis() + "");
        mrIntent.putExtra("address", address);
        context.startService(mrIntent);

        com.klinker.android.messaging_sliding.MainActivity.messageRecieved = true;
    }
}
