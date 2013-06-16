package com.klinker.android.messaging_card.group;

import com.klinker.android.messaging_donate.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

public class GroupActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_list_card);
		
		if (sharedPrefs.getString("card_theme", "Light").equals("Light"))
		{
			setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
			getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_message_list_back));
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Dark"))
		{
			setTheme(android.R.style.Theme_Holo_NoActionBar);
			getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_dark_message_list_back));
		} else if (sharedPrefs.getString("card_theme", "Light").equals("Pitch Black"))
		{
			setTheme(android.R.style.Theme_Holo_NoActionBar);
			getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.card_black_message_list_back));
		}
		
		Intent intent = getIntent();
		Bundle b = intent.getExtras();
		String names = b.getString("names");
		String numbers = b.getString("numbers");
		
		ListView contactList = (ListView) findViewById(R.id.messageListView);
		GroupArrayAdapter adapter = new GroupArrayAdapter(this, names, numbers);
		contactList.setAdapter(adapter);
	}

}
