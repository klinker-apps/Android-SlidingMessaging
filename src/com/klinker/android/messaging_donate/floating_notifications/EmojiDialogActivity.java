package com.klinker.android.messaging_donate.floating_notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter;
import com.klinker.android.messaging_sliding.emojis.EmojiAdapter2;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter;
import com.klinker.android.messaging_sliding.emojis.EmojiConverter2;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;
import robj.floating.notifications.Extension;

public class EmojiDialogActivity extends Activity {

    public EditText editText;
    public String message;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		final long id = getIntent().getLongExtra("id", -1);
		final Context context = this;

        message = getIntent().getStringExtra("message");

		setTitle("Insert Emojis");
        setContentView(R.layout.emoji_frame);

        editText = (EditText) findViewById(R.id.emoji_text);
        ImageButton peopleButton = (ImageButton) findViewById(R.id.peopleButton);
        ImageButton objectsButton = (ImageButton) findViewById(R.id.objectsButton);
        ImageButton natureButton = (ImageButton) findViewById(R.id.natureButton);
        ImageButton placesButton = (ImageButton) findViewById(R.id.placesButton);
        ImageButton symbolsButton = (ImageButton) findViewById(R.id.symbolsButton);

        final StickyGridHeadersGridView emojiGrid = (StickyGridHeadersGridView) findViewById(R.id.emojiGrid);
        Button okButton = (Button) findViewById(R.id.emoji_ok);

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

			LinearLayout buttons = (LinearLayout) findViewById(R.id.linearLayout);
			buttons.setMinimumHeight(0);
			buttons.setVisibility(View.GONE);
		}

		okButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				final String editTextHint = context.getResources().getString(R.string.reply_to) + " " + MainActivity.findContactName(FNReceiver.messages.get(id)[0], context);
                final String previousText = FNReceiver.messages.get(id)[1];
                final Bitmap image = MainActivity.getFacebookPhoto(FNReceiver.messages.get(id)[0], context);
                final Extension.onClickListener imageOnClick = new Extension.onClickListener() {
                    @Override
                    public void onClick() {
                            Intent intent = new Intent(context, com.klinker.android.messaging_sliding.MainActivity.class);
                            intent.putExtra("com.klinker.android.OPEN_THREAD", FNReceiver.messages.get(id)[0]);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(intent);
                            FNReceiver.messages.remove(id);
                            Extension.remove(id, context);
                            finish();
                    }
                };

                final Extension.onClickListener sendOnClick = new Extension.onClickListener() {
                    @Override
                    public void onClick(String str) {
                            Extension.remove(id, context);
                            FNReceiver.messages.remove(id);
                            FNAction.sendMessage(context, FNReceiver.messages.get(id)[0], str);
                            finish();
                    }
                };

                final Bitmap extraButton = BitmapFactory.decodeResource(context.getResources(), R.drawable.emo_im_smirk);

                Extension.onClickListener extraOnClick = new Extension.onClickListener() {
                    @Override
                    public void onClick(final String str) {
                            Intent emojiDialog = new Intent(context, EmojiDialogActivity.class);
                            emojiDialog.putExtra("id", id);
                            emojiDialog.putExtra("message", str);
                            message = str;
                            emojiDialog.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            context.startActivity(emojiDialog);
                    }
                };

                Extension.replyOverlay(editTextHint, previousText, image, imageOnClick, sendOnClick, extraOnClick, true, extraButton, context, false, editText.getText().toString());
                Extension.hideAll(id, context);
			}

		});
	}

    @Override
    public void onResume() {
        super.onResume();

        editText.setText(message);
        editText.setSelection(message.length());
    }
}