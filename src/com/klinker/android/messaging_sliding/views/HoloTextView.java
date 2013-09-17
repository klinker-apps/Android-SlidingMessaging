package com.klinker.android.messaging_sliding.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.TextView;

public class HoloTextView extends TextView {
    public HoloTextView (Context context) {
        super(context);
        setTypeface(context);
    }

    public HoloTextView (Context context, AttributeSet attrs) {
        super(context, attrs);
        setTypeface(context);
    }

    public HoloTextView (Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setTypeface(context);
    }

    public static Typeface typeface;
    private static boolean useDeviceFont;
    private void setTypeface (Context context) {
        if (typeface == null) {
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            useDeviceFont = sharedPreferences.getBoolean("device_font", false);
            typeface = Typeface.createFromAsset(context.getAssets(), "Roboto-Light.ttf");
        }

        if (!useDeviceFont) {
            setTypeface(typeface);
        }
    }
}
