package com.klinker.android.messaging_donate;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;

/**
 * Created by lucasklinker on 5/27/16.
 */
public class PermissionActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.READ_SMS,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                },
                51);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        startActivity(new Intent(this, MainActivity.class));
        finish();

        overridePendingTransition(0,0);
    }
}
