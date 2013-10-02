package com.klinker.android.messaging_sliding.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class VoicePushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, VoiceReceiver.class));
    }
}
