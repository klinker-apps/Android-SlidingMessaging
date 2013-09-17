package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.util.Arrays;

public class  ContactView extends ViewGroup {
    public static Context mContext;

    public Paint strokePaint;
    public Paint blackPaint;
    public Paint contactCurrentPaint;
    public Paint contactClosedPaint;

    public static SharedPreferences sharedPrefs;

    public static Resources resources;

    public int height;
    public int width;

    public static int currentContact = 0;

    public static Bitmap[] contactPics = new Bitmap[5];
    public static String[] contactNames = new String[5];
    public static String[][] message = new String[5][3];
    public static int[][] type = new int[5][3];
    public static boolean[] ignore = new boolean[5];

    public ContactView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        blackPaint = new Paint();
        blackPaint.setColor(getResources().getColor(R.color.black));
        blackPaint.setAlpha(75);

        strokePaint = new Paint(blackPaint);
        strokePaint.setColor(getResources().getColor(R.color.white));
        strokePaint.setAlpha(50);
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);

        contactCurrentPaint = new Paint();
        contactCurrentPaint.setColor(getResources().getColor(R.color.black));
        contactCurrentPaint.setAlpha(215);

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

        if (!ignore[0]) {
            Rect contOneRect = new Rect(0, 0, 100, 100);
            canvas.drawRect(0, 0, 100, 100, blackPaint);
            if (currentContact != 0)
                canvas.drawRect(0, 0, 100, 100, strokePaint);
            canvas.drawBitmap(contactPics[0], null, contOneRect, currentContact == 0 ? contactCurrentPaint : contactClosedPaint);
        }

        if (!ignore[1]) {
            Rect contTwoRect = new Rect(105, 0, 205, 100);
            canvas.drawRect(105, 0, 205, 100, blackPaint);
            if (currentContact != 1)
                canvas.drawRect(105, 0, 205, 100, strokePaint);
            canvas.drawBitmap(contactPics[1], null, contTwoRect, currentContact == 1 ? contactCurrentPaint : contactClosedPaint);
        }

        if (!ignore[2]) {
            Rect contThreeRect = new Rect(210, 0, 310, 100);
            canvas.drawRect(210, 0, 310, 100, blackPaint);
            if (currentContact != 2)
                canvas.drawRect(210, 0, 310, 100, strokePaint);
            canvas.drawBitmap(contactPics[2], null, contThreeRect, currentContact == 2 ? contactCurrentPaint : contactClosedPaint);
        }

        if (!ignore[3]) {
            Rect contFourRect = new Rect(315, 0, 415, 100);
            canvas.drawRect(315, 0, 415, 100, blackPaint);
            if (currentContact != 3)
                canvas.drawRect(315, 0, 415, 100, strokePaint);
            canvas.drawBitmap(contactPics[3], null, contFourRect, currentContact == 3 ? contactCurrentPaint : contactClosedPaint);
        }

        if (!ignore[4]) {
            Rect contFiveRect = new Rect(420, 0, 520, 100);
            canvas.drawRect(420, 0, 520, 100, blackPaint);
            if (currentContact != 4)
                canvas.drawRect(420, 0, 520, 100, strokePaint);
            canvas.drawBitmap(contactPics[4], null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);
        }

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

        /* This is the test data that i was using
        contactNames[0] = "Jacob Klinker";
        contactNames[1] = "Luke Klinker";
        contactNames[2] = "Ben Madden";
        contactNames[3] = "Cody Chapman";
        contactNames[4] = "Jake Alleman";

        message[0] = "Contact 0. This is the first message that i am making with the new slideover. Contact 0" + "\n\n" + "this is the 2nd half of the message. It keeps going for this conversation to test if the thing is big enough for 5 lines or not. this is a continuation of the testing now.";
        message[1] = "Contact 1. This is the first message that i \n\nContact 1" + "\n\n" + "this is the 2nd half of the message";
        message[2] = "Contact 2. This is the first message that i\n\nContact 2" + "\n\n" + "this is the 2nd half of the message \ntesting";
        message[3] = "Contact 3. This is the first message that i am making with the new slideover. Contact 3" + "\n\n" + "this is the 2nd half of the message";
        message[4] = "Contact 4. This is the first message that i am making with the new slideover. Contact 4" + "\n\n" + "this is the 2nd half of the message";

        for (int i = 0; i < 5; i++)
            contactPics[i] = BitmapFactory.decodeResource(resources, R.drawable.ic_contact_picture);
        */

        // initializing data isn't necessary
        for (int i = 0; i < 5; i++) {
            contactNames[i] = "";
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j< 3; j++) {
                message[i][j] = "";
            }
        }

        for (int i = 0; i < 5; i++) {
            for (int j = 0; j< 3; j++) {
                type[i][j] = 0;
            }
        }

        Arrays.fill(ignore, Boolean.FALSE);

        /*for (int i = 0; i < 5; i++)
            contactPics[i] = BitmapFactory.decodeResource(resources, R.drawable.ic_contact_picture);*/

        Uri SMS_CONTENT_URI = Uri.parse("content://mms-sms/conversations/?simple=true");
        Cursor cursor;

        if (sharedPrefs.getBoolean("slideover_only_unread", false)) {
            cursor = mContext.getContentResolver().query( SMS_CONTENT_URI, new String[]{"_id", "recipient_ids"}, "read=?", new String[] {"0"}, "date desc");
        } else {
            cursor = mContext.getContentResolver().query( SMS_CONTENT_URI, new String[]{"_id", "recipient_ids"}, null, null, "date desc");
        }

        if (cursor.moveToFirst()) {
            int count = 0;
            do {
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String number = MainActivity.findContactNumber(cursor.getString(cursor.getColumnIndex("recipient_ids")), mContext);
                String name = MainActivity.findContactName(number, mContext);

                Cursor cursor2;

                cursor2 = mContext.getContentResolver().query( Uri.parse("content://sms/"), new String[]{"body", "type", "thread_id"}, "thread_id=?", new String[] {id}, "date desc");

                //Cursor cursor2 = mContext.getContentResolver().query( Uri.parse("content://mms-sms/conversations/"), new String[]{"body", "address", "thread_id", "msg_box"}, "thread_id=?", new String[] {id}, "date desc");
                //Log.v("reading_cursor_data", "looking for conversation " + id);

                if (cursor2.moveToFirst()) {
                    //Log.v("reading_cursor_data", "found conversation " + id);
                    int count2 = 0;

                    contactNames[count] = name;
                    contactPics[count] = MainActivity.getFacebookPhoto(number, mContext);
                    //contactNames[count] = "contact " + count;
                    do {
                        //String s = cursor2.getString(cursor2.getColumnIndex("msg_box"));

                        //if (s != null) {
                            //Log.v("reading_cursor_data", "found mms message");
                        //} else {
                            //Log.v("reading_cursor_data", cursor2.getString(cursor2.getColumnIndex("body")));
                            message[count][count2] = cursor2.getString(cursor2.getColumnIndex("body"));
                            String type2 = cursor2.getString(cursor2.getColumnIndex("type"));
                        //}

                        if (type2.equals("2") || type2.equals("4") || type2.equals("5") || type2.equals("6"))
                        {
                            type[count][count2] = 1;
                        } else
                        {
                            type[count][count2] = 0;
                        }

                        count2++;
                    } while (cursor2.moveToNext() && count2 < 3);

                    cursor2.close();

                    count++;
                }
            } while (cursor.moveToNext() && count < 5);

            cursor.close();
        }

        for (int i = 0; i < 5; i++) {
            if (contactNames[i].equals("")) {
                ignore[i] = true;
            }
        }
    }
}
