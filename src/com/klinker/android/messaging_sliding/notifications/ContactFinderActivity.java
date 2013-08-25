package com.klinker.android.messaging_sliding.notifications;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class ContactFinderActivity extends Activity {
	
	public static Context context;
	public ListView contacts;
	public SharedPreferences sharedPrefs;
	public ArrayList<String> name;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.custom_fonts);
		contacts = (ListView) findViewById(R.id.fontListView);
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		context = this;
		
		name = new ArrayList<String>();
		
		findContacts();
		contacts.setAdapter(new TemplateArrayAdapter((Activity) context, name));
		
		contacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				ArrayList<IndividualSetting> individuals = readFromFile(context);
				boolean flag = false;
				int pos = 0;
				
				for (int i = 0; i < individuals.size(); i++)
				{
					if (individuals.get(i).name.equals(name.get(arg2)))
					{
						flag = true;
						pos = i;
						break;
					}
				}
				
				if (!flag)
				{
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putString("temp_ringtone", "content://settings/system/notification_sound");
					editor.putInt("temp_led_color", context.getResources().getColor(R.color.white));
					editor.putString("temp_vibrate_pattern", "0L, 400L, 100L, 400L");
					editor.commit();
				} else
				{
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putString("temp_ringtone", individuals.get(pos).ringtone);
					editor.putInt("temp_led_color", individuals.get(pos).color);
					editor.putString("temp_vibrate_pattern", individuals.get(pos).vibratePattern);
					editor.commit();
				}
				
				Intent intent = new Intent(context, NotificationSetterActivity.class);
				intent.putExtra("com.klinker.android.messaging.CONTACT_NAME", name.get(arg2));
				startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
				
				finish();
			}
			
		});
		
		if (sharedPrefs.getBoolean("override_lang", false))
		{
			String languageToLoad  = "en";
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		} else
		{
			String languageToLoad = Resources.getSystem().getConfiguration().locale.getLanguage();
		    Locale locale = new Locale(languageToLoad); 
		    Locale.setDefault(locale);
		    Configuration config = new Configuration();
		    config.locale = locale;
		    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
		}
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
	
	public void findContacts()
	{
		Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
		String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME};

		Cursor people = getContentResolver().query(uri, projection, null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " asc");
		int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

		if (people.moveToFirst())
		{
			do {
			    name.add(people.getString(indexName));
			} while (people.moveToNext());
		}
	}
	
	@SuppressWarnings("resource")
	private ArrayList<IndividualSetting> readFromFile(Context context) {
		
	      ArrayList<IndividualSetting> ret = new ArrayList<IndividualSetting>();
	      
	      try {
	          InputStream inputStream;
	          
	          if (sharedPrefs.getBoolean("save_to_external", true))
	          {
	         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt");
	          } else
	          {
	        	  inputStream = context.openFileInput("individualNotifications.txt");
	          }
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null) {
	          		ret.add(new IndividualSetting(receiveString, Integer.parseInt(bufferedReader.readLine()), bufferedReader.readLine(), bufferedReader.readLine()));
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
}