package com.klinker.android.messaging_sliding.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class VoicePushReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("voice_push", "voice message received, starting download");
        context.startService(new Intent(context, VoiceReceiver.class));
    }
}
