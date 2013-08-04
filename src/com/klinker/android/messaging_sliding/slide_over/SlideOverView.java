package com.klinker.android.messaging_sliding.slide_over;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.telephony.SmsManager;
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

    public int haloX = 0;
    public int haloY = 0;

    public SlideOverView(Context context,Bitmap halo) {
        super(context);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setColor(Color.WHITE);
        arcPaint.setAlpha(60);
        arcPaint.setStyle(Paint.Style.STROKE);

        this.halo = halo;
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
