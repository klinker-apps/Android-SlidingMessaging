package com.klinker.android.messaging_donate.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.blacklist.BlacklistContact;
import com.klinker.android.messaging_sliding.notifications.IndividualSetting;
import com.klinker.android.messaging_sliding.scheduled.ScheduledSms;

import java.io.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import a_vcard.android.syncml.pim.PropertyNode;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.VCardParser;

public class IOUtil {

    public static final int TRIAL_LENGTH = 15;

    public static byte[] readFile(String file) throws IOException {
        return readFile(new File(file));
    }

    public static byte[] readFile(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");
        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength)
                throw new IOException("File size >= 2 GB");
            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        } finally {
            f.close();
        }
    }

    public static String getPath(Uri uri, Context context) {
        String[] projection = {MediaStore.Images.Media.DATA};
        String path;
        try {
            Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
            cursor.moveToFirst();
            path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        } catch (Exception e) {
            path = uri.getPath();
        }
        return path;
    }

    public static void saveImage(Bitmap finalBitmap, String d, Context context) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Download");
        myDir.mkdirs();
        String fname = d + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        //context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
        context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Toast.makeText(context, context.getResources().getString(R.string.save_image), Toast.LENGTH_SHORT).show();
    }

    public static Bitmap decodeFile(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 200;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static Bitmap decodeFileWithExif(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 150;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

            try {
                ExifInterface exif = new ExifInterface(f.getAbsolutePath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                if (orientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                } else if (orientation == 3) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                } else if (orientation == 8) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return image;
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static Bitmap decodeFileWithExif2(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            int REQUIRED_SIZE = 300;

            //Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while (o.outWidth / scale / 2 >= REQUIRED_SIZE && o.outHeight / scale / 2 >= REQUIRED_SIZE)
                scale *= 2;

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap image = BitmapFactory.decodeStream(new FileInputStream(f), null, o2);

            try {
                ExifInterface exif = new ExifInterface(f.getPath());
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

                if (orientation == 6) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                } else if (orientation == 3) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(180);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                } else if (orientation == 8) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(270);
                    image = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return image;
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    public static boolean checkUnlocked() {
        boolean unlocked = true;

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/Android/data/com.klinker.android/");
        dir.mkdirs();
        File file = new File(dir, "messaging_expires.txt");

        if (file.exists()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String s = reader.readLine();
                long date = Long.parseLong(s);

                if (Calendar.getInstance().getTimeInMillis() > date) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception e) {
            }
        } else {
            try {
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);

                pw.println(Calendar.getInstance().getTimeInMillis() + (TRIAL_LENGTH * 24 * 60 * 60 * 1000));

                pw.flush();
                pw.close();
                f.close();

                return true;
            } catch (Exception e) {
            }
        }

        return unlocked;
    }

    public static ArrayList<String> readNewMessages(Context context) {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            InputStream inputStream = context.openFileInput("newMessages.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    ret.add(receiveString);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }

        return ret;
    }

    public static void writeNewMessages(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("newMessages.txt", Context.MODE_PRIVATE));

            for (int i = 0; i < data.size(); i++) {
                outputStreamWriter.write(data.get(i) + "\n");
            }

            outputStreamWriter.close();
        } catch (Exception e) {
        }
    }

    public static ArrayList<String> readNotifications(Context context) {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            InputStream inputStream = context.openFileInput("notifications.txt");

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    ret.add(receiveString);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static void writeNotifications(ArrayList<String> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("notifications.txt", Context.MODE_PRIVATE));

            for (int i = 0; i < data.size(); i++) {
                outputStreamWriter.write(data.get(i) + "\n");
            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    @SuppressWarnings("resource")
    public static ArrayList<IndividualSetting> readIndividualNotifications(Context context) {

        ArrayList<IndividualSetting> ret = new ArrayList<IndividualSetting>();

        try {
            InputStream inputStream;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt");
            } else {
                inputStream = context.openFileInput("individualNotifications.txt");
            }

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    ret.add(new IndividualSetting(receiveString, Integer.parseInt(bufferedReader.readLine()), bufferedReader.readLine(), bufferedReader.readLine()));
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    @SuppressWarnings("resource")
    public static ArrayList<BlacklistContact> readBlacklist(Context context) {

        ArrayList<BlacklistContact> ret = new ArrayList<BlacklistContact>();

        try {
            InputStream inputStream;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt");
            } else {
                inputStream = context.openFileInput("blacklist.txt");
            }

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    ret.add(new BlacklistContact(receiveString, Integer.parseInt(bufferedReader.readLine())));
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    @SuppressWarnings("resource")
    public static ArrayList<String> readTemplates(Context context) {

        ArrayList<String> ret = new ArrayList<String>();

        try {
            InputStream inputStream;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt");
            } else {
                inputStream = context.openFileInput("templates.txt");
            }

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {
                    ret.add(receiveString);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static void writeBlacklist(ArrayList<BlacklistContact> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt"));
            } else {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("blacklist.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++) {
                BlacklistContact write = data.get(i);

                outputStreamWriter.write(write.name + "\n" + write.type + "\n");
            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    public static void writeIndividualNotifications(ArrayList<IndividualSetting> data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt"));
            } else {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("individualNotifications.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++) {
                IndividualSetting write = data.get(i);

                outputStreamWriter.write(write.name + "\n" + write.color + "\n" + write.vibratePattern + "\n" + write.ringtone + "\n");
            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    @SuppressWarnings("resource")
    public static ArrayList<String[]> readScheduledSMS(Context context) {

        ArrayList<String[]> ret = new ArrayList<String[]>();

        try {
            InputStream inputStream;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt");
            } else {
                inputStream = context.openFileInput("scheduledSMS.txt");
            }

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {

                    String[] details = new String[5];
                    details[0] = receiveString;

                    for (int i = 1; i < 5; i++)
                        details[i] = bufferedReader.readLine();

                    ret.add(details);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static void writeScheduledSMS(ArrayList<String[]> data, Context context) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt"));
            } else {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("scheduledSMS.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++) {
                String[] details = data.get(i);

                for (int j = 0; j < 5; j++) {
                    outputStreamWriter.write(details[j] + "\n");
                }

            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    @SuppressWarnings("resource")
    public static ArrayList<String[]> readScheduledSMS2(Context context, boolean tryRemove) {

        ArrayList<String[]> ret = new ArrayList<String[]>();

        if (tryRemove)
            ScheduledSms.removeOld();

        try {
            InputStream inputStream;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/scheduledSMS.txt");
            } else {
                inputStream = context.openFileInput("scheduledSMS.txt");
            }


            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";

                while ((receiveString = bufferedReader.readLine()) != null) {

                    String[] details = new String[5];
                    details[0] = receiveString;

                    for (int i = 1; i < 5; i++)
                        details[i] = bufferedReader.readLine();

                    ret.add(details);
                }

                inputStream.close();
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static void writeTemplates(ArrayList<String> data, Context context) {
        try {

            OutputStreamWriter outputStreamWriter;

            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean("save_to_external", true)) {
                outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt"));
            } else {
                outputStreamWriter = new OutputStreamWriter(context.openFileOutput("templates.txt", Context.MODE_PRIVATE));
            }

            for (int i = 0; i < data.size(); i++) {
                outputStreamWriter.write(data.get(i) + "\n");
            }

            outputStreamWriter.close();
        } catch (IOException e) {

        }

    }

    public static void writeTheme(String data, String name) {
        String[] data2 = data.split("\n");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", name);
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);

            for (int i = 0; i < data2.length; i++) {
                pw.println(data2[i]);
            }

            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {

        }

    }

    public static String readTheme(String fileName) {

        String ret = "";

        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", fileName);
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String s = "";

            while ((s = reader.readLine()) != null) {
                ret += s + "\n";
            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static boolean loadSharedPreferencesFromFile(File src, Context context) {
        boolean res = false;
        ObjectInputStream input = null;

        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEdit.clear();

            @SuppressWarnings("unchecked")
            Map<String, ?> entries = (Map<String, ?>) input.readObject();

            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean) {
                    prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
                } else if (v instanceof Float) {
                    prefEdit.putFloat(key, ((Float) v).floatValue());
                } else if (v instanceof Integer) {
                    prefEdit.putInt(key, ((Integer) v).intValue());
                } else if (v instanceof Long) {
                    prefEdit.putLong(key, ((Long) v).longValue());
                } else if (v instanceof String) {
                    prefEdit.putString(key, ((String) v));
                }
            }

            prefEdit.commit();

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (Exception e) {

            }
        }

        return res;
    }

    public static boolean saveSharedPreferencesToFile(File dst, Context context) {
        boolean res = false;
        ObjectOutputStream output = null;

        try {
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

            output.writeObject(pref.getAll());

            res = true;
        } catch (Exception e) {

        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (Exception e) {

            }
        }

        return res;
    }

    public static void writePopupTheme(String data, String name) {
        String[] data2 = data.split("\n");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", name);
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);

            for (int i = 0; i < data2.length; i++) {
                pw.println(data2[i]);
            }

            pw.flush();
            pw.close();
            f.close();
        } catch (Exception e) {

        }

    }

    public static String readPopupTheme(String fileName) {

        String ret = "";

        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", fileName);
            @SuppressWarnings("resource")
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String s = "";

            while ((s = reader.readLine()) != null) {
                ret += s + "\n";
            }

        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return ret;
    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor query = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = query.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            query.moveToFirst();
            return query.getString(column_index);
        } catch (Exception e) {
            return "";
        }
    }

    public static String[] parseVCard(Context context, Uri uri) {
        try {
            VCardParser parser = new VCardParser();
            VDataBuilder builder = new VDataBuilder();

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File(IOUtil.getRealPathFromURI(context, uri)))));

            String vcardString = "";
            String line;
            while ((line = reader.readLine()) != null) {
                vcardString += line + "\n";
            }
            reader.close();

            parser.parse(vcardString, "UTF-8", builder);
            List<VNode> pimContacts = builder.vNodeList;

            for (VNode contact : pimContacts) {
                ArrayList<PropertyNode> props = contact.propList;

                String name = null;
                String number = null;
                for (PropertyNode prop : props) {
                    if ("FN".equals(prop.propName)) {
                        name = prop.propValue;
                    } else if ("TEL".equals(prop.propName)) {
                        number = prop.propValue;
                    }
                }

                return new String[] {name, number};
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new String[] {"", ""};
    }
}