package com.klinker.android.messaging_sliding.notifications;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;
import com.klinker.android.messaging_donate.utils.SendUtil;
import com.klinker.android.messaging_sliding.Conversation;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.pushbullet.android.extension.MessagingExtension;

public class PushbulletService extends MessagingExtension {

    private static final String TAG = "PushbulletService";

    public static final String UPDATE_NOTIFICATION = "com.klinker.android.messaging.UPDATE_NOTIFICATION";

    @Override
    protected void onMessageReceived(final String conversationIden, final String message) {
        String address;

        if (!conversationIden.startsWith("mms")) {
            Cursor query = getContentResolver().query(
                    Uri.parse("content://sms/inbox"),
                    new String[]{"address", "body", "thread_id"},
                    "thread_id=?",
                    new String[]{conversationIden},
                    "date desc limit 1"
            );

            query.moveToFirst();
            address = query.getString(0);
            query.close();
        } else {
            address = conversationIden;
        }

        Log.v(TAG, "Pushbullet Message from " + address + ": " + message);

        try { Looper.prepare(); } catch (Exception e) { }
        SendUtil.sendMessage(this, address, message);
    }

    @Override
    public void onConversationDismissed(final String conversationIden) {
        Log.v(TAG, "conversation dismissed: " + conversationIden);
        final NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!conversationIden.startsWith("mms")) {
            long threadId = Long.parseLong(conversationIden);
            Log.v(TAG, "dismissing " + threadId);
            markRead(threadId);
            QmMarkRead.enabled = false;
            sendBroadcast(new Intent(UPDATE_NOTIFICATION));
            try {
                Thread.sleep(500);
            } catch (Exception e) { }
            QmMarkRead.enabled = true;
        } else {
            // conversation dismissed was mms, which only supports one message in the notification
            // so dismiss that notification
            QmMarkRead.enabled = false;
            mNotificationManager.cancel(2);
            try {
                Thread.sleep(200);
            } catch (Exception e) { }
            QmMarkRead.enabled = true;
        }
    }

    private void markRead(long threadId) {
        Conversation.markConversationAsReadNoAsync(this, threadId);
        Intent stopRepeating = new Intent(this, NotificationRepeaterService.class);
        PendingIntent pStopRepeating = PendingIntent.getService(this, 0, stopRepeating, 0);
        AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pStopRepeating);
    }
}
