package com.klinker.android.messaging_sliding.slide_over;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.IBinder;
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

    @Override
    public void onCreate() {
        super.onCreate();

        Toast.makeText(getBaseContext(),"onCreate", Toast.LENGTH_LONG).show();

        final Bitmap halo = BitmapFactory.decodeResource(getResources(),
                R.drawable.halo_bg);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.RIGHT | Gravity.TOP;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);



        mView = new SlideOverView(this,halo);

        mView.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                if(arg1.getX()<halo.getWidth() & arg1.getY()>0)
                {
                    if (!isRunning(getApplication()))
                    {
                        Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
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
