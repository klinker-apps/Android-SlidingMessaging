package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;

public class  ContactView extends ViewGroup {
    public Context mContext;

    public Paint strokePaint;
    public Paint blackPaint;
    public Paint contactCurrentPaint;
    public Paint contactClosedPaint;

    public SharedPreferences sharedPrefs;

    public static Resources resources;

    public int height;
    public int width;

    public static int currentContact = 0;

    public static Bitmap[] contactPics = new Bitmap[5];
    public static String[] contactNames = new String[5];
    public static String[] message = new String[5];

    public ContactView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha(200);

        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(2);

        contactCurrentPaint = new Paint();
        contactCurrentPaint.setColor(getResources().getColor(R.color.black));
        contactCurrentPaint.setAlpha(150);

        contactClosedPaint = new Paint();
        contactClosedPaint.setColor(getResources().getColor(R.color.black));
        contactClosedPaint.setAlpha(40);

        resources = getResources();

        refreshArrays();
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

        Rect contOneRect = new Rect(0, 0, 100, 100);
        canvas.drawBitmap(contactPics[0], null, contOneRect, currentContact == 0 ? contactCurrentPaint : contactClosedPaint);

        Rect contTwoRect = new Rect(105, 0, 205, 100);
        canvas.drawBitmap(contactPics[1], null, contTwoRect, currentContact == 1 ? contactCurrentPaint : contactClosedPaint);

        Rect contThreeRect = new Rect(210, 0, 310, 100);
        canvas.drawBitmap(contactPics[2], null, contThreeRect, currentContact == 2 ? contactCurrentPaint : contactClosedPaint);

        Rect contFourRect = new Rect(315, 0, 415, 100);
        canvas.drawBitmap(contactPics[3], null, contFourRect, currentContact == 3 ? contactCurrentPaint : contactClosedPaint);

        Rect contFiveRect = new Rect(420, 0, 520, 100);
        canvas.drawBitmap(contactPics[4], null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);

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

    public static void refreshArrays() {
        // need a contact pictures array, contact names, first message, second message
        // all should be 5 long for the 5 conversations

        contactNames[0] = "Jacob Klinker";
        contactNames[1] = "Luke Klinker";
        contactNames[2] = "Ben Madden";
        contactNames[3] = "Cody Chapman";
        contactNames[4] = "Jake Alleman";

        message[0] = "Contact 0. This is the first message that i am making with the new slideover. Contact 0";
        message[1] = "Contact 1. This is the first message that i am making with the new slideover. Contact 1";
        message[2] = "Contact 2. This is the first message that i am making with the new slideover. Contact 2";
        message[3] = "Contact 3. This is the first message that i am making with the new slideover. Contact 3";
        message[4] = "Contact 4. This is the first message that i am making with the new slideover. Contact 4";

        for (int i = 0; i < 5; i++)
            contactPics[i] = BitmapFactory.decodeResource(resources, R.drawable.ic_contact_picture);
    }
}
