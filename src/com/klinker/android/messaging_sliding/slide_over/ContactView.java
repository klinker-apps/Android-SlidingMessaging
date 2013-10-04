package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;

import java.util.Arrays;

public class ContactView extends ViewGroup {
    public static Context mContext;

    public Paint strokePaint;
    public Paint blackPaint;
    public Paint contactCurrentPaint;
    public Paint contactClosedPaint;

    public Rect contOneRect;
    public Rect contTwoRect;
    public Rect contThreeRect;
    public Rect contFourRect;
    public Rect contFiveRect;

    public static SharedPreferences sharedPrefs;

    public static Resources resources;

    public int height;
    public int width;

    public static int numberOfContacts = 0;

    public static int currentContact = 0;

    public static Bitmap[] contactPics = new Bitmap[6];
    public static String[] contactNames = new String[6];
    public static String[] threadIds = new String[6];
    public static String[] numbers = new String[6];
    public static String[][] message = new String[6][3];
    public static int[][] type = new int[6][3];
    public static boolean[] ignore = new boolean[6];

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
        contactCurrentPaint.setAlpha(200);

        contactClosedPaint = new Paint();
        contactClosedPaint.setColor(getResources().getColor(R.color.black));
        contactClosedPaint.setAlpha(40);

        resources = getResources();

        contOneRect = new Rect(0, 0, toDP(60), toDP(60));
        contTwoRect = new Rect(toDP(63), 0, toDP(123), toDP(60));
        contThreeRect = new Rect(toDP(126), 0, toDP(186), toDP(60));
        contFourRect = new Rect(toDP(189), 0, toDP(249), toDP(60));
        contFiveRect = new Rect(toDP(252), 0, toDP(312), toDP(60));

        numberOfContacts = sharedPrefs.getInt("quick_peek_contact_num", 5);

        refreshArrays();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        if (!ignore[0]) {
            canvas.drawRect(contOneRect, blackPaint);
            //if (currentContact != 0)
            canvas.drawRect(contOneRect, strokePaint);
            try {
                canvas.drawBitmap(contactPics[0], null, contOneRect, currentContact == 0 ? contactCurrentPaint : contactClosedPaint);
            } catch (Exception e) {
                canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar), null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);;
            }
        }

        if (!ignore[1]) {
            canvas.drawRect(contTwoRect, blackPaint);
            //if (currentContact != 1)
            canvas.drawRect(contTwoRect, strokePaint);
            try {
                canvas.drawBitmap(contactPics[1], null, contTwoRect, currentContact == 1 ? contactCurrentPaint : contactClosedPaint);
            } catch (Exception e) {
                canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar), null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);;
            }
        }

        if (!ignore[2]) {
            canvas.drawRect(contThreeRect, blackPaint);
            if (currentContact != 2)
                canvas.drawRect(contThreeRect, strokePaint);
            try {
                canvas.drawBitmap(contactPics[2], null, contThreeRect, currentContact == 2 ? contactCurrentPaint : contactClosedPaint);
            } catch (Exception e) {
                canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar), null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);;
            }
        }

        if (!ignore[3]) {
            canvas.drawRect(contFourRect, blackPaint);
            //if (currentContact != 3)
            canvas.drawRect(contFourRect, strokePaint);
            try {
                canvas.drawBitmap(contactPics[3], null, contFourRect, currentContact == 3 ? contactCurrentPaint : contactClosedPaint);
            } catch (Exception e) {
                canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar), null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);;
            }
        }

        if (!ignore[4]) {
            canvas.drawRect(contFiveRect, blackPaint);
            //if (currentContact != 4)
            canvas.drawRect(contFiveRect, strokePaint);
            try {
                canvas.drawBitmap(contactPics[4], null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);
            } catch (Exception e) {
                canvas.drawBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_avatar), null, contFiveRect, currentContact == 4 ? contactCurrentPaint : contactClosedPaint);;
            }
        }

        canvas.drawCircle(width - 50 - toDP(60), toDP(30), toDP(10), blackPaint);
        canvas.drawCircle(width - 50 - toDP(60), toDP(30), toDP(10), strokePaint);
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public static void refreshArrays() {
        for (int i = 0; i < 6; i++) {
            contactNames[i] = "";
        }

        for (int i = 0; i < 6; i++) {
            numbers[i] = "";
        }

        for (int i = 0; i < 6; i++) {
            threadIds[i] = "";
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j< 3; j++) {
                message[i][j] = "";
            }
        }

        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 3; j++) {
                type[i][j] = 0;
            }
        }

        Arrays.fill(ignore, Boolean.FALSE);

        Uri SMS_CONTENT_URI = Uri.parse("content://mms-sms/conversations/?simple=true");
        Cursor cursor;

        if (sharedPrefs.getBoolean("slideover_only_unread", false)) {
            cursor = mContext.getContentResolver().query(SMS_CONTENT_URI, new String[]{"_id", "recipient_ids"}, "read=?", new String[]{"0"}, "date desc");
        } else {
            cursor = mContext.getContentResolver().query(SMS_CONTENT_URI, new String[]{"_id", "recipient_ids"}, null, null, "date desc");
        }

        try {
            if (cursor.moveToFirst()) {
                int count = 0;
                do {
                    String id = cursor.getString(cursor.getColumnIndex("_id"));
                    String number = ContactUtil.findContactNumber(cursor.getString(cursor.getColumnIndex("recipient_ids")), mContext);
                    String name = ContactUtil.findContactName(number, mContext);

                    Cursor cursor2;

                    cursor2 = mContext.getContentResolver().query(Uri.parse("content://sms/"), new String[]{"body", "type", "thread_id"}, "thread_id=?", new String[]{id}, "date desc");
                    //Cursor cursor2 = mContext.getContentResolver().query( Uri.parse("content://mms-sms/conversations/"), new String[]{"body", "address", "thread_id", "msg_box"}, "thread_id=?", new String[] {id}, "date desc");

                    if (cursor2.moveToFirst()) {
                        int count2 = 0;

                        contactNames[count] = name;
                        threadIds[count] = id;
                        numbers[count] = number;
                        contactPics[count] = ContactUtil.getFacebookPhoto(number, mContext);

                        if (contactPics[count] == null) {
                            contactPics[count] = BitmapFactory.decodeResource(mContext.getResources(),
                                    R.drawable.default_avatar);
                        }
                        do {
                            /*String s = cursor2.getString(cursor2.getColumnIndex("msg_box"));

                            if (s != null) {
                                //Log.v("reading_cursor_data", "found mms message");
                            } else {
                                //Log.v("reading_cursor_data", cursor2.getString(cursor2.getColumnIndex("body")));
                                message[count][count2] = cursor2.getString(cursor2.getColumnIndex("body"));
                                String type2 = cursor2.getString(cursor2.getColumnIndex("type"));
                            }*/

                            message[count][count2] = cursor2.getString(cursor2.getColumnIndex("body"));
                            String type2 = cursor2.getString(cursor2.getColumnIndex("type"));

                            if (type2.equals("2") || type2.equals("4") || type2.equals("5") || type2.equals("6")) {
                                type[count][count2] = 1;
                            } else {
                                type[count][count2] = 0;
                            }

                            count2++;
                        } while (cursor2.moveToNext() && count2 < 3);

                        cursor2.close();

                        count++;
                    }
                } while (cursor.moveToNext() && count < numberOfContacts);

                cursor.close();
            }
        } catch (Exception e) {
            // something is wrong with the cursor causing it to crash
        }

        for (int i = 0; i < 5; i++) {
            if (contactNames[i].equals("")) {
                ignore[i] = true;
            }
        }
    }

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, SlideOverService.displayMatrix);
    }
}
