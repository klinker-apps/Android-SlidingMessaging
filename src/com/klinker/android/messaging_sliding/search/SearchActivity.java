package com.klinker.android.messaging_sliding.search;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;

import java.util.Calendar;

/**
 * Created by luke on 7/23/13.
 */
public class SearchActivity extends Activity {

    public final static String EXTRA_QUERY = "com.klinker.android.messaging_sliding.QUERY";

    String query;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        getActionBar().hide();

        handleIntent(getIntent());

        TextView tv = (TextView) findViewById(R.id.searchText);
        tv.setText(query);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        query = intent.getStringExtra("query");
    }
}
