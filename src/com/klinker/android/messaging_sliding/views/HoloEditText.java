package com.klinker.android.messaging_sliding.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.klinker.android.messaging_sliding.emojis.*;

public class HoloEditText extends EditText {

    private static SharedPreferences sharedPreferences;

    public HoloEditText(Context context) {
        super(context);
        setUp(context);
    }

    public HoloEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setUp(context);
    }

    public HoloEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setUp(context);
    }

    private void setUp(Context context) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }

        setTypeface(context);
        addTextWatcher(context);
    }

    public static Typeface typeface;
    private static boolean useDeviceFont;

    private void setTypeface(Context context) {
        if (typeface == null) {
            useDeviceFont = sharedPreferences.getBoolean("device_font", false);
            typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }

        if (!useDeviceFont) {
            setTypeface(typeface);
        }
    }

    private static String smiliesFormat;
    private static boolean smiliesType;
    private static boolean emojiType;

    private void addTextWatcher(final Context context) {
        if (smiliesFormat == null) {
            smiliesFormat = sharedPreferences.getString("smilies", "with");
            smiliesType = sharedPreferences.getBoolean("smiliesType", true);
            emojiType = sharedPreferences.getBoolean("emoji_type", true);
        }

        addTextChangedListener(new TextWatcher() {
            private int LENGTH = 5;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (getVisibility() == View.GONE) {
                    return;
                }

                Spannable string = null;

                if (EmojiUtil.smileyPattern.matcher(charSequence.length() > LENGTH ? charSequence.subSequence(charSequence.length() - 5, charSequence.length()) : charSequence).find()) {
                    if (smiliesFormat.equals("with")) {
                        if (smiliesType) {
                            string = EmoticonConverter2New.getSmiledText(context, charSequence);
                        } else {
                            string = EmoticonConverter2.getSmiledText(context, charSequence);
                        }
                    } else if (smiliesFormat.equals("without")) {
                        if (smiliesType) {
                            string = EmoticonConverterNew.getSmiledText(context, charSequence);
                        } else {
                            string = EmoticonConverter.getSmiledText(context, charSequence);
                        }
                    } else if (smiliesFormat.equals("both")) {
                        if (smiliesType) {
                            string = EmoticonConverter3New.getSmiledText(context, charSequence);
                        } else {
                            string = EmoticonConverter3.getSmiledText(context, charSequence);
                        }
                    }
                }

                if (EmojiUtil.emojiPattern.matcher(charSequence.length() > LENGTH ? charSequence.subSequence(charSequence.length() - 5, charSequence.length()) : charSequence).find()) {
                    if (string == null) {
                        string = Spannable.Factory.getInstance().newSpannable(charSequence);
                    }

                    if (emojiType) {
                        string = EmojiConverter2.getSmiledText(context, string);
                    } else {
                        string = EmojiConverter.getSmiledText(context, string);
                    }
                }

                if (string != null) {
                    removeTextChangedListener(this);
                    setText(string);
                    setSelection(string.length());
                    addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
