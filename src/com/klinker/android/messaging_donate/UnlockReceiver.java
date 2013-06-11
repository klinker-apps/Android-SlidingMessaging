package com.klinker.android.messaging_donate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class UnlockReceiver extends BroadcastReceiver {

    public static boolean openApp = false;

    @Override
    public void onReceive(final Context context, Intent intent)
    {
        if (intent.getAction() != null && openApp == true)
        {
            final Intent intent3 = new Intent(context, com.klinker.android.messaging_donate.MainActivity.class);

            try
            {
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | 0x00002000);
            } catch (Exception e)
            {
                intent3.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run()
                {
                    context.startActivity(intent3);
                    openApp = false;
                }
            }, 200);
        }
    }
}