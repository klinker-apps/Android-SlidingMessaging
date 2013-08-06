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

    public HaloView haloView;
    public ArcView arcView;

    public WindowManager.LayoutParams haloParams;
    public WindowManager.LayoutParams arcParams;

    public Context mContext;
    public WindowManager haloWindow;
    public WindowManager arcWindow;
    public SharedPreferences sharedPrefs;

    public static int SWIPE_MIN_DISTANCE = 0;
    public static double HALO_SLIVER_RATIO = .33;
    public static double PERCENT_DOWN_SCREEN = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

        final Bitmap halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);

        Display d = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        final int height = d.getHeight();
        final int width = d.getWidth();

        HALO_SLIVER_RATIO = sharedPrefs.getInt("slideover_sliver", 33)/100.0;

        PERCENT_DOWN_SCREEN = sharedPrefs.getInt("slideover_vertical", 50)/100.0;
        PERCENT_DOWN_SCREEN -= PERCENT_DOWN_SCREEN * (halo.getHeight()/(double)height);

        haloParams = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                sharedPrefs.getString("slideover_side", "left").equals("left") ? (int) (-1 * (1 - HALO_SLIVER_RATIO) * halo.getWidth()) : (int) (width - (halo.getWidth() * (HALO_SLIVER_RATIO))),
                (int)(height * PERCENT_DOWN_SCREEN),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        |WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        haloParams.gravity = Gravity.TOP | Gravity.LEFT;

        arcParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        |WindowManager.LayoutParams.FLAG_DIM_BEHIND,
                PixelFormat.TRANSLUCENT);
        arcParams.dimAmount=.4f;

        setGravity(haloParams);

        haloWindow = (WindowManager) getSystemService(WINDOW_SERVICE);
        arcWindow = (WindowManager) getSystemService(WINDOW_SERVICE);

        if (width > height)
            SWIPE_MIN_DISTANCE = (int)(height * (sharedPrefs.getInt("slideover_activation", 33)/100.0));
        else
            SWIPE_MIN_DISTANCE = (int)(width * (sharedPrefs.getInt("slideover_activation", 33)/100.0));

        haloView = new HaloView(this, halo);
        arcView = new ArcView(this, halo, SWIPE_MIN_DISTANCE);

        haloView.setOnTouchListener(new View.OnTouchListener() {
            private boolean needDetection = false;
            private boolean vibrateNeeded = true;

            private float initX;
            private float initY;

            private double distance = 0;

            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if ((arg1.getX() > haloView.getX() && arg1.getX() < haloView.getX() + halo.getWidth() && arg1.getY() > haloView.getY() && arg1.getY() < haloView.getY() + halo.getHeight()) || needDetection) {
                    final int type = arg1.getActionMasked();
                    Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                    switch (type) {
                        case MotionEvent.ACTION_DOWN:

                            v.vibrate(10);

                            int[] position = getPosition();
                            initX = arg1.getX();
                            initY = arg1.getY();

                            arcView.arcPaint.setAlpha(60);

                            arcWindow.addView(arcView, arcParams);
                            needDetection = true;
                            return true;

                        case MotionEvent.ACTION_MOVE:

                            distance = Math.sqrt(Math.pow(initX - arg1.getX(), 2) + Math.pow(initY - arg1.getY(), 2));

                            if (distance > SWIPE_MIN_DISTANCE && vibrateNeeded) {
                                arcView.arcPaint.setAlpha(150);
                                arcView.invalidate();
                                arcWindow.updateViewLayout(arcView, arcParams);

                                v.vibrate(25);

                                vibrateNeeded = false;
                            }

                            if (distance < SWIPE_MIN_DISTANCE) {
                                arcView.arcPaint.setAlpha(60);
                                arcView.invalidate();
                                arcWindow.updateViewLayout(arcView, arcParams);
                                vibrateNeeded = true;
                            }

                            return true;

                        case MotionEvent.ACTION_UP:

                            if (distance > SWIPE_MIN_DISTANCE) {
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

                            arcView.arcPaint.setAlpha(60);

                            arcWindow.removeViewImmediate(arcView);

                            needDetection = true;

                            return true;
                    }

                }

                return false;
            }
        });

        haloWindow.addView(haloView, haloParams);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.STOP_HALO");
        registerReceiver(stopSlideover, filter);

        filter = new IntentFilter();
        filter.addAction("android.intent.action.CONFIGURATION_CHANGED");
        this.registerReceiver(mBroadcastReceiver, filter);
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
            returnArray[0] = (int)(-1 * haloView.halo.getWidth() * (1 - SlideOverService.HALO_SLIVER_RATIO));
        } else {
            returnArray[0] = (int)(width - (haloView.halo.getWidth() * SlideOverService.HALO_SLIVER_RATIO));
        }

        returnArray[1] = (int)(height * SlideOverService.PERCENT_DOWN_SCREEN);

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
    public void onDestroy() {
        super.onDestroy();

        try {
            unregisterReceiver(stopSlideover);
            unregisterReceiver(mBroadcastReceiver);
        } catch (Exception e) {

        }
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    public BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent myIntent) {
            stopService(new Intent(context, SlideOverService.class));
            haloView.invalidate();
            haloWindow.removeViewImmediate(haloView);
            unregisterReceiver(this);
            startService(new Intent(context, SlideOverService.class));
        }
    };

    public BroadcastReceiver stopSlideover = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            haloView.invalidate();
            haloWindow.removeViewImmediate(haloView);
            stopSelf();
            unregisterReceiver(this);
        }
    };
}
