package com.klinker.android.messaging_donate.receivers;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import java.util.List;

public class UnlockReceiver extends BroadcastReceiver {

    public static boolean openApp = false;

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        if (intent.getAction() != null && openApp == true)
        {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
            boolean halo = sharedPrefs.getBoolean("halo_popup", false);
            
            final Intent intent3;
            
            if (halo) {
                intent3 = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class); 
            } else {
                intent3 = new Intent(context, com.klinker.android.messaging_sliding.MainActivityPopup.class);
            }

            try
            {
                if (halo) {
                    intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
                } else {
                    intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
            } catch (Exception e)
            {
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    if (!isRunning(context)) {
                        context.startActivity(intent3);
                        openApp = false;
                    }
                }
            }, 200);
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
}