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
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        // draws the rectangle and the outline
        canvas.drawRect(0, 0, width - 100, toDP(160), blackPaint);
        canvas.drawRect(0, 0, width - 100, toDP(160), strokePaint);

        canvas.drawText(ContactView.contactNames[ContactView.currentContact], 10, 40, namePaint);

        int amountDown = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 19, mContext.getResources().getDisplayMetrics());

        String message3 = ContactView.message[ContactView.currentContact][2];

        StaticLayout mTextLayout3 = new StaticLayout(message3,
                ContactView.type[ContactView.currentContact][2] == 0 ? messageReceivedPaint : messageSentPaint,
                canvas.getWidth() - 40,
                ContactView.type[ContactView.currentContact][2] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                1.0f,
                0.0f,
                false);

        if (mTextLayout3.getLineCount() > 0) {
            int start = mTextLayout3.getLineStart(1);

            if (mTextLayout3.getLineCount() > 1) {
                message3 = message3.substring(0, start - 4) + "...";
                //amountDown -= (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18, mContext.getResources().getDisplayMetrics());
            } else {
                message3 = message3.substring(0, start);
            }

            mTextLayout3 = new StaticLayout(message3,
                    ContactView.type[ContactView.currentContact][2] == 0 ? messageReceivedPaint : messageSentPaint,
                    canvas.getWidth() - 40,
                    ContactView.type[ContactView.currentContact][2] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                    1.0f,
                    0.0f,
                    false);
        }

        amountDown += (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18 * mTextLayout3.getLineCount() + 3, mContext.getResources().getDisplayMetrics());

        canvas.save();
        canvas.translate(20, amountDown);
        mTextLayout3.draw(canvas);
        canvas.restore();

        String message2 = ContactView.message[ContactView.currentContact][1];

        StaticLayout mTextLayout2 = new StaticLayout(message2,
                ContactView.type[ContactView.currentContact][1] == 0 ? messageReceivedPaint : messageSentPaint,
                canvas.getWidth() - 40,
                ContactView.type[ContactView.currentContact][1] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                1.0f,
                0.0f,
                false);

        if (mTextLayout2.getLineCount() > 1) {
            int start = mTextLayout2.getLineStart(2);

            if (mTextLayout2.getLineCount() > 2) {
                message2 = message2.substring(0, start - 4) + "...";
            } else {
                message2 = message2.substring(0, start);
            }

            mTextLayout2 = new StaticLayout(message2,
                    ContactView.type[ContactView.currentContact][1] == 0 ? messageReceivedPaint : messageSentPaint,
                    canvas.getWidth() - 40,
                    ContactView.type[ContactView.currentContact][1] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                    1.0f,
                    0.0f,
                    false);
        }

        amountDown += (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18 * mTextLayout3.getLineCount() + 3, mContext.getResources().getDisplayMetrics());

        canvas.save();
        canvas.translate(20, amountDown);
        mTextLayout2.draw(canvas);
        canvas.restore();

        String message1 = ContactView.message[ContactView.currentContact][0];

        StaticLayout mTextLayout1 = new StaticLayout(
                message1,
                ContactView.type[ContactView.currentContact][0] == 0 ? messageReceivedPaint : messageSentPaint,
                canvas.getWidth() - 40,
                ContactView.type[ContactView.currentContact][0] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                1.0f,
                0.0f,
                false);

        if (mTextLayout1.getLineCount() > 1) {
            int start = mTextLayout1.getLineStart(2);

            if (mTextLayout1.getLineCount() > 2) {
                message1 = message1.substring(0, start - 4) + "...";
            } else {
                message1 = message1.substring(0, start);
            }

            mTextLayout1 = new StaticLayout(message1,
                    ContactView.type[ContactView.currentContact][0] == 0 ? messageReceivedPaint : messageSentPaint,
                    canvas.getWidth() - 40,
                    ContactView.type[ContactView.currentContact][0] == 0 ? Layout.Alignment.ALIGN_NORMAL : Layout.Alignment.ALIGN_OPPOSITE,
                    1.0f,
                    0.0f,
                    false);
        }

        amountDown += (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 18 * mTextLayout2.getLineCount() + 3, mContext.getResources().getDisplayMetrics());

        if (!(mTextLayout1.getHeight() + mTextLayout2.getHeight() + mTextLayout3.getHeight() > 230)) {
            canvas.save();
            canvas.translate(20, amountDown);
            mTextLayout1.draw(canvas);
            canvas.restore();
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
