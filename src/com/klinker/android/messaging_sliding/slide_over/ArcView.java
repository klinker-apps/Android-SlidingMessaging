package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.*;

/**
 * Created by luke on 8/2/13.
 */
public class ArcView extends ViewGroup {
    public Context mContext;

    public Bitmap halo;

    public Paint newMessagePaint;
    public Paint conversationsPaint;
    public float radius;
    public float breakAngle;

    public SharedPreferences sharedPrefs;

    public int height;
    public int width;

    public ArcView(Context context, Bitmap halo, float radius, float breakAngle) {
        super(context);

        mContext = context;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        height = d.getHeight();
        width = d.getWidth();

        newMessagePaint = new Paint();
        newMessagePaint.setAntiAlias(true);
        newMessagePaint.setColor(Color.WHITE);
        newMessagePaint.setAlpha(60);
        newMessagePaint.setStyle(Paint.Style.STROKE);
        newMessagePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, context.getResources().getDisplayMetrics()));

        conversationsPaint = new Paint(newMessagePaint);
        conversationsPaint.setPathEffect(new DashPathEffect(new float[] {10,20}, 0));

        this.halo = halo;
        this.radius = radius;
        this.breakAngle = breakAngle;
    }


    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int[] point = getPosition();

        RectF oval = new RectF(-1 * radius, point[1] + (halo.getHeight() / 2) -  radius, radius, point[1] + (halo.getHeight() / 2) + radius);

        Path newMessagePath = new Path();
        newMessagePath.addArc(oval, breakAngle, -180);

        Path conversationsPath = new Path();
        conversationsPath.addArc(oval, breakAngle, 180);

        canvas.drawPath(newMessagePath, newMessagePaint);
        canvas.drawPath(conversationsPath, conversationsPaint);

        /*

        Original Circle Drawing

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        } else {
            canvas.drawCircle(point[0] + (halo.getHeight()/2), point[1] + (halo.getHeight() / 2), radius, newMessagePaint);
        }

        */
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
            returnArray[0] = (int)(-1 * halo.getWidth() * (1 - SlideOverService.HALO_SLIVER_RATIO));
        } else {
            returnArray[0] = (int)(width - (halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
        }

        returnArray[1] = (int)(height * SlideOverService.PERCENT_DOWN_SCREEN);

        return returnArray;
    }
}
