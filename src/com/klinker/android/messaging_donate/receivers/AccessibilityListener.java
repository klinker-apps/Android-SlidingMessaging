package com.klinker.android.messaging_donate.receivers;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import com.klinker.android.messaging_sliding.receivers.VoiceReceiver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class AccessibilityListener extends AccessibilityService {

    boolean connected;
    private SharedPreferences settings;

    @Override
    public boolean onUnbind(Intent intent) {
        connected = false;
        return super.onUnbind(intent);
    }

    @Override
    protected void onServiceConnected (){
        super.onServiceConnected();
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        connected = true;

        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;
        info.packageNames = new String[] {"com.google.android.apps.googlevoice"};
        setServiceInfo(info);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.v("accessibility_service", "accessibility event created");

        if (null == settings.getString("voice_account", null)) {
            Log.v("accessibility_service", "account null");
            return;
        }

        if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.v("accessibility_service", "no notification changed");
            return;
        }

        if (!("com.google.android.apps.googlevoice".equals(event.getPackageName()))) {
            Log.v("accessibility_service", "not a google voice notification");
            return;
        }

        clearGoogleVoiceNotifications();

        startService(new Intent(this, VoiceReceiver.class));
    }

    @Override
    public void onInterrupt() {

    }

    Method cancelAllNotifications;
    Object internalNotificationService;
    int userId;
    private void clearGoogleVoiceNotifications() {
        try {
            if (cancelAllNotifications == null) {
                // run this to get the internal service to populate
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancelAll();

                Field f = NotificationManager.class.getDeclaredField("sService");
                f.setAccessible(true);
                internalNotificationService = f.get(null);
                cancelAllNotifications = internalNotificationService.getClass().getDeclaredMethod("cancelAllNotifications", String.class, int.class);
                userId = (Integer)UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
            }
            if (cancelAllNotifications != null) {
                Log.v("accessibility_service", "cancelling notification");
                cancelAllNotifications.invoke(internalNotificationService, "com.google.android.apps.googlevoice", userId);
            }
        } catch (Exception e) {
            Log.v("accessibility_service", "error cancelling...");
            e.printStackTrace();
        }
    }

}
