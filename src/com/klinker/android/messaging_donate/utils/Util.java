package com.klinker.android.messaging_donate.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.CamcorderProfile;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Telephony;

import java.util.List;

public class Util {
    public static boolean isRunning(Context ctx) {
        ActivityManager activityManager = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = activityManager.getRunningTasks(1);

        for (ActivityManager.RunningTaskInfo task : tasks) {
            if (ctx.getPackageName().equalsIgnoreCase(task.baseActivity.getPackageName()))
                return true;
        }

        return false;
    }

    public static void checkOverride(Context context) {
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.setData(Uri.parse("sms:"));
        List<ResolveInfo> smsApps = context.getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);

        if (smsApps.size() == 1) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean("override", true).commit();
        }
    }

    private static final int[] sVideoDuration =
            new int[] {0, 5, 10, 15, 20, 30, 40, 50, 60, 90, 120};

    public static int getVideoCaptureDurationLimit(long bytesAvailable) {
        CamcorderProfile camcorder = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
        if (camcorder == null) {
            return 0;
        }
        bytesAvailable *= 8;        // convert to bits
        long seconds = bytesAvailable / (camcorder.audioBitRate + camcorder.videoBitRate);

        // Find the best match for one of the fixed durations
        for (int i = sVideoDuration.length - 1; i >= 0; i--) {
            if (seconds >= sVideoDuration[i]) {
                return sVideoDuration[i];
            }
        }
        return 0;
    }

    public static boolean isDefaultSmsApp(Context context) {
        if (hasKitKat()) {
            return context.getPackageName().equals(Telephony.Sms.getDefaultSmsPackage(context));
        }

        return true;
    }

    public static void setDefaultSmsApp(Context context) {
        if (hasKitKat()) {
            Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
            intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, context.getPackageName());
            context.startActivity(intent);
        }
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }
}
