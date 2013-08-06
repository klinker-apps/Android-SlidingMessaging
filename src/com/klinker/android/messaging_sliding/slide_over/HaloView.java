package com.klinker.android.messaging_sliding.slide_over;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by luke on 8/2/13.
 */
public class HaloView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;
    public Paint arcPaint;
    public float radius;

    public boolean isTouched = false;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public HaloView(Context context, Bitmap halo) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.WHITE);
        arcPaint.setAlpha(60);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        this.halo = halo;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        canvas.drawBitmap(halo, 0, 0, null);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
