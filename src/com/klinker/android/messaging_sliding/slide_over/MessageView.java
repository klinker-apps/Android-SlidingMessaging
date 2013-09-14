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

public class MessageView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;

    public Paint haloPaint;
    public Paint haloNewPaint;
    public int haloAlpha = 255;
    public int haloNewAlpha = 0;

    public int haloColor;
    public int haloUnreadColor;

    public boolean animating = false;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public MessageView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        haloPaint = new Paint();
        haloPaint.setColor(getResources().getColor(R.color.black));
        haloPaint.setAlpha(40);

        haloNewPaint = new Paint();
        haloNewPaint.setAlpha(haloNewAlpha);

        haloColor = sharedPrefs.getInt("slideover_color", context.getResources().getColor(R.color.white));
        haloUnreadColor = sharedPrefs.getInt("slideover_unread_color", context.getResources().getColor(R.color.holo_red));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        canvas.drawRect(0, 0, width - 160, 200, haloPaint);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
