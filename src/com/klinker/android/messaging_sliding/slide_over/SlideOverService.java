package com.klinker.android.messaging_sliding.slide_over;

import android.app.ActivityManager;
import android.app.Service;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.os.Vibrator;
import android.preference.PreferenceManager;
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

        params = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                0,
                0,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        setGravity(params);

        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (width > height)
            SWIPE_MIN_DISTANCE = height/3;
        else
            SWIPE_MIN_DISTANCE = width/3;

        mView = new SlideOverView(this, halo, SWIPE_MIN_DISTANCE);

        mView.setOnTouchListener(new View.OnTouchListener() {
            private boolean needDetection = false;
            private boolean vibrateNeeded = true;

            private float initX;
            private float initY;

            private double distance = 0;
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if((arg1.getX() > mView.getX() && arg1.getX() < mView.getX() + halo.getWidth() && arg1.getY() > mView.getY() && arg1.getY() < mView.getY() + halo.getHeight()) || needDetection)
                {
                    final int type = arg1.getActionMasked();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    switch (type)
                    {
                        case MotionEvent.ACTION_DOWN:

                            v.vibrate(10);

                            int[] position = getPosition();
                            initX = arg1.getX() + position[0];
                            initY = arg1.getY() + position[1];

                            params = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.TYPE_PHONE,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                                            |WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                                    PixelFormat.TRANSLUCENT);
                            params.dimAmount=.4f;
                            setGravity(params);

                            mView.isTouched = true;
                            mView.arcPaint.setAlpha(60);
                            mView.invalidate();
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
                                    0,
                                    WindowManager.LayoutParams.TYPE_PHONE,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                    PixelFormat.TRANSLUCENT);
                            setGravity(params);

                            mView.isTouched = false;
                            //mView.animate = true;
                            mView.invalidate();
                            wm.removeViewImmediate(mView);
                            wm.addView(mView, params);

                            needDetection = true;

                            return true;
                    }

                }

                return false;
            }
        });

        wm.addView(mView, params);

        BroadcastReceiver stopSlideover = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mView.invalidate();
                wm.removeViewImmediate(mView);
                stopSelf();
                unregisterReceiver(this);
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.STOP_HALO");
        registerReceiver(stopSlideover, filter);

    }

    public void setGravity(WindowManager.LayoutParams lp)
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                lp.gravity = Gravity.LEFT | Gravity.TOP;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                lp.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            } else {
                lp.gravity = Gravity.LEFT | Gravity.BOTTOM;
            }
        } else {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                lp.gravity = Gravity.RIGHT | Gravity.TOP;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                lp.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
            } else {
                lp.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            }
        }
    }

    public int[] getPosition()
    {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int[] returnArray = {0, 0};

        Display d = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int height = d.getHeight();
        int width = d.getWidth();

        if (sharedPrefs.getString("slideover_side", "left").equals("left")) {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                returnArray[0] = 0;
                returnArray[1] = 0;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                returnArray[0] = 0;
                returnArray[1] = (height/2) - (mView.halo.getHeight()/2);
            } else {
                returnArray[0] = 0;
                returnArray[1] = (height) - (mView.halo.getHeight());
            }
        } else {
            if (sharedPrefs.getString("slideover_alignment", "middle").equals("top")) {
                returnArray[0] = width - mView.halo.getWidth();
                returnArray[1] = 0;
            } else if (sharedPrefs.getString("slideover_alignment", "middle").equals("middle")) {
                returnArray[0] = width - mView.halo.getWidth();
                returnArray[1] = (height/2) - (mView.halo.getHeight()/2);
            } else {
                returnArray[0] = width - mView.halo.getWidth();
                returnArray[1] = (height) - (mView.halo.getHeight());
            }
        }

        return returnArray;
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
