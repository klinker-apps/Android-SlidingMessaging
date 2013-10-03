package com.klinker.android.messaging_sliding;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.google.android.mms.MMSPart;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;

import java.io.*;
import java.util.ArrayList;

public class AttachMore extends Activity {

    public static ArrayList<MMSPart> data = new ArrayList<MMSPart>();
    public static ArrayList<Bitmap> images = new ArrayList<Bitmap>();
    public ListView list;
    public SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_viewer);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2")) {
            setTheme(android.R.style.Theme_Holo);
            getWindow().getDecorView().setBackgroundColor(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver)));
        } else if (sharedPrefs.getString("run_as", "sliding").equals("card")) {
            if (sharedPrefs.getString("card_theme", "Light").equals("Light")) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_message_list_back));
                setTheme(android.R.style.Theme_Holo_Light);
            } else if (sharedPrefs.getString("card_theme", "Light").equals("Dark")) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_dark_message_list_back));
                setTheme(android.R.style.Theme_Holo);
            } else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black")) {
                getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_black_message_list_back));
                setTheme(android.R.style.Theme_Holo);
            }
        }

        ActionBar ab = getActionBar();
        ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_action_bar));

        list = (ListView) findViewById(R.id.listView1);
        AttachMoreArrayAdapter adapter = new AttachMoreArrayAdapter(this, data.toArray(new MMSPart[data.size()]));
        list.setAdapter(adapter);
        list.setDividerHeight(0);

        final Context context = this;

        list.setOnItemLongClickListener(new OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
                                           final int arg2, long arg3) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage(context.getResources().getString(R.string.remove_attachment));
                builder.setPositiveButton(context.getResources().getString(R.string.yes), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        data.remove(arg2);
                        AttachMoreArrayAdapter adapter = new AttachMoreArrayAdapter((Activity) context, data.toArray(new MMSPart[data.size()]));
                        list.setAdapter(adapter);
                    }

                });
                builder.setNegativeButton(context.getResources().getString(R.string.no), new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }

                });

                builder.create().show();

                return false;
            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.attach_more, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final Context context = this;

        switch (item.getItemId()) {
            case R.id.menu_attach:
                AlertDialog.Builder attachBuilder = new AlertDialog.Builder(this);
                attachBuilder.setItems(R.array.attach_more_options, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        switch (arg1) {
                            case 0:
                                Intent getImage = new Intent();
                                getImage.setType("image/*");
                                getImage.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(Intent.createChooser(getImage, getResources().getString(R.string.select_picture)), 1);

                                break;
                            case 1:
                                Intent getVideo = new Intent();
                                getVideo.setType("video/*");
                                getVideo.setAction(Intent.ACTION_GET_CONTENT);
                                //startActivityForResult(Intent.createChooser(getVideo, getResources().getString(R.string.select_video)), 2); // TODO
                                Toast.makeText(context, "Currently Unsupported", Toast.LENGTH_SHORT).show();
                                break;
                            case 2:
                                Intent getAudio = new Intent();
                                getAudio.setType("audio/*");
                                getAudio.setAction(Intent.ACTION_GET_CONTENT);
                                //startActivityForResult(Intent.createChooser(getAudio, getResources().getString(R.string.select_audio)), 3); // TODO
                                Toast.makeText(context, "Currently Unsupported", Toast.LENGTH_SHORT).show();
                                break;
                        }

                    }

                });

                attachBuilder.create().show();

                return true;
            case R.id.menu_done:
                Intent i = getIntent();
                setResult(RESULT_OK, i);
                finish();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                Uri image = imageReturnedIntent.getData();
                Bitmap b = decodeFile2(new File(getPath(image)));
                images.add(b);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                b.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                MMSPart part = new MMSPart();
                part.Name = "Image";
                part.MimeType = "image/png";
                part.Data = byteArray;

                data.add(part);

                AttachMoreArrayAdapter adapter = new AttachMoreArrayAdapter(this, data.toArray(new MMSPart[data.size()]));
                list.setAdapter(adapter);
            }
        } else if (requestCode == 2) {
            if (resultCode == Activity.RESULT_OK) {
                Uri video = imageReturnedIntent.getData();
                String path = getPath(video);
                byte[] bytes = null;

                try {
                    bytes = IOUtil.readFile(path);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                MMSPart part = new MMSPart();
                part.Name = "Video";
                part.MimeType = "video/3gp";
                part.Data = bytes;
                part.Path = video;

                data.add(part);

                AttachMoreArrayAdapter adapter = new AttachMoreArrayAdapter(this, data.toArray(new MMSPart[data.size()]));
                list.setAdapter(adapter);
            }
        } else if (requestCode == 3) {
            if (resultCode == Activity.RESULT_OK) {

            }
        } else {

        }
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String path = cursor.getString(column_index);
        cursor.close();
        return path;
    }

    private Bitmap decodeFile2(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            int REQUIRED_SIZE = 300;

            if (!sharedPrefs.getBoolean("limit_attachment_size", true)) {
                REQUIRED_SIZE = 500;
            }

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
                    matrix.postRotate(2700);
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
}