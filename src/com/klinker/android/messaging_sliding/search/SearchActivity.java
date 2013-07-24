package com.klinker.android.messaging_sliding.search;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;
import com.klinker.android.messaging_sliding.scheduled.SchedulesArrayAdapter;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by luke on 7/23/13.
 */
public class SearchActivity extends FragmentActivity {

    public String searchQuery;
    public ArrayList<String[]> messages;
    public SharedPreferences sharedPrefs;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsTheme);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver))));

        setContentView(R.layout.activity_search);

        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.search_menu, null);

        final SearchView search = (SearchView) customActionBarView.findViewById(R.id.menu_search);
        search.setQueryHint("New Search");
        search.setIconifiedByDefault(false);
        search.setSubmitButtonEnabled(false);
        //search.requestFocusFromTouch();

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                String query = search.getQuery().toString();

                Intent intent = new Intent(getBaseContext(), SearchActivity.class);
                intent.putExtra("query", query);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                finish();
                overridePendingTransition(0, 0);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        actionBar.setDisplayHomeAsUpEnabled(true);

        if (!sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.black)));

            if (sharedPrefs.getInt("ct_conversationListBackground", getResources().getColor(R.color.light_silver)) == getResources().getColor(R.color.pitch_black))
            {
                if (!sharedPrefs.getBoolean("hide_title_bar", true))
                {
                    actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.pitch_black_action_bar_blue));
                } else
                {
                    actionBar.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.pitch_black)));
                }
            }
        } else
        {
            actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_hangouts));
        }

        searchQuery = handleIntent(getIntent());

        // TODO - might want to make it so it shows progress dialog while searching, saw something about an async task to do this. will look into it later

        //messages = fillMessagesExample(); // this will just be a filler one with a dummy arraylist with dummy values
        messages = fillMessages(searchQuery); // TODO - read in the messages with the search text into arraylist

        // Return as an arraylist of string arrays
        // [0] is the name or number, don't care which you get me, will be changed to name eventually anyways
        // [1] is the message/body
        // [2] is the date
        // [3] is the type (1 for sent, 0 for recieved)
        // [4] tells if it has a picture (true if it does, false if it doesn't)
        // Might think of more i need later, but this is it for now

        ListView lv = (ListView) findViewById(R.id.searchList);
        SearchArrayAdapter adapter = new SearchArrayAdapter(this, messages);
        lv.setAdapter(adapter);

        lv.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_messageDividerColor", getResources().getColor(R.color.light_silver))));

        if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true) && sharedPrefs.getString("run_as", "sliding").equals("sliding"))
        {
            lv.setDividerHeight(1);
        } else
        {
            lv.setDividerHeight(0);
        }

    }

    public ArrayList<String[]> fillMessages(String text)
    {
        ArrayList<String[]> messages = new ArrayList<String[]>();

        Uri uri ;
        Cursor c;
        uri = Uri.parse("content://sms");
        c = getContentResolver().query(uri, null, null ,null, "date DESC");

        String body;

        if(c.moveToFirst()){
            for(int i=0;i<c.getCount();i++){

                //if (c.getString(c.getColumnIndexOrThrow("msg_box")) != null)
                //{

                //} else
                //{
                    body = c.getString(c.getColumnIndexOrThrow("body"));

                    if (body.contains(text))
                    {
                        String[] data = new String[5];
                        data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                        data[1] = body;
                        data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                        data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                        data[4] = "false";

                        messages.add(data);
                    }
                //}


                c.moveToNext();
            }
        }
        c.close();

        return messages;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private String handleIntent(Intent intent) {
        return intent.getStringExtra("query");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}
