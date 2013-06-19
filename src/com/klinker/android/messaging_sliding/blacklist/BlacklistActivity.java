package com.klinker.android.messaging_sliding.blacklist;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneNumberUtils;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.templates.TemplateArrayAdapter;

public class BlacklistActivity extends Activity {
	
	public static Context context;
	public ListView contacts;
	public Button addNew;
	public SharedPreferences sharedPrefs;
	public ArrayList<BlacklistContact> individuals;
	public ArrayList<String> names;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.templates);
		contacts = (ListView) findViewById(R.id.templateListView);
		addNew = (Button) findViewById(R.id.addNewButton);
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		context = this;
		
		individuals = readFromFile(this);
		names = new ArrayList<String>();
		
		for (int i = 0; i < individuals.size(); i++)
		{
			names.add(findContactName(individuals.get(i).name, this));
		}
		
		TemplateArrayAdapter adapter = new TemplateArrayAdapter(this, names);
		contacts.setAdapter(adapter);
		contacts.setStackFromBottom(false);
		
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
		
		contacts.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int arg2, long arg3) {
				new AlertDialog.Builder(context)
					.setMessage(context.getResources().getString(R.string.delete_blacklist_contact))
					.setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            individuals.remove(arg2);
				            names.remove(arg2);
				            
				            TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, names);
				    		contacts.setAdapter(adapter);
				    		
				    		writeToFile(individuals, context);
				        }
				    }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
				        public void onClick(DialogInterface dialog, int whichButton) {
				            
				        }
				    }).show();
				return false;
			}
			
		});
		
		contacts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				writeToFile(individuals, context);
				Intent intent = new Intent(context, NewBlacklistActivity.class);
				intent.putExtra("com.klinker.android.messaging.BLACKLIST_NAME", individuals.get(arg2).name);
				intent.putExtra("com.klinker.android.messaging.BLACKLIST_TYPE", individuals.get(arg2).type);
				context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
			}
			
		});
		
		addNew.setText(getResources().getString(R.string.add_new_individual));
		
		addNew.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				writeToFile(individuals, context);
				Intent intent = new Intent(context, NewBlacklistActivity.class);
				context.startActivity(intent);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
				
			}
			
		});
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
		contacts.setAdapter(adapter);
		contacts.setStackFromBottom(false);
	}
	
	@Override
	public void onBackPressed() {
		writeToFile(individuals, this);
		super.onBackPressed();
		finish();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
	}
	
	@SuppressWarnings("resource")
	private ArrayList<BlacklistContact> readFromFile(Context context) {
		
	      ArrayList<BlacklistContact> ret = new ArrayList<BlacklistContact>();
	      
	      try {
	    	  InputStream inputStream;
	          
	          if (sharedPrefs.getBoolean("save_to_external", true))
	          {
	         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt");
	          } else
	          {
	        	  inputStream = context.openFileInput("blacklist.txt");
	          }
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null) {
	          		ret.add(new BlacklistContact(receiveString, Integer.parseInt(bufferedReader.readLine())));
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
	  	
	  	private void writeToFile(ArrayList<BlacklistContact> data, Context context) {
	        try {
	        	OutputStreamWriter outputStreamWriter;
	            
	            if (sharedPrefs.getBoolean("save_to_external", true))
	            {
	            	outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/blacklist.txt"));
	            } else
	            {
	            	outputStreamWriter = new OutputStreamWriter(context.openFileOutput("blacklist.txt", Context.MODE_PRIVATE));
	            }
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	BlacklistContact write = data.get(i);
	            	
	            	outputStreamWriter.write(write.name + "\n" + write.type + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}
	  	
	  	public static String findContactName(String number, Context context)
		{
			String name = "";
			
			String origin = number;
			
			if (origin.length() != 0)
			{
				Uri phoneUri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(origin));
				Cursor phonesCursor = context.getContentResolver().query(phoneUri, new String[] {ContactsContract.Contacts.DISPLAY_NAME_PRIMARY, ContactsContract.RawContacts._ID}, null, null, ContactsContract.Contacts.DISPLAY_NAME + " desc limit 1");

				if(phonesCursor != null && phonesCursor.moveToFirst()) {
					name = phonesCursor.getString(0);
				} else
				{
					if (!number.equals(""))
					{
						try
						{
							Locale sCachedLocale = Locale.getDefault();
							int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
							Editable editable = new SpannableStringBuilder(number);
							PhoneNumberUtils.formatNumber(editable, sFormatType);
							name = editable.toString();
						} catch (Exception e)
						{
							name = number;
						}
					} else
					{
						name = "No Information";
					}
				}
				
				phonesCursor.close();
			} else
			{			
				if (!number.equals(""))
				{
					try
					{
						Long.parseLong(number.replaceAll("[^0-9]", ""));
						Locale sCachedLocale = Locale.getDefault();
						int sFormatType = PhoneNumberUtils.getFormatTypeForLocale(sCachedLocale);
						Editable editable = new SpannableStringBuilder(number);
						PhoneNumberUtils.formatNumber(editable, sFormatType);
						name = editable.toString();
					} catch (Exception e)
					{
						name = number;
					}
				} else
				{
					name = "No Information";
				}
			}
			
			return name;
		}
}