package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;

public class  ContactView extends ViewGroup {
    public Context mContext;

    public Bitmap cancel;

    public Paint strokePaint;
    public Paint blackPaint;
    public int haloAlpha = 255;
    public int haloNewAlpha = 0;

    public int haloColor;
    public int haloUnreadColor;

    public boolean animating = false;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public ContactView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        cancel = BitmapFactory.decodeResource(getResources(),
                R.drawable.ic_cancel);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha(200);


        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        haloColor = sharedPrefs.getInt("slideover_color", context.getResources().getColor(R.color.white));
        haloUnreadColor = sharedPrefs.getInt("slideover_unread_color", context.getResources().getColor(R.color.holo_red));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        canvas.drawRect(0, 0, 100, 100, blackPaint);
        canvas.drawRect(105, 0, 205, 100, blackPaint);
        canvas.drawRect(210, 0, 310, 100, blackPaint);
        canvas.drawRect(315, 0, 415, 100, blackPaint);
        canvas.drawRect(420, 0, 520, 100, blackPaint);

        canvas.drawRect(0, 0, 100, 100, strokePaint);
        canvas.drawRect(105, 0, 205, 100, strokePaint);
        canvas.drawRect(210, 0, 310, 100, strokePaint);
        canvas.drawRect(315, 0, 415, 100, strokePaint);
        canvas.drawRect(420, 0, 520, 100, strokePaint);

        //Rect cancelRect = new Rect(width - 100 - 100, 0, width - 100, 100);
        //canvas.drawBitmap(cancel, null, cancelRect, strokePaint);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
