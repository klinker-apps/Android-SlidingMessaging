package com.klinker.android.messaging_sliding.slide_over;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.*;
import android.telephony.SmsManager;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * Created by luke on 8/2/13.
 */
public class SlideOverView extends ViewGroup {
    Bitmap halo;
    private Canvas canvas;
    public Paint arcPaint;
    public float radius = 300;

    public float haloX = 10;
    public float haloY = 10;

    public SlideOverView(Context context, Bitmap halo, float radius) {
        super(context);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.WHITE);
        arcPaint.setAlpha(60);
        arcPaint.setStyle(Paint.Style.STROKE);

        this.halo = halo;
        this.radius = radius;

        this.haloX = -1 * (halo.getWidth() / 2);
    }

    protected void onDraw(Canvas canvas) {

        this.canvas = canvas;

        canvas.drawCircle(haloX + (halo.getWidth() / 2), haloY + (halo.getHeight() / 2), radius, arcPaint);
        //canvas.drawCircle(circleX, circleY, radius - 1, arcPaintInner);
        canvas.drawBitmap(halo, haloX, haloY, null);
    }

    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }


}
