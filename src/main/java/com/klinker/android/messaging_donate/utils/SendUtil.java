package com.klinker.android.messaging_donate.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.TypedValue;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.settings.AppSettings;
import com.klinker.android.messaging_sliding.quick_reply.QmMarkRead;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Settings;
import com.klinker.android.send_message.Transaction;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

public class SendUtil {

    public static void sendMessage(Context context, String number, String body) {
        try { Looper.prepare(); } catch (Exception e) {}
        Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);
        message.setType(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("voice_enabled", false) ?
                Message.TYPE_VOICE : Message.TYPE_SMSMMS);
        message.setDelay(AppSettings.init(context).sendDelay);

        sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        MainActivity.messageRecieved = true;
    }

    public static void sendMessage(Context context, String[] number, String body) {
        try { Looper.prepare(); } catch (Exception e) {}
        Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);
        message.setType(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("voice_enabled", false) ?
                Message.TYPE_VOICE : Message.TYPE_SMSMMS);
        message.setDelay(AppSettings.init(context).sendDelay);

        sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        MainActivity.messageRecieved = true;
    }

    public static void sendMessage(Context context, String[] number, String body, Bitmap[] images) {
        try { Looper.prepare(); } catch (Exception e) {}
        final Transaction sendTransaction = new Transaction(context, getSendSettings(context));

        final Message message = new Message(body, number);
        message.setImages(images);
        message.setType(PreferenceManager.getDefaultSharedPreferences(context).getBoolean("voice_enabled", false) ?
                Message.TYPE_VOICE : Message.TYPE_SMSMMS);

        new Thread(new Runnable() {
            @Override
            public void run() {
                sendTransaction.sendNewMessage(message, Transaction.NO_THREAD_ID);
            }
        }).start();

        Intent mrIntent = new Intent();
        mrIntent.setClass(context, QmMarkRead.class);
        mrIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mrIntent.putExtra("body", body);
        mrIntent.putExtra("date", Calendar.getInstance().getTimeInMillis() + "");
        mrIntent.putExtra("address", number);
        context.startService(mrIntent);

        MainActivity.messageRecieved = true;
    }

    public static Settings getSendSettings(Context context) {
        final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Settings sendSettings = new Settings();

        sendSettings.setMmsc(sharedPrefs.getString("mmsc_url", ""));
        sendSettings.setProxy(sharedPrefs.getString("mms_proxy", ""));
        sendSettings.setPort(sharedPrefs.getString("mms_port", ""));
        sendSettings.setAgent(sharedPrefs.getString("mms_agent", ""));
        sendSettings.setUserProfileUrl(sharedPrefs.getString("mms_user_agent_profile_url", ""));
        sendSettings.setUaProfTagName(sharedPrefs.getString("mms_user_agent_tag_name", ""));
        sendSettings.setGroup(sharedPrefs.getBoolean("group_message", false));
        sendSettings.setWifiMmsFix(sharedPrefs.getBoolean("wifi_mms_fix", false));
        sendSettings.setDeliveryReports(sharedPrefs.getBoolean("delivery_reports", false));
        sendSettings.setSplit(sharedPrefs.getBoolean("split_sms", false));
        sendSettings.setSplitCounter(sharedPrefs.getBoolean("split_counter", false));
        sendSettings.setStripUnicode(sharedPrefs.getBoolean("strip_unicode", false));
        sendSettings.setSignature(sharedPrefs.getString("signature", ""));
        sendSettings.setPreText(sharedPrefs.getBoolean("giffgaff_delivery", false) ? "*0#" : "");
        sendSettings.setSendLongAsMms(sharedPrefs.getBoolean("send_as_mms", false));
        sendSettings.setSendLongAsMmsAfter(sharedPrefs.getInt("mms_after", 4));
        sendSettings.setAccount(sharedPrefs.getString("voice_account", null));
        sendSettings.setRnrSe(sharedPrefs.getString("voice_rnrse", null));

        return sendSettings;
    }

    public static Bitmap getThumbnail(Context context, Uri uri) throws IOException {

        int THUMBNAIL_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources().getDisplayMetrics());

        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    public static Bitmap getThumbnailRotated(Context context, Uri uri, int angle) throws IOException {

        int THUMBNAIL_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80, context.getResources().getDisplayMetrics());

        InputStream input = context.getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return RotateBitmap(bitmap, angle);
    }

    public static Bitmap getImage(Context context, Uri uri, int size) throws IOException {
        int THUMBNAIL_SIZE = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size, context.getResources().getDisplayMetrics());

        InputStream input = context.getContentResolver().openInputStream(uri);
        Log.v("downloading_image", input + "");

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1))
            return null;

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = context.getContentResolver().openInputStream(uri);
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        } catch (Throwable e) {
            return null;
        }
        input.close();

        Log.v("bitmap_size", bitmap.getByteCount() + " " + bitmap.getHeight() + " " + bitmap.getWidth());

        if (bitmap.getHeight() > MainActivity.settings.mmsMaxHeight || bitmap.getWidth() > MainActivity.settings.mmsMaxWidth) {
            double r;
            if (bitmap.getHeight() > bitmap.getWidth()) {
                r = MainActivity.settings.mmsMaxHeight / (double) bitmap.getHeight();
            } else {
                r = MainActivity.settings.mmsMaxWidth / (double) bitmap.getWidth();
            }

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * r), (int) (bitmap.getHeight() * r), false);
        }

        return bitmap;
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }
}
