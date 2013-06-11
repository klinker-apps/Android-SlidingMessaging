package com.klinker.android.messaging_donate;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;

public class ScreenUpdateService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        BroadcastReceiver mReceiver = new ScreenReceiver();
        registerReceiver(mReceiver, filter);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        boolean screenOn = intent.getBooleanExtra("screen_state", false);
        if (!screenOn) {

        } else {
            Intent intent3 = new Intent(this, com.klinker.android.messaging_donate.MainActivity.class);

            try
            {
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
            } catch (Exception e)
            {
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            startActivity(intent3);
        }
    }

    public class LocalBinder extends Binder {
        ScreenUpdateService getService() {
            return ScreenUpdateService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private final IBinder mBinder = new LocalBinder();
}