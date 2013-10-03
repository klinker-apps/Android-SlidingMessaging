package com.klinker.android.messaging_sliding.mms;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;

import java.util.ArrayList;

public class APNSettingsActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_fonts);

        ArrayList<String> names = new ArrayList<String>();
        ArrayList<String> values = new ArrayList<String>();

        for (int i = 0; i < PresetAPNs.apns.length; i++) {
            String[] apn = PresetAPNs.apns[i].split("--");
            names.add(apn[0]);
            values.add(apn[1]);
        }

        ListView apns = (ListView) findViewById(R.id.fontListView);
        APNArrayAdapter adapter = new APNArrayAdapter(this, names, values);
        apns.setAdapter(adapter);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return super.onOptionsItemSelected(item);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}
