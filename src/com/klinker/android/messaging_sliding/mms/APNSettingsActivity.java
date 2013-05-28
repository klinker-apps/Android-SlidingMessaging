package com.klinker.android.messaging_sliding.mms;

import java.util.ArrayList;

import com.klinker.android.messaging_donate.R;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

public class APNSettingsActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_fonts);
		
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		
		for (int i = 0; i < PresetAPNs.apns.length; i++)
		{
			String[] apn = PresetAPNs.apns[i].split("--");
			names.add(apn[0]);
			values.add(apn[1]);
		}
		
		ListView apns = (ListView) findViewById(R.id.fontListView);
		APNArrayAdapter adapter = new APNArrayAdapter(this, names, values);
		apns.setAdapter(adapter);
	}

}
