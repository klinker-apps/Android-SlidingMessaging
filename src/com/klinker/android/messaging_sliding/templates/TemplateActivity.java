package com.klinker.android.messaging_sliding.templates;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import com.klinker.android.messaging_donate.R;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class TemplateActivity extends Activity {
	
	public static Context context;
	public DragSortListView templates;
	public Button addNew;
    public ImageButton delete;
	public SharedPreferences sharedPrefs;
	public ArrayList<String> text;

    public TemplateArrayAdapter adapter;

    private DragSortListView.DropListener onDrop =
            new DragSortListView.DropListener() {
                @Override
                public void drop(int from, int to) {
                    if (from != to) {

                        String item = adapter.getItem(from);
                        adapter.remove(item);
                        adapter.insert(item, to);

                        text.remove(item);
                        text.add(to, item);

                        writeToFile(text, getBaseContext());

                        adapter = new TemplateArrayAdapter(getActivity(), text);
                        templates.setAdapter(adapter);
                    }
                }
            };

    private DragSortListView.RemoveListener onRemove =
            new DragSortListView.RemoveListener() {
                @Override
                public void remove(int which) {
                    text.remove(which);

                    adapter = new TemplateArrayAdapter(getActivity(), text);
                    templates.setAdapter(adapter);
                }
            };
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.templates);
		templates = (DragSortListView) findViewById(R.id.templateListView);
        findViewById(R.id.templateListView2).setVisibility(View.GONE);
		addNew = (Button) findViewById(R.id.addNewButton);

		sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		context = this;
		
		text = readFromFile(this);
		
		adapter = new TemplateArrayAdapter(this, text);
		templates.setStackFromBottom(false);
        templates.setDropListener(onDrop);
        templates.setRemoveListener(onRemove);
        templates.setAdapter(adapter);

        DragSortController controller = new DragSortController(templates);
        controller.setRemoveEnabled(true);
        controller.setSortEnabled(true);
        controller.setDragInitMode(DragSortController.ON_DRAG);

        templates.setFloatViewManager(controller);
        templates.setOnTouchListener(controller);
        templates.setDragEnabled(true);

		
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

		templates.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1,
                                   final int arg2, long arg3) {
                final EditText input = new EditText(context);
                input.setText(text.get(arg2));

                new AlertDialog.Builder(context)
                        .setTitle(context.getResources().getString(R.string.add_new))
                        .setView(input)
                        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                String inputText = input.getText().toString();
                                text.set(arg2, inputText);

                                TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, text);
                                templates.setAdapter(adapter);
                            }
                        }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                }).show();
            }

        });

            addNew.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick (View arg0){
                    final EditText input = new EditText(context);

                    new AlertDialog.Builder(context)
                            .setTitle(context.getResources().getString(R.string.add_new))
                            .setView(input)
                            .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    String inputText = input.getText().toString();
                                    text.add(inputText);

                                    TemplateArrayAdapter adapter = new TemplateArrayAdapter((Activity) context, text);
                                    templates.setAdapter(adapter);
                                }
                            }).setNegativeButton(context.getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {

                        }
                    }).show();

                }

            }

            );


        }

        @Override
	public void onBackPressed() {
		writeToFile(text, this);
		super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
	}
	
	@SuppressWarnings("resource")
	private ArrayList<String> readFromFile(Context context) {
		
	      ArrayList<String> ret = new ArrayList<String>();
	      
	      try {
	    	  InputStream inputStream;
	          
	          if (sharedPrefs.getBoolean("save_to_external", true))
	          {
	         	 inputStream = new FileInputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt");
	          } else
	          {
	        	  inputStream = context.openFileInput("templates.txt");
	          }
	          
	          if ( inputStream != null ) {
	          	InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	          	BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	          	String receiveString = "";
	          	
	          	while ( (receiveString = bufferedReader.readLine()) != null ) {
	          		ret.add(receiveString);
	          	}
	          	
	          	inputStream.close();
	          }
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
	  	
	  	private void writeToFile(ArrayList<String> data, Context context) {
	        try {
	        	
	        	OutputStreamWriter outputStreamWriter;
	            
	            if (sharedPrefs.getBoolean("save_to_external", true))
	            {
	            	outputStreamWriter = new OutputStreamWriter(new FileOutputStream(Environment.getExternalStorageDirectory() + "/SlidingMessaging/templates.txt"));
	            } else
	            {
	            	outputStreamWriter = new OutputStreamWriter(context.openFileOutput("templates.txt", Context.MODE_PRIVATE));
	            }
	            
	            for (int i = 0; i < data.size(); i++)
	            {
	            	outputStreamWriter.write(data.get(i) + "\n");
	            }
	            	
	            outputStreamWriter.close();
	        }
	        catch (IOException e) {
	            
	        } 
			
		}

    public Activity getActivity()
    {
        return this;
    }
}