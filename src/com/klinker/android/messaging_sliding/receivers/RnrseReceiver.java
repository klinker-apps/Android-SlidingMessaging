package com.klinker.android.messaging_sliding.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;

public class RnrseReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("voice_rnrse", intent.getStringExtra("_rnr_se")).commit();
    }
}
