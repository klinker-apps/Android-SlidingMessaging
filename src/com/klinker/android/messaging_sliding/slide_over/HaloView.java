package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.ContactUtil;

import java.io.InputStream;

public class HaloView extends ViewGroup {
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

    public HaloView(Context context) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        haloColor = sharedPrefs.getInt("slideover_color", context.getResources().getColor(R.color.white));
        haloUnreadColor = sharedPrefs.getInt("slideover_unread_color", context.getResources().getColor(R.color.holo_red));

        halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        halo = Bitmap.createScaledBitmap(halo, halo.getWidth() * sharedPrefs.getInt("scaled_size", 40)/50, halo.getHeight() * sharedPrefs.getInt("scaled_size", 40)/50, true);

        haloPaint = new Paint();
        haloPaint.setAlpha(haloAlpha);
        haloPaint.setColorFilter(new PorterDuffColorFilter(haloColor, PorterDuff.Mode.MULTIPLY));

        haloNewPaint = new Paint();
        haloNewPaint.setAlpha(haloNewAlpha);
        haloNewPaint.setColorFilter(new PorterDuffColorFilter(haloUnreadColor, PorterDuff.Mode.MULTIPLY));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        if (haloAlpha != 0) {
            haloPaint.setAlpha(haloAlpha);
            canvas.drawBitmap(halo, 0, 0, haloPaint);
        }

        if (haloNewAlpha != 0) {
            haloNewPaint.setAlpha(haloNewAlpha);

            if (sharedPrefs.getBoolean("contact_pics_slideover", true)) {
                canvas.drawBitmap(halo, 0, 0, haloPaint);
                Paint contactPaint = new Paint();
                contactPaint.setAlpha(210);
                canvas.drawBitmap(currentImage == null ? getClip() : currentImage, 0, 0, contactPaint);
            } else {
                canvas.drawBitmap(halo, 0, 0, haloNewPaint);
            }
        }
    }

    public Bitmap currentImage = null;
    private Bitmap getClip() {
        String number;
        try {
            number = ArcView.newConversations.get(ArcView.newConversations.size() - 1)[2];
        } catch (Exception e) {
            // Fix for active notifications not working correctly.
            // they cleared the new conversations array, but it still tried to go through this code.
            number = "";
        }
        
        Bitmap bitmap;

        try {
            bitmap = Bitmap.createScaledBitmap(ContactUtil.getFacebookPhoto(number, mContext), halo.getWidth(), halo.getHeight(), true);
        } catch (Exception e) {
            bitmap = Bitmap.createScaledBitmap(ContactUtil.drawableToBitmap(mContext.getResources().getDrawable(R.drawable.default_avatar), mContext), halo.getWidth(), halo.getHeight(), true);
        }

        Bitmap output = Bitmap.createBitmap(halo.getWidth(),
                halo.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, halo.getWidth(),
                halo.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(halo.getWidth() / 2,
                halo.getHeight() / 2, (halo.getWidth() / 2) - (halo.getWidth()/25), paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, null, rect, paint);
        currentImage = output;
        return output;
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
