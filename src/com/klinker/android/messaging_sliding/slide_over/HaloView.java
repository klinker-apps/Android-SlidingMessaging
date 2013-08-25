package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;

public class HaloView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;
    public Bitmap haloNew;

    public Paint haloPaint;
    public Paint haloNewPaint;
    public int haloAlpha = 255;
    public int haloNewAlpha = 0;

    public boolean animating = false;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public HaloView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        haloNew = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_new);

        haloPaint = new Paint();
        haloPaint.setAlpha(haloAlpha);

        haloNewPaint = new Paint();
        haloNewPaint.setAlpha(haloNewAlpha);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        if (haloAlpha != 0) {
            haloPaint.setAlpha(haloAlpha);
            canvas.drawBitmap(halo, 0, 0, haloPaint);
        }

        if (haloNewAlpha != 0) {
            haloNewPaint.setAlpha(haloNewAlpha);
            canvas.drawBitmap(haloNew, 0, 0, haloNewPaint);
        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
