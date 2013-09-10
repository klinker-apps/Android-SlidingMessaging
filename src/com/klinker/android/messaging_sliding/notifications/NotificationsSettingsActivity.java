package com.klinker.android.messaging_sliding.notifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class NotificationsSettingsActivity extends Activity {
	
	public static Context context;
	public ListView templates;
	public Button addNew;
	public SharedPreferences sharedPrefs;
	public ArrayList<IndividualSetting> individuals;
	public ArrayList<String> names;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.templates);
		templates = (ListView) findViewById(R.id.templateListView2);
		findViewById(R.id.templateListView).setVisibility(View.GONE);
		addNew = (Button) findViewById(R.id.addNewButton);
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		context = this;
		
		individuals = readFromFile(this);
		names = new ArrayList<String>();
		
		for (int i = 0; i < individuals.size(); i++)
		{
			names.add(individuals.get(i).name);
		}
		
		TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
		templates.setAdapter(adapter);
		templates.setStackFromBottom(false);
		
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
		
		templates.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				new AlertDialog.Builder(context)
					.setMessage(context.getResources().getString(R.string.delete_contact_settings))
					.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            individuals.remove(arg2);
				            names.remove(arg2);
				            
				            TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, names);
				    		templates.setAdapter(adapter);
				    		
				    		writeToFile(individuals, context);
				        }
				    }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            
				        }
				    }).show();
				return false;
			}
			
		});
		
		templates.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("temp_ringtone", individuals.get(arg2).ringtone);
				editor.putInt("temp_led_color", individuals.get(arg2).color);
				editor.putString("temp_vibrate_pattern", individuals.get(arg2).vibratePattern);
				editor.commit();
				
				Intent intent = new Intent(context, NotificationSetterActivity.class);
				intent.putExtra("com.klinker.android.messaging.CONTACT_NAME", individuals.get(arg2).name);
				
				individuals.remove(arg2);
				writeToFile(individuals, context);
				startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
			}
			
		});
		
		addNew.setText(getResources().getString(R.string.add_new_individual));
		
		addNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				writeToFile(individuals, context);
				Intent intent = new Intent(context, ContactFinderActivity.class);
				context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
				
			}
			
		});

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
	public void onResume()
	{
		super.onResume();
		
		individuals = readFromFile(this);
		names = new ArrayList<String>();
		
		for (int i = 0; i < individuals.size(); i++)
		{
			names.add(individuals.get(i).name);
		}
		
		TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
		templates.setAdapter(adapter);
		templates.setStackFromBottom(false);
	}
	
	@Override
	public void onBackPressed() {
		writeToFile(individuals, this);
		super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
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
	  	
	  	private void writeToFile(ArrayList<IndividualSetting> data, Context context) {
	        try {
	            OutputStreamWriter outputStreamWriter;
	            
	            if (sharedPrefs.getBoolean("save_to_external", true))
	            {
	            	outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/individualNotifications.txt"));
	            } else
	            {
	            	outputStreamWriter = new OutputStreamWriter(context.openFileOutput("individualNotifications.txt", Context.MODE_PRIVATE));
	            }
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	IndividualSetting write = data.get(i);
	            	
	            	outputStreamWriter.write(write.name + "\n" + write.color + "\n" + write.vibratePattern + "\n" + write.ringtone + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
}