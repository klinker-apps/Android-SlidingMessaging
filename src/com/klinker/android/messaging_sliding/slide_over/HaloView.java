package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;

import com.klinker.android.messaging_donate.R;

public class HaloView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;
    public Paint arcPaint;
    public float radius;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public HaloView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.WHITE);
        arcPaint.setAlpha(60);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        setRegularHalo();
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

    public void setRegularHalo()
    {
        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);
    }

    public void setRecievedHalo()
    {
        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_new);
    }
}
