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
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

import java.util.List;

/**
 * Created by luke on 8/2/13.
 */
public class SlideOverService extends Service {

    SlideOverView mView;
    private static final int SWIPE_MIN_DISTANCE = 300;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;
    private GestureDetector gestureDetector;
    View.OnTouchListener gestureListener;
    public WindowManager.LayoutParams params;

    @Override
    public void onCreate() {
        super.onCreate();

        final Bitmap halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);


        params = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.setTitle("Load Average");
        final WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);

        mView = new SlideOverView(this,halo);

        // Gesture detection
        /*gestureDetector = new GestureDetector(this, new MyGestureDetector());
        gestureListener = new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        };*/

        mView.setOnTouchListener(new View.OnTouchListener() {
            private boolean needDetection = false;
            private boolean vibrateNeeded = true;

            private float initX;
            private float initY;

            private double distance = 0;
            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {

                if((arg1.getX()<halo.getWidth() & arg1.getY()>0 && !isRunning(getApplication())) || needDetection)
                {
                    final int type = arg1.getActionMasked();

                    switch (type)
                    {
                        case MotionEvent.ACTION_DOWN:
                            initX = arg1.getX();
                            initY = arg1.getY();

                            params = new WindowManager.LayoutParams(
                                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                    PixelFormat.TRANSPARENT);
                            params.gravity = Gravity.LEFT | Gravity.TOP;

                            wm.updateViewLayout(mView, params);
                            needDetection = true;
                            return true;

                        case MotionEvent.ACTION_MOVE:

                            distance = Math.sqrt(Math.pow(initX - arg1.getX(), 2) + Math.pow(initY - arg1.getY(),2));

                            if (distance > SWIPE_MIN_DISTANCE && vibrateNeeded)
                            {
                                mView.arcPaint.setAlpha(100);
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

                            break;

                        case MotionEvent.ACTION_UP:

                            if(distance > SWIPE_MIN_DISTANCE)
                            {
                                Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }

                            params = new WindowManager.LayoutParams(
                                    halo.getWidth(),
                                    halo.getHeight(),
                                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                                            |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                            |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                                    PixelFormat.TRANSLUCENT);
                            params.gravity = Gravity.LEFT | Gravity.TOP;

                            wm.updateViewLayout(mView, params);

                            needDetection = false;

                            return false;
                    }

                }
                return true;
            }
        });

        // Do this for each view added to the grid
        //mView.setOnClickListener(SelectFilterActivity.this);
        //mView.setOnTouchListener(gestureListener);

        wm.addView(mView, params);

    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {
        // When the event is done and the user has released their finger

        double initX = 0;
        double initY = 0;

        double changeX = 0;
        double changeY = 0;
        double distance = 0;

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //double distance = Math.sqrt(Math.pow(e1.getX()- e2.getX(), 2) + Math.pow(e1.getY() -e2.getY(),2));
                //double distance = Math.sqrt(Math.pow(initX- e2.getX(), 2) + Math.pow(initY -e2.getY(),2));
            distance += Math.pow(distanceX*distanceX + distanceY*distanceY, 1/2);
            if(distance > SWIPE_MIN_DISTANCE) {
                if (!isRunning(getApplication()))
                {
                    Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                    intent.putExtra("fromHalo", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
            return false;
        }

        // First press of the event, will draw overlay arch from here
        @Override
        public boolean onDown(MotionEvent event)
        {
            super.onDown(event);

            initX = event.getX();
            initY = event.getY();


            /*final Bitmap arch = BitmapFactory.decodeResource(getResources(),
                    R.drawable.halo_bg);

            SlideOverView archView = new SlideOverView(getBaseContext(), arch);

            WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
            wm.addView(mView, params);*/

            return false;
        }
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
