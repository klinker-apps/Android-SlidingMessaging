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
    public TextPaint messageReceivedPaint;
    public TextPaint messageSentPaint;
    public Paint namePaint;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    private int TEXTGAP = toDP(6);

    public MessageView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_GAP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 2, context.getResources().getDisplayMetrics());
        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, context.getResources().getDisplayMetrics());
        TEXT_SIZE_BIG = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, context.getResources().getDisplayMetrics());

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha((int) (sharedPrefs.getInt("quick_peek_transparency", 100) * 2.5));

        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);

        messageReceivedPaint = new TextPaint();
        messageReceivedPaint.setAntiAlias(true);
        messageReceivedPaint.setColor(Color.WHITE);
        messageReceivedPaint.setAlpha(SlideOverService.TOUCHED_ALPHA - 50);
        messageReceivedPaint.setTextSize(TEXT_SIZE);
        messageReceivedPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));

        messageSentPaint = new TextPaint(messageReceivedPaint);
        messageSentPaint.setColor(Color.GRAY);
        messageSentPaint.setAlpha(SlideOverService.TOUCHED_ALPHA);

        namePaint = new Paint(messageReceivedPaint);
        namePaint.setAlpha(SlideOverService.TOUCHED_ALPHA);
        namePaint.setTextSize(TEXT_SIZE_BIG);
        namePaint.setTypeface(Typeface.create("sans-serif-light", Typeface.BOLD));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        // draws the rectangle and the outline
        canvas.drawRect(0, 0, width - 100, toDP(160) + toDP(56), blackPaint);
        canvas.drawRect(0, 0, width - 100, toDP(160) + toDP(56), strokePaint);

        canvas.drawText(ContactView.contactNames[ContactView.currentContact], toDP(10), toDP(23), namePaint);

        int amountDown = toDP(30) + TEXTGAP;

        StaticLayout mTextLayout[] = new StaticLayout[3];

        for (int i = 0; i < 3; i++){

            String message = ContactView.message[ContactView.currentContact][2-i];
            boolean received = ContactView.type[ContactView.currentContact][2-i] == 0;

            mTextLayout[i] = new StaticLayout(message,
                    received ? messageReceivedPaint : messageSentPaint,
                    canvas.getWidth() - toDP(64),
                    received ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                    1.0f,
                    0.0f,
                    false);

            if (mTextLayout[i].getLineCount() > 1) {
                int start = mTextLayout[i].getLineStart(2);

                if (mTextLayout[i].getLineCount() > 2) {
                    message = message.substring(0, start - 4) + "...";
                    //amountDown -= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, mContext.getResources().getDisplayMetrics());
                } else {
                    message = message.substring(0, start);
                }

                mTextLayout[i] = new StaticLayout(message,
                        received ? messageReceivedPaint : messageSentPaint,
                        canvas.getWidth() - toDP(64),
                        received ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                        1.0f,
                        0.0f,
                        false);
            }

            canvas.save();
            canvas.translate(toDP(32), amountDown);
            mTextLayout[i].draw(canvas);
            canvas.restore();

            int originalDown = amountDown;
            amountDown += toDP((18 * mTextLayout[i].getLineCount()) + TEXTGAP);

            if (!message.equals("") && sharedPrefs.getBoolean("quick_peek_text_markers", true)) {
                canvas.drawLine(received ? toDP(22) : canvas.getWidth() - toDP(22),
                                    originalDown,
                                    received ? toDP(22) : canvas.getWidth() - toDP(22),
                                    amountDown - TEXTGAP - toDP(3),
                                    received ? messageReceivedPaint : messageSentPaint);
            }


        }
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, getResources().getDisplayMetrics());
    }
}
