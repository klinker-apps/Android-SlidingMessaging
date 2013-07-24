package com.klinker.android.messaging_sliding.search;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by luke on 7/23/13.
 */
public class SearchActivity extends FragmentActivity {

    public String searchQuery;
    public ArrayList<String[]> messages;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        final View customActionBarView = inflater.inflate(
                R.layout.search_menu, null);

        SearchView search = (SearchView) customActionBarView.findViewById(R.id.menu_search);
        search.setQueryHint("New Search");
        search.setIconifiedByDefault(false);
        search.setSubmitButtonEnabled(false);

        // Show the custom action bar view and hide the normal Home icon and title.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(
                ActionBar.DISPLAY_SHOW_CUSTOM,
                ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                        | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        searchQuery = handleIntent(getIntent());

        // TODO - might want to make it so it shows progress dialog while searching, saw something about an async task to do this. will look into it later

        messages = fillMessagesExample(); // this will just be a filler one with a dummy arraylist with dummy values
        //messages = fillMessages(searchQuery); // TODO - read in the messages with the search text into arraylist

        // Return as an arraylist of string arrays
        // [0] is the name or number, don't care which you get me, will be changed to name eventually anyways
        // [1] is the message/body
        // [2] is the date
        // [3] is the type (1 for sent, 0 for recieved)
        // [4] tells if it has a picture (true if it does, false if it doesn't)
        // Might think of more i need later, but this is it for now

    }

    public ArrayList<String[]> fillMessages(String text)
    {
        return null;
    }

    public ArrayList<String[]> fillMessagesExample()
    {
        ArrayList<String[]> messages = new ArrayList<String[]>();

        for(int i = 0; i < 5; i++)
        {
            String[] data = new String[5];
        }

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
}
