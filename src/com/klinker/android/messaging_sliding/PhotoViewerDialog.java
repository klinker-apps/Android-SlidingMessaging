/*
 * Copyright 2013 Jacob Klinker
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.klinker.android.messaging_sliding;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;
import com.klinker.android.messaging_sliding.views.HoloTextView;
import uk.co.senab.photoview.PhotoViewAttacher;

public class PhotoViewerDialog extends Activity {

    private Context context;
    private String url;
    private String name;
    private ImageView picture;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        }

        url = getIntent().getStringExtra("uri");
        name = getIntent().getStringExtra("name");

        if (url == null) {
            finish();
            return;
        }

        try {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        } catch (Exception e) { }

        setContentView(R.layout.photo_dialog_layout);

        picture = (ImageView) findViewById(R.id.picture);
        PhotoViewAttacher mAttacher = new PhotoViewAttacher(picture);

        picture.setImageURI(Uri.parse(url));

        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                ((Activity)context).finish();
            }
        });

        HoloTextView download = (HoloTextView) findViewById(R.id.download);
        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                IOUtil.saveFile(Uri.parse(url), name, ".jpg", PhotoViewerDialog.this);
                finish();
            }
        });
    }
}