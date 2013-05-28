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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import com.klinker.android.messaging_donate.R;

public class NewBlacklistActivity extends Activity {
	
	public static Context context;
	public ListView contacts;
	public EditText contact;
	public Button save;
	public SharedPreferences sharedPrefs;
	public ArrayList<BlacklistContact> individuals;
	public ArrayList<String> contactNames, contactNumbers, contactTypes;
	
	public boolean firstContactSearch = true;
	
	public int saveType = 1;
	public RadioButton radio1;
	public RadioButton radio2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.blacklist);
		contacts = (ListView) findViewById(R.id.contactSearchListView);
		save = (Button) findViewById(R.id.saveBlacklist);
		contact = (EditText) findViewById(R.id.contactEntry);
		
		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		context = this;
		
		individuals = readFromFile(this);
		
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
		
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				boolean stop = false;
				int pos = 0;
				
				for (int i = 0; i < individuals.size(); i++)
				{
					if (individuals.get(i).name.trim().equals(contact.getText().toString()))
					{
						stop = true;
						pos = i;
						break;
					}
				}
				
				if (!contact.getText().toString().equals("") && stop == false)
				{
					individuals.add(new BlacklistContact(contact.getText().toString(), saveType));
					writeToFile(individuals, context);
					finish();
				} else if (stop == true)
				{
					individuals.set(pos, new BlacklistContact(contact.getText().toString(), saveType));
					writeToFile(individuals, context);
					finish();
				} else
				{
					Toast.makeText(context, "No contact to save.", Toast.LENGTH_SHORT).show();
				}
			}
			
		});
		
		contact.addTextChangedListener(new TextWatcher() {
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        	if (firstContactSearch)
	        	{
	        		try
	        		{
	        			contactNames = new ArrayList<String>();
	        			contactNumbers = new ArrayList<String>();
	        			contactTypes = new ArrayList<String>();
	        			
	        			Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
	        			String[] projection    = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
	        			                ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.LABEL};
	        	
	        			Cursor people = getContentResolver().query(uri, projection, null, null, null);
	        	
	        			int indexName = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
	        			int indexNumber = people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
	        	
	        			people.moveToFirst();
	        			do {
	        				int type = people.getInt(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
	        				String customLabel = people.getString(people.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL));
	        				
	        				if (sharedPrefs.getBoolean("mobile_only", false))
	        				{
	        					if (type == 2)
	        					{
	        						contactNames.add(people.getString(indexName));
	    	        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        						contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        					}
	        				} else
	        				{
	        					contactNames.add(people.getString(indexName));
		        				contactNumbers.add(people.getString(indexNumber).replaceAll("[^0-9\\+]", ""));
	        					contactTypes.add(ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.getResources(), type, customLabel).toString());
	        				}
	        			} while (people.moveToNext());
	        			people.close();
	        		} catch (IllegalArgumentException e)
	        		{
	        			
	        		}
	        		
	        		firstContactSearch = false;
	        	}
	        }

	        @SuppressLint("DefaultLocale")
			public void onTextChanged(CharSequence s, int start, int before, int count) {
	        	ArrayList<String> searchedNames = new ArrayList<String>();
	        	ArrayList<String> searchedNumbers = new ArrayList<String>();
	        	ArrayList<String> searchedTypes = new ArrayList<String>();
	        	
	        	String text = contact.getText().toString();
	        	
	        	String[] text2 = text.split("; ");
	        	
	        	text = text2[text2.length-1];
	        	
	        	if (text.startsWith("+"))
	        	{
	        		text = text.substring(1);
	        	}
	        	
	        	Pattern pattern;
	        	
	        	try
	        	{
	        		pattern = Pattern.compile(text.toLowerCase());
	        	} catch (Exception e)
	        	{
	        		pattern = Pattern.compile(text.toLowerCase().replace("(", "").replace(")", "").replace("?", "").replace("[", "").replace("{", "").replace("}", "").replace("\\", ""));
	        	}
	        	
			    for (int i = 0; i < contactNames.size(); i++)
			    {
			    	try
			    	{
			    		Long.parseLong(text);
			    		
				        if (text.length() <= contactNumbers.get(i).length())
				        {
				        	Matcher matcher = pattern.matcher(contactNumbers.get(i));
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	} catch (Exception e)
			    	{
			    		if (text.length() <= contactNames.get(i).length())
				        {
			    			Matcher matcher = pattern.matcher(contactNames.get(i).toLowerCase());
					        if(matcher.find())
					        {
					        	searchedNames.add(contactNames.get(i));
					        	searchedNumbers.add(contactNumbers.get(i));
					        	searchedTypes.add(contactTypes.get(i));
					        }
				        }
			    	}
			    }
	        	
		        ContactSearchArrayAdapter adapter;
		        
		        if (text.length() != 0)
		        {
	        		adapter = new ContactSearchArrayAdapter((Activity)context, searchedNames, searchedNumbers, searchedTypes);
		        } else
		        {
	        		adapter = new ContactSearchArrayAdapter((Activity)context, new ArrayList<String>(), new ArrayList<String>(), new ArrayList<String>());
		        }
	        	
	        	contacts.setAdapter(adapter);
	        	
	        	contacts.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						TextView view2 = (TextView) arg1.findViewById(R.id.receivedMessage);
						
						String[] t1 = contact.getText().toString().split("; ");
						String string = "";
						
						for (int i = 0; i < t1.length - 1; i++)
						{
							string += t1[i];
						}
						
						contact.setText(string + view2.getText());
						contact.setSelection(contact.getText().toString().length());
						
						InputMethodManager imm = (InputMethodManager)getSystemService(
							      Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(contact.getWindowToken(), 0);
					}
	        		
	        	});
	        }

	        public void afterTextChanged(Editable s) {
	        }
		});
		
		radio1 = (RadioButton) findViewById(R.id.level1);
		radio2 = (RadioButton) findViewById(R.id.level2);
		
		radio1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (radio1.isChecked())
				{
					saveType = 1;
				} else
				{
					saveType = 2;
				}
			}
			
		});
		
		radio2.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (radio2.isChecked())
				{
					saveType = 2;
				} else
				{
					saveType = 1;
				}
			}
			
		});
		
		Bundle b = getIntent().getExtras();
		
		if (b != null)
		{
			contact.setText(b.getString("com.klinker.android.messaging.BLACKLIST_NAME"));
			
			int type = b.getInt("com.klinker.android.messaging.BLACKLIST_TYPE");
			
			if (type == 1)
			{
				radio1.setChecked(true);
			} else
			{
				radio2.setChecked(true);
			}
		}
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
	}
	
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		
		boolean stop = false;
		int pos = 0;
		
		for (int i = 0; i < individuals.size(); i++)
		{
			if (individuals.get(i).name.trim().equals(contact.getText().toString()))
			{
				stop = true;
				pos = i;
				break;
			}
		}
		
		if (!contact.getText().toString().equals("") && !stop)
		{
			individuals.add(new BlacklistContact(contact.getText().toString(), saveType));
			writeToFile(individuals, context);
		} else if (stop)
		{
			individuals.set(pos, new BlacklistContact(contact.getText().toString(), saveType));
			writeToFile(individuals, context);
		}
		
		finish();
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
}