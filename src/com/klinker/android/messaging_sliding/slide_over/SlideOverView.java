package com.klinker.android.messaging_sliding.slide_over;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.TypedValue;
import android.view.*;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by luke on 8/2/13.
 */
public class SlideOverView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;
    private Canvas canvas;
    public Paint arcPaint;
    public float radius;

    public float haloX = 0;
    public float haloY = 0;

    public boolean isTouched = false;
    public boolean animate = false;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public SlideOverView(Context context, Bitmap halo, float radius) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.WHITE);
        arcPaint.setAlpha(60);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        this.halo = halo;
        this.radius = radius;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        this.canvas = canvas;

        if (!isTouched) {
            canvas.drawBitmap(halo, 0, 0, null);
        } else {
            int[] point = getPosition();

            if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
                canvas.drawCircle(0 - (int)(halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO), point[1] + (halo.getHeight() / 2), radius, arcPaint);
            } else {
                canvas.drawCircle(width + (int)(halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO), point[1] + (halo.getHeight() / 2), radius, arcPaint);
            }

            canvas.drawBitmap(halo, point[0], point[1], null);
        }

        /*if (animate)
        {
            int aniRadius = halo.getWidth();
            int[] point = getPosition();
            canvas.save();

            if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
                for (int i = 0; i < height/3 - aniRadius; i+=3)
                {
                    canvas.drawCircle(0 - (int)(halo.getWidth() * .25), point[1] + (halo.getHeight() / 2), aniRadius + i, arcPaint);
                    //canvas.restore();
                }
            } else {
                for (int i = 0; i < height/3 - aniRadius; i+=3)
                {
                    canvas.drawCircle(width + (int)(halo.getWidth() * .25), point[1] + (halo.getHeight() / 2), aniRadius + i, arcPaint);
                    //canvas.restore();
                }
            }

            animate = false;
        }*/
    }

    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    public int[] getPosition()
    {
        int[] returnArray = {0, 0};

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                returnArray[0] = (int)(-1 * halo.getWidth() * (1-SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = 0;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                returnArray[0] = (int)(-1 * halo.getWidth() * (1-SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = (height/2) - (halo.getHeight()/2);
            } else {
                returnArray[0] = (int)(-1 * halo.getWidth() * (1-SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = (height) - (halo.getHeight());
            }
        } else {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = 0;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = (height/2) - (halo.getHeight()/2);
            } else {
                returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
                returnArray[1] = (height) - (halo.getHeight());
            }
        }

        return returnArray;
    }
}
