package com.klinker.android.messaging_sliding;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.klinker.android.messaging_donate.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class ImageViewer extends Activity {

    public String[] images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.image_viewer);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

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
        ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

        if (sharedPrefs.getBoolean("dark_theme", false)) {
            if (sharedPrefs.getBoolean("pitch_black_theme", false)) {
                String titleColor = sharedPrefs.getString("title_color", "blue");

                if (titleColor.equals("blue")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_blue));
                } else if (titleColor.equals("orange")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_orange));
                } else if (titleColor.equals("red")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_red));
                } else if (titleColor.equals("green")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_green));
                } else if (titleColor.equals("purple")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_purple));
                } else if (titleColor.equals("grey")) {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_grey));
                } else {
                    ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_black));
                }
            } else {
                ab.setBackgroundDrawable(getResources().getDrawable(R.drawable.dark_action_bar));
            }
        }

        String image = this.getIntent().getBundleExtra("bundle").getString("image", "");
        images = image.split(" ");

        ListView v1 = (ListView) findViewById(R.id.listView1);
        ImageArrayAdapter adapter = new ImageArrayAdapter(this, images);
        v1.setAdapter(adapter);
        v1.setDividerHeight(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_image_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.save_images:
                for (int i = 0; i < images.length; i++) {
                    try {
                        Random rnd = new Random();
                        int num = (int) (((double) rnd.nextDouble()) * 1000);
                        saveImage(MediaStore.Images.Media.getBitmap(this.getContentResolver(), Uri.parse(images[i])), num + "");
                    } catch (FileNotFoundException e1) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                        e1.printStackTrace();
                    } catch (IOException e1) {
                        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
                        e1.printStackTrace();
                    }
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void saveImage(Bitmap finalBitmap, String d) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/Download");
        myDir.mkdirs();
        String fname = d + ".jpg";
        File file = new File(myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        Toast.makeText(this, this.getResources().getString(R.string.save_images), Toast.LENGTH_SHORT).show();
    }

}
