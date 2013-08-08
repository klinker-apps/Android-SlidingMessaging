package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

/**
 * Created by luke on 8/2/13.
 */
public class UnreadView extends ViewGroup {
    public Context mContext;

    public static float TEXT_SIZE;

    public Bitmap unreadBubble;

    public Paint textPaint = new Paint();

    public int xCoor;
    public int yCoor;

    public String unreadCount;


    public UnreadView(Context context, int vertical, int horizontal, String unreadCount) {
        super(context);

        mContext = context;

        xCoor = vertical;
        yCoor = horizontal;

        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());

        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setAlpha(SlideOverService.START_ALPHA2);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        unreadBubble = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        this.unreadCount = unreadCount;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawBitmap(unreadBubble, xCoor, yCoor, null);
        canvas.drawText(unreadCount, xCoor, yCoor, textPaint);
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

}
