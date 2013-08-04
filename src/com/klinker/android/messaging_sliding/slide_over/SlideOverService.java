package com.klinker.android.messaging_sliding.slide_over;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.klinker.android.messaging_donate.R;

import java.util.List;

/**
 * Created by luke on 8/2/13.
 */
public class SlideOverService extends Service {

    SlideOverView mView;
    private static int SWIPE_MIN_DISTANCE = 300;

    public WindowManager.LayoutParams params;

    private int pixelsDown;

    public Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;

        final Bitmap halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int height = d.getHeight();
        final int width = d.getWidth();

        double heightPercent = 0;

        pixelsDown = (int)(height * heightPercent);

        params = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                0,
                pixelsDown,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;

        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (width > height)
            SWIPE_MIN_DISTANCE = height/3;
        else
            SWIPE_MIN_DISTANCE = width/3;

        mView = new SlideOverView(this, halo, SWIPE_MIN_DISTANCE, pixelsDown);

        mView.setOnTouchListener(new View.OnTouchListener() {
            private boolean needDetection = false;
            private boolean vibrateNeeded = true;

            private float initX;
            private float initY;

            private double distance = 0;
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if((arg1.getX()<halo.getWidth() && arg1.getY()>0) || needDetection)
                {
                    final int type = arg1.getActionMasked();

                    switch (type)
                    {
                        case MotionEvent.ACTION_DOWN:
                            initX = arg1.getX();
                            initY = arg1.getY();

                            params = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.TYPE_PHONE,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                            |WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                                    PixelFormat.TRANSLUCENT);
                            params.gravity = Gravity.LEFT | Gravity.TOP;
                            params.dimAmount=.4f;

                            mView.arcPaint.setAlpha(60);
                            wm.updateViewLayout(mView, params);
                            needDetection = true;
                            return true;

                        case MotionEvent.ACTION_MOVE:

                            distance = Math.sqrt(Math.pow(initX - arg1.getX(), 2) + Math.pow(initY - arg1.getY(),2));

                            if (distance > SWIPE_MIN_DISTANCE && vibrateNeeded)
                            {
                                mView.arcPaint.setAlpha(150);
                                mView.invalidate();
                                wm.updateViewLayout(mView, params);
                                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(25);

                                vibrateNeeded = false;
                            }

                            if (distance < SWIPE_MIN_DISTANCE)
                            {
                                mView.arcPaint.setAlpha(60);
                                mView.invalidate();
                                wm.updateViewLayout(mView, params);
                                vibrateNeeded = true;
                            }

                            return true;

                        case MotionEvent.ACTION_UP:

                            if(distance > SWIPE_MIN_DISTANCE)
                            {
                                if (isRunning(getApplication())) {
                                    Intent intent = new Intent();
                                    intent.setAction("com.klinker.android.messaging_donate.KILL_FOR_HALO");
                                    sendBroadcast(intent);
                                }

                                Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("fromHalo", true);
                                startActivity(intent);
                            }

                            params = new WindowManager.LayoutParams(
                                    halo.getWidth(),
                                    halo.getHeight(),
                                    0,
                                    pixelsDown,
                                    WindowManager.LayoutParams.TYPE_PHONE,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                    PixelFormat.TRANSLUCENT);
                            params.gravity = Gravity.LEFT | Gravity.TOP;

                            wm.updateViewLayout(mView, params);

                            needDetection = false;

                            return true;
                    }

                }

                return false;
            }
        });

        wm.addView(mView, params);

    }

    public boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
