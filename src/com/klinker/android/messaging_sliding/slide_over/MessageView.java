package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;

public class MessageView extends ViewGroup {
    public Context mContext;

    public static float TEXT_SIZE;
    public static float TEXT_SIZE_BIG;
    public static float TEXT_GAP;

    public Bitmap halo;

    public Paint blackPaint;
    public Paint strokePaint;
    public TextPaint messagePaint;
    public Paint namePaint;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public MessageView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_GAP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, context.getResources().getDisplayMetrics());
        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());
        TEXT_SIZE_BIG = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, context.getResources().getDisplayMetrics());

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha(200);

        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        messagePaint = new TextPaint();
        messagePaint.setAntiAlias(true);
        messagePaint.setColor(Color.WHITE);
        messagePaint.setAlpha(SlideOverService.TOUCHED_ALPHA - 50);
        messagePaint.setTextSize(TEXT_SIZE);
        messagePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        namePaint = new Paint(messagePaint);
        namePaint.setAlpha(SlideOverService.TOUCHED_ALPHA - 70);
        namePaint.setTextSize(TEXT_SIZE_BIG);
        namePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        // draws the rectangle and the outline
        canvas.drawRect(0, 0, width - 100, 200, blackPaint);
        canvas.drawRect(0, 0, width - 100, 200, strokePaint);

        canvas.drawText(ContactView.contactNames[ContactView.currentContact], 10, 40, namePaint);

        String message = ContactView.message[ContactView.currentContact];

        StaticLayout mTextLayout = new StaticLayout(message, messagePaint, canvas.getWidth() - 40, Layout.Alignment.ALIGN_NORMAL, .85f, 0.0f, false);

        if (mTextLayout.getLineCount() > 4) {
            int start = mTextLayout.getLineStart(5);

            message = message.substring(0, start);

            mTextLayout = new StaticLayout(message, messagePaint, canvas.getWidth() - 40, Layout.Alignment.ALIGN_NORMAL, .85f, 0.0f, false);
        }

        //canvas.drawText(message, 20, 80, messagePaint);

        canvas.save();
        canvas.translate(20, 56);
        mTextLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
