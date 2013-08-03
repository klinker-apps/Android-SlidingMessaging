package com.klinker.android.messaging_sliding.slide_over;

import android.app.Service;
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
                R.drawable.ic_cancel);


        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                halo.getWidth(),
                halo.getHeight(),
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        |WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        |WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);






        params.gravity = Gravity.LEFT | Gravity.BOTTOM;
        params.setTitle("Load Average");
        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);



        mView = new SlideOverView(this,halo);

        mView.setOnTouchListener(new View.OnTouchListener() {


            @Override
            public boolean onTouch(View arg0, MotionEvent arg1) {
                // TODO Auto-generated method stub
                //Log.e("kordinatlar", arg1.getX()+":"+arg1.getY()+":"+display.getHeight()+":"+kangoo.getHeight());
                if(arg1.getX()<halo.getWidth() & arg1.getY()>0)
                {
                    Intent intent = new Intent(getBaseContext(), com.klinker.android.messaging_sliding.MainActivityPopup.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                return false;
            }
        });


        wm.addView(mView, params);



    }



    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
