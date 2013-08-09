package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.WindowManager;


public class AnimationView extends ViewGroup {

    public Context mContext;
    public SharedPreferences sharedPrefs;

    public static float TEXT_SIZE;
    public static float ORIG_ARC_OFFSET;

    public Bitmap halo;

    public boolean circleText = false;
    public Paint textPaint;
    public Paint circlePaint;
    public float circleRadius;
    public float circleStroke;
    public RectF oval;
    public Path textPath;

    public int height;
    public int width;

    public float arcOffset;
    public boolean firstText = true;
    public String[] name = new String[2];

    public AnimationView(Context context, Bitmap halo) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        TEXT_SIZE = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, context.getResources().getDisplayMetrics());

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setAlpha(200);
        textPaint.setTextSize(TEXT_SIZE);
        textPaint.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        
        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.BLACK);
        circlePaint.setAlpha(200);
        circlePaint.setStrokeWidth(TEXT_SIZE);
        circlePaint.setStyle(Paint.Style.STROKE);

        this.halo = halo;
        
        circleRadius = (halo.getWidth()/2) + (TEXT_SIZE/2);
        circleStroke = TEXT_SIZE;

        int radius = halo.getWidth();
        int xOffset = (int)(-1 * (1 - SlideOverService.HALO_SLIVER_RATIO) * radius);
        int yOffset = (int)(height * SlideOverService.PERCENT_DOWN_SCREEN);

        if (!sharedPrefs.getString("slideover_side", "left").equals("left")) {
            xOffset = (int) (width - (halo.getWidth() * (SlideOverService.HALO_SLIVER_RATIO)));
        }

        oval = new RectF(xOffset, yOffset, xOffset + radius, yOffset + radius);
        arcOffset = (float)(3.14 * radius * (SlideOverService.HALO_SLIVER_RATIO + .1));
        ORIG_ARC_OFFSET = arcOffset;
        textPath = new Path();

        int arcLength = (int)(360 - ((1 - SlideOverService.HALO_SLIVER_RATIO - .1) * 360));;
        int arcStart;

        if (!sharedPrefs.getString("slideover_side", "left").equals("left")) {
            arcStart = (int)(0 + ((1 - SlideOverService.HALO_SLIVER_RATIO - .1)*180));

            if (SlideOverService.HALO_SLIVER_RATIO >= .80) {
                arcLength = 340;
                arcStart = 0;
            }
        } else {
            arcStart = (int)(180 + ((1 - SlideOverService.HALO_SLIVER_RATIO - .1)*180));

            if (SlideOverService.HALO_SLIVER_RATIO >= .80) {
                arcLength = 340;
                arcStart = 180;
            }
        }

        textPath.addArc(oval, arcStart, arcLength);
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (circleText) {
            circlePaint.setStrokeWidth(circleStroke);
            canvas.drawCircle(oval.centerX(), oval.centerY(), circleRadius, circlePaint);
            canvas.drawTextOnPath(firstText ? name[0] : name[1], textPath, arcOffset, 0f, textPaint);
        }
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }
}
