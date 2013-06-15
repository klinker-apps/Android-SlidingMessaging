package com.klinker.android.messaging_donate.widget;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.MainActivity;

public class WidgetProxyActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        final Intent activity = new Intent(this, MainActivity.class);
        activity.setAction(Intent.ACTION_SENDTO);
        activity.setData(getIntent().getData());
        activity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                startActivity(activity);
                finish();
            }
        }, 500);


    }
}
