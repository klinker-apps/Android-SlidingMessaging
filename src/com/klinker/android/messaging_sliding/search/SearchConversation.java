package com.klinker.android.messaging_sliding.search;

import android.app.ActionBar;
import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.MainActivity;

import java.util.ArrayList;
import java.util.Collections;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;

/**
 * Created by luke on 7/24/13.
 */
public class SearchConversation extends Activity implements PullToRefreshAttacher.OnRefreshListener{

    public SharedPreferences sharedPrefs;
    private ArrayList<String[]> messages;
    public String searchQuery;
    public String threadId = "";

    public PullToRefreshAttacher mPullToRefreshAttacher;

    public ListView lv;
    public ConversationArrayAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsTheme);
        }

        getWindow().setBackgroundDrawable(new ColorDrawable(sharedPrefs.getInt("ct_messageListBackground", getResources().getColor(R.color.light_silver))));

        setContentView(R.layout.activity_search);

        final ActionBar actionBar = getActionBar();

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

        searchQuery = getIntent().getStringExtra("search");
        messages = fillList(getIntent().getStringExtra("id"));

        int i = 0;

        while(messages.get(i)[3].equals("1"))
            i++;

        String contactName = MainActivity.loadGroupContacts(messages.get(i)[0], this);

        actionBar.setTitle(contactName + " - Search");
        actionBar.setSubtitle(messages.get(i)[0]);

        lv = (ListView) findViewById(R.id.searchList);
        adapter = new ConversationArrayAdapter(this, messages, searchQuery);
        lv.setAdapter(adapter);
        lv.setStackFromBottom(true);

        lv.setDivider(new ColorDrawable(sharedPrefs.getInt("ct_messageDividerColor", getResources().getColor(R.color.light_silver))));

        if (sharedPrefs.getBoolean("ct_messageDividerVisibility", true) && sharedPrefs.getString("run_as", "sliding").equals("sliding"))
        {
            lv.setDividerHeight(1);
        } else
        {
            lv.setDividerHeight(0);
        }

        mPullToRefreshAttacher = new PullToRefreshAttacher(this, sharedPrefs.getBoolean("ct_light_action_bar", false));
        mPullToRefreshAttacher.setRefreshableView(lv, this);
    }

    @Override
    public void onRefreshStarted(View view) {
        /**
         * Simulate Refresh with 4 seconds sleep
         */
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Uri uri = Uri.parse("content://sms/conversations/" + threadId);
                Cursor c = getContentResolver().query(uri, null, null ,null, "date DESC");

                int i;

                messages.clear();

                if(c.moveToLast()){
                    for(i=0;i<c.getCount();i++){

                        String[] data = new String[6];
                        data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                        data[1] = c.getString(c.getColumnIndexOrThrow("body"));
                        data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                        data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                        data[4] = "false";

                        messages.add(data);

                        c.moveToPrevious();
                    }
                }
                c.close();

                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                ConversationArrayAdapter adapter = new ConversationArrayAdapter(getActivity(), messages, searchQuery);
                lv.setAdapter(adapter);
                lv.setStackFromBottom(true);

                // Notify PullToRefreshAttacher that the refresh has finished
                mPullToRefreshAttacher.setRefreshComplete();
            }
        }.execute();
    }

    public Activity getActivity()
    {
        return this;
    }

    public ArrayList<String[]> fillList(String id)
    {
        ArrayList<String[]> messages = new ArrayList<String[]>();

        Uri uri ;
        Cursor c;
        uri = Uri.parse("content://sms");
        c = getContentResolver().query(uri, null, null ,null, "date DESC");

        String messageId = "";
        int i = 0;

        if(c.moveToFirst()){
            for(i=0;i<c.getCount();i++){

                messageId = c.getString(c.getColumnIndexOrThrow("_id"));

                if (messageId.equals(id))
                {
                    threadId = c.getString(c.getColumnIndexOrThrow("thread_id"));
                    break;
                }

                c.moveToNext();
            }
        }

        c.close();

        uri = Uri.parse("content://sms/conversations/" + threadId);
        c = getContentResolver().query(uri, null, null ,null, "date DESC");

        if(c.moveToFirst()){
            for(i=0;i<c.getCount();i++){

                messageId = c.getString(c.getColumnIndexOrThrow("_id"));

                if (messageId.equals(id))
                {
                    break;
                }

                c.moveToNext();
            }

            int maxLength = c.getCount();
            /* 4 Possibilities

            1.) you can't go back 10 messages and you can't go forward 10 messages
            2.) you can't go back 10 messages but you can go forward 10 messages
            3.) you can go back 10 messages but you can't go forward 10 messages
            4.) you can go back 10 messages and yo can go forward 10 messages

            */

            if (i > maxLength - 10 && i < 10) // case 1
            {
                c.moveToLast();
                for(int x = maxLength; x > 0; x--)
                {
                    String[] data = new String[6];
                    data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                    data[1] = c.getString(c.getColumnIndexOrThrow("body"));
                    data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                    data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                    data[4] = "false";

                    messages.add(data);

                    c.moveToPrevious();
                }
            } else if (i > maxLength - 10 && i > 10) // case 2
            {
                c.moveToLast();
                for (int x = maxLength; x > i - 10; x--)
                {
                    String[] data = new String[6];
                    data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                    data[1] = c.getString(c.getColumnIndexOrThrow("body"));
                    data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                    data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                    data[4] = "false";

                    messages.add(data);

                    c.moveToPrevious();
                }
            } else if (i < maxLength - 10 && i < 10) // case 3
            {
                c.move(i + 9);
                for (int x = i + 10; x > 0; x--)
                {
                    String[] data = new String[6];
                    data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                    data[1] = c.getString(c.getColumnIndexOrThrow("body"));
                    data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                    data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                    data[4] = "false";

                    messages.add(data);

                    c.moveToPrevious();
                }
            } else if (i < maxLength - 10 && i > 10) // case 4
            {
                for (int x = 0; x < 10; x++)
                    c.moveToNext();

                for (int x = i + 10; x > i - 10; x--)
                {
                    String[] data = new String[6];
                    data[0] = c.getString(c.getColumnIndexOrThrow("address"));
                    data[1] = c.getString(c.getColumnIndexOrThrow("body"));
                    data[2] = c.getString(c.getColumnIndexOrThrow("date"));
                    data[3] = c.getString(c.getColumnIndexOrThrow("type"));
                    data[4] = "false";

                    messages.add(data);

                    c.moveToPrevious();
                }
            }
        }
        c.close();

        return messages;
    }
}
