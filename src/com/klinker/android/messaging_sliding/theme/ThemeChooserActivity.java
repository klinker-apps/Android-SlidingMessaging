package com.klinker.android.messaging_sliding.theme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
import com.klinker.android.messaging_donate.R;

import java.io.*;
import java.util.ArrayList;
import java.util.Locale;

public class ThemeChooserActivity extends Activity {
	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	public SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	public ViewPager mViewPager;
	public SharedPreferences sharedPrefs;
	public static ArrayList<CustomTheme> themes;
	public static int NUMBER_DEFAULT_THEMES = 10;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		setContentView(R.layout.activity_theme);
		
		if (sharedPrefs.getBoolean("override_lang", false))
		{
			String languageToLoad  = "en";
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
	
	public void refreshThemes()
	{
		themes = new ArrayList<CustomTheme>();
		
		themes.add(new CustomTheme("White", this));
		themes.add(new CustomTheme("Dark", this));
		themes.add(new CustomTheme("Pitch Black", this));
        themes.add(new CustomTheme("Hangouts", this));
        themes.add(new CustomTheme("Light 2.0", this));
		themes.add(new CustomTheme("Dark Blue", this));
		themes.add(new CustomTheme("Light Green", this));
		themes.add(new CustomTheme("Burnt Orange", this));
		themes.add(new CustomTheme("Holo Purple", this));
		themes.add(new CustomTheme("Bright Red", this));
		
		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(root + "/SlidingMessaging");
		file.mkdir();
		
		File list[] = file.listFiles();
		ArrayList<File> files = new ArrayList<File>();
		
		if (list != null)
		{
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].toString().endsWith(".theme"))
				{
					files.add(list[i]);
				}
			}
		}
		
		for (int i = 0; i < files.size(); i++)
		{
			String data = readFromFile(this, files.get(i).getName());
			themes.add(CustomTheme.themeFromString(data));
		}
		
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		
		for (int i = 0; i < themes.size(); i++)
		{
			if (sharedPrefs.getString("ct_theme_name", "Light Theme").equals(themes.get(i).name))
			{
				mViewPager.setCurrentItem(i, true);
				break;
			}
		}
		
		Button select = (Button) findViewById(R.id.selectButton);
		
		final Context context = this;
		
		select.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if(mViewPager.getCurrentItem() <= 1)
				{
					saveSettings(true);
					finish();
				} else if (mViewPager.getCurrentItem() >= 2)
				{
					try
	            	 {
	            		 PackageManager pm = context.getPackageManager();
	            		 pm.getPackageInfo("com.klinker.android.messaging_donate", PackageManager.GET_ACTIVITIES);
	            		 saveSettings(true);
	            		 finish();
	            	 } catch (PackageManager.NameNotFoundException e)
	            	 {
	            		 try
		            	 {
		            		 PackageManager pm = context.getPackageManager();
		            		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
		            		 saveSettings(true);
		            		 finish();
		            	 } catch (PackageManager.NameNotFoundException f)
		            	 {
		            		 Intent intent = new Intent(Intent.ACTION_VIEW);
		            		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
			            	 startActivity(intent);
		            	 }
	            	 }
				} else
				{
					try
	            	 {
	            		 PackageManager pm = context.getPackageManager();
	            		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
	            		 saveSettings(true);
	            		 finish();
	            	 } catch (PackageManager.NameNotFoundException e)
	            	 {
	            		 Intent intent = new Intent(Intent.ACTION_VIEW);
	            		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
	            		 startActivity(intent);
	            	 }
				}
			}
		});
	}
	
	public void saveSettings(boolean toast)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putBoolean("custom_theme", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).custom);
		editor.putString("ct_theme_name", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).name);
		editor.putInt("ct_titleBarColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).titleBarColor);
		editor.putInt("ct_titleBarTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).titleBarTextColor);
		editor.putInt("ct_messageListBackground", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).messageListBackground);
		editor.putInt("ct_sendbarBackground", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).sendbarBackground);
		editor.putInt("ct_sentMessageBackground", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).sentMessageBackground);
		editor.putInt("ct_receivedMessageBackground", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).receivedMessageBackground);
		editor.putInt("ct_sentTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).sentTextColor);
		editor.putInt("ct_receivedTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).receivedTextColor);
		editor.putInt("ct_conversationListBackground", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).conversationListBackground);
		editor.putInt("ct_nameTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).nameTextColor);
		editor.putInt("ct_summaryTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).summaryTextColor);
		editor.putBoolean("ct_messageDividerVisibility", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).messageDividerVisibility);
		editor.putInt("ct_messageDividerColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).messageDividerColor);
		editor.putInt("ct_sendButtonColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).sendButtonColor);
		editor.putBoolean("ct_darkContactImage", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).darkContactImage);
		editor.putInt("ct_messageCounterColor", CustomTheme.convertToColorInt(ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).messageCounterColor));
		editor.putInt("ct_draftTextColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).draftTextColor);
		editor.putInt("ct_emojiButtonColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).emojiButtonColor);
		editor.putInt("ct_conversationDividerColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).conversationDividerColor);
        editor.putInt("ct_unreadConversationColor", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).unreadConversationColor);
        editor.putBoolean("ct_light_action_bar", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).lightAb);
		editor.commit();
		
		if (toast)
			Toast.makeText(getBaseContext(), getResources().getString(R.string.toast_theme_saved), Toast.LENGTH_LONG).show();
	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		refreshThemes();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_theme, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	    case R.id.menu_add_theme:
	    	try
	       	 {
	       		 PackageManager pm = this.getPackageManager();
	       		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
	       		 
	       		SharedPreferences.Editor editor = sharedPrefs.edit();
				editor.putString("ct_theme_name", "My Custom Theme");
				editor.commit();
				
		    	Intent intent = new Intent(this, CustomThemeActivity.class);
		    	startActivity(intent);
                 overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
	       	 } catch (PackageManager.NameNotFoundException e)
	       	 {
	       		 Intent intent = new Intent(Intent.ACTION_VIEW);
	       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
	       		 startActivity(intent);
	       	 }
	    	
	        return true;
	    case R.id.menu_edit_theme:
	    	if (mViewPager.getCurrentItem() < 2)
	    	{
	    		 saveSettings(false);
	    		
	    		 Intent intent2 = new Intent(this, DefaultThemeActivity.class);
		    	 startActivity(intent2);
                overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
	    	} else if (mViewPager.getCurrentItem() < 3)
	    	{
	    		try
		       	 {
		       		 PackageManager pm = this.getPackageManager();
		       		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
		       		 
		       		 saveSettings(false);
		       		 
			    	 Intent intent2 = new Intent(this, DefaultThemeActivity.class);
			    	 startActivity(intent2);
                     overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
		       	 } catch (PackageManager.NameNotFoundException e)
		       	 {
		       		 try
		       		 {
		       			 PackageManager pm = this.getPackageManager();
			       		 pm.getPackageInfo("com.klinker.android.messaging_donate", PackageManager.GET_ACTIVITIES);
			       		 
			       		 saveSettings(false);
			       		 
			       		Intent intent2 = new Intent(this, DefaultThemeActivity.class);
				    	 startActivity(intent2);
                         overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
		       		 } catch (Exception f)
		       		 {
		       			 Intent intent = new Intent(Intent.ACTION_VIEW);
			       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
			       		 startActivity(intent);
		       		 }
		       	 }
	    	} else if (mViewPager.getCurrentItem() < NUMBER_DEFAULT_THEMES)
	    	{
	    		try
		       	 {
		       		 PackageManager pm = this.getPackageManager();
		       		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
		       		 
		       		 saveSettings(false);
		       		 
		       		 SharedPreferences.Editor editor = sharedPrefs.edit();
		    		 editor.putString("ct_theme_name", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).name + " 2");
		    		 editor.commit();
			    	
			    	 Intent intent2 = new Intent(this, CustomThemeActivity.class);
			    	 startActivity(intent2);
                     overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
		       	 } catch (PackageManager.NameNotFoundException e)
		       	 {
		       		 Intent intent = new Intent(Intent.ACTION_VIEW);
		       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
		       		 startActivity(intent);
		       	 }
	    	} else
	    	{
		    	try
		       	 {
		       		 PackageManager pm = this.getPackageManager();
		       		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);
		       		 
		       		 saveSettings(false);
		       		 
		       		 if (mViewPager.getCurrentItem() < NUMBER_DEFAULT_THEMES)
		       		 {
			       		SharedPreferences.Editor editor = sharedPrefs.edit();
			    		editor.putString("ct_theme_name", ThemeChooserActivity.themes.get(mViewPager.getCurrentItem()).name + " 2");
			    		editor.commit();
		       		 }
			    	
			    	 Intent intent2 = new Intent(this, CustomThemeActivity.class);
			    	 startActivity(intent2);
                     overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
		       	 } catch (PackageManager.NameNotFoundException e)
		       	 {
		       		 Intent intent = new Intent(Intent.ACTION_VIEW);
		       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
		       		 startActivity(intent);
		       	 }
	    	}
	    	
	    	return true;
	    case R.id.menu_delete_theme:
	    	if (mViewPager.getCurrentItem() < NUMBER_DEFAULT_THEMES)
	    	{
	    		Toast.makeText(this, getResources().getString(R.string.cannot_delete), Toast.LENGTH_SHORT).show();
	    	} else
	    	{
	    		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging/" + themes.get(mViewPager.getCurrentItem()).name.replace(" ", "") + ".theme");
	    		file.delete();
	    		
	    		int position = mViewPager.getCurrentItem();
	    		refreshThemes();
	    		
	    		try
	    		{
	    			mViewPager.setCurrentItem(position);
	    		} catch (Exception e)
	    		{
	    			mViewPager.setCurrentItem(position - 1);
	    		}
	    	}
	    	
			return true;
	    case R.id.menu_get_more_themes:
	    	 Intent intent = new Intent(Intent.ACTION_VIEW);
      		 intent.setData(Uri.parse("http://forum.xda-developers.com/showthread.php?p=39533859#post39533859"));
      		 startActivity(intent);
	    	return true;
	    case R.id.menu_custom_font:
       		 Intent intent3 = new Intent(this, CustomFontSettingsActivity.class);
       		 startActivity(intent3);
            overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
	    	return true;
	    case R.id.menu_instructions:
	    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	    	LayoutInflater inflater = getLayoutInflater();
	    	View view = inflater.inflate(R.layout.theme_help, null);
	    	builder.setView(view);
	    	builder.setTitle(getResources().getString(R.string.menu_instructions));
	    	
	    	TextView link = (TextView) view.findViewById(R.id.textView8);
	    	link.setMovementMethod(LinkMovementMethod.getInstance());
	    	
	    	AlertDialog dialog = builder.create();
	    	dialog.show();
	    	return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

	private String readFromFile(Context context, String fileName) {
		
	      String ret = "";
	      
	      try {
	    	  File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", fileName);
	          @SuppressWarnings("resource")
			BufferedReader reader = new BufferedReader(new FileReader(file));
	          
	          String s = "";
	          
	          while ((s = reader.readLine()) != null)
	          {
	        	  ret += s + "\n";
	          }
	          
	      }
	      catch (FileNotFoundException e) {
	      	
			} catch (IOException e) {
				
			}

	      return ret;
		}
	
	@SuppressWarnings("unused")
	private void writeToFile(String data, Context context, String name) {
		String[] data2 = data.split("\n");
        try {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging", name);
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            
            for (int i = 0; i < data2.length; i++)
            {
            	pw.println(data2[i]);
            }
            
            pw.flush();
            pw.close();
            f.close();
        }
        catch (Exception e) {
            
        } 
		
	}
	
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends android.support.v13.app.FragmentStatePagerAdapter {
		
		public SectionsPagerAdapter(android.app.FragmentManager fm) {
			super(fm);
		}

		@Override
		public DummySectionFragment getItem(int position) {
			DummySectionFragment fragment = new DummySectionFragment();
			Bundle args = new Bundle();
			args.putInt("position", position);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return ThemeChooserActivity.themes.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {	
			return ThemeChooserActivity.themes.get(position).name;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends android.app.Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public int position;
		private View view;
		public SharedPreferences sharedPrefs;
		
		public DummySectionFragment() {
			
		}
		
		@Override
		public void onCreate(Bundle savedInstanceState)
		{
			super.onCreate(savedInstanceState);
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			
			Bundle args = getArguments();

            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
			
			this.position = args.getInt("position");

            if (sharedPrefs.getString("run_as", "sliding").equals("sliding")) {
                view = inflater.inflate(R.layout.theme_preview, container, false);
            } else if (sharedPrefs.getString("run_as", "sliding").equals("hangout")) {
                view = inflater.inflate(R.layout.theme_preview_hangouts, container, false);
            } else if (sharedPrefs.getString("run_as", "sliding").equals("card2")) {
                view = inflater.inflate(R.layout.theme_preview_card2, container, false);
            } else if (sharedPrefs.getString("run_as", "sliding").equals("card+")) {
                view = inflater.inflate(R.layout.theme_preview_card_plus, container, false);
            }
			
			return refreshTheme();
		}		
		
		public View refreshTheme()
		{
            if (sharedPrefs.getString("run_as", "sliding").equals("hangout") || sharedPrefs.getString("run_as", "sliding").equals("card2") || sharedPrefs.getString("run_as", "sliding").equals("card+")) {
                TextView receivedMessageText = (TextView) view.findViewById(R.id.textBody);
                TextView sentMessageText = (TextView) view.findViewById(R.id.textBody2);
                ImageButton emojiButton = (ImageButton) view.findViewById(R.id.display_emoji);
                EditText messageEntry = (EditText) view.findViewById(R.id.messageEntry);
                TextView titleBar = (TextView) view.findViewById(R.id.contactNamePreview);
                View headerPadding = view.findViewById(R.id.headerPadding);
                View sendbar = view.findViewById(R.id.view1);
                View sentBackground = view.findViewById(R.id.messageBody2);
                View receivedBackground = view.findViewById(R.id.messageBody);
                TextView sentDate = (TextView) view.findViewById(R.id.textDate2);
                TextView receiveDate = (TextView) view.findViewById(R.id.textDate);
                ImageButton sendButton = (ImageButton) view.findViewById(R.id.sendButton);
                ImageView sentContactPicture = (ImageView) view.findViewById(R.id.imageContactPicture);
                ImageView receiveContactPicture = (ImageView) view.findViewById(R.id.imageContactPicture2);
                ImageView sentTriangle = (ImageView) view.findViewById(R.id.msgBubble);
                ImageView receivedTriangle = (ImageView) view.findViewById(R.id.msgBubble2);

                receivedMessageText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
                sentMessageText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));

                if (!sharedPrefs.getBoolean("emoji", false))
                {
                    emojiButton.setVisibility(View.GONE);
                    LayoutParams params = (RelativeLayout.LayoutParams) messageEntry.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    messageEntry.setLayoutParams(params);
                }

                try {
                    titleBar.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                    titleBar.setBackgroundColor(ThemeChooserActivity.themes.get(position).titleBarColor);
                    titleBar.setTextColor(ThemeChooserActivity.themes.get(position).titleBarTextColor);
                } catch (Exception e) {

                }

                headerPadding.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageListBackground);
                sendbar.setBackgroundColor(ThemeChooserActivity.themes.get(position).sendbarBackground);
                sentBackground.setBackgroundColor(ThemeChooserActivity.themes.get(position).sentMessageBackground);
                receivedBackground.setBackgroundColor(ThemeChooserActivity.themes.get(position).receivedMessageBackground);
                sentMessageText.setTextColor(ThemeChooserActivity.themes.get(position).sentTextColor);
                sentDate.setTextColor(ThemeChooserActivity.themes.get(position).sentTextColor);
                receivedMessageText.setTextColor(ThemeChooserActivity.themes.get(position).receivedTextColor);
                receiveDate.setTextColor(ThemeChooserActivity.themes.get(position).receivedTextColor);
                sendButton.setColorFilter(ThemeChooserActivity.themes.get(position).sendButtonColor);
                emojiButton.setColorFilter(ThemeChooserActivity.themes.get(position).emojiButtonColor);
                sentTriangle.setColorFilter(ThemeChooserActivity.themes.get(position).sentMessageBackground);
                receivedTriangle.setColorFilter(ThemeChooserActivity.themes.get(position).receivedMessageBackground);

                if(sharedPrefs.getString("run_as", "sliding").equals("card+"))
                {
                    sentBackground.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    receivedBackground.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }

                if (ThemeChooserActivity.themes.get(position).darkContactImage)
                {
                    sentContactPicture.setImageResource(R.drawable.ic_contact_dark);
                    receiveContactPicture.setImageResource(R.drawable.ic_contact_dark);
                }

                if (!ThemeChooserActivity.themes.get(position).custom)
                {
                    String titleColor = sharedPrefs.getString("title_color", "blue");
                    String color = sharedPrefs.getString("sent_text_color", "default");

                    if (titleColor.equals("blue"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_blue));
                    } else if (titleColor.equals("orange"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_orange));
                    } else if (titleColor.equals("red"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_red));
                    } else if (titleColor.equals("green"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_green));
                    } else if (titleColor.equals("purple"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_purple));
                    } else if (titleColor.equals("grey"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.grey));
                    } else if (titleColor.equals("black"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.pitch_black));
                    } else if (titleColor.equals("darkgrey"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.darkgrey));
                    }

                    if (color.equals("blue"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_blue));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.white));
                        sentDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_green));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_orange));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_red));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_purple));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.pitch_black));
                        sentDate.setTextColor(getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.grey));
                        sentDate.setTextColor(getResources().getColor(R.color.grey));
                    }

                    color = sharedPrefs.getString("received_text_color", "default");

                    if (color.equals("blue"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_blue));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.white));
                        receiveDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_green));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_orange));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_red));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_purple));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.pitch_black));
                        receiveDate.setTextColor(getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.grey));
                        receiveDate.setTextColor(getResources().getColor(R.color.grey));
                    }

                    if (sharedPrefs.getBoolean("title_text_color", false))
                    {
                        titleBar.setTextColor(getActivity().getResources().getColor(R.color.black));
                    }
                }

                if (!sharedPrefs.getBoolean("hide_title_bar", true))
                {
                    titleBar.setVisibility(View.GONE);
                }

                if (sharedPrefs.getBoolean("title_caps", true))
                {
                    titleBar.setText(getActivity().getResources().getString(R.string.ct_contact_name).toUpperCase());
                    titleBar.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                }

                if (sharedPrefs.getBoolean("custom_font", false))
                {
                    Typeface font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));

                    sentMessageText.setTypeface(font);
                    receivedMessageText.setTypeface(font);
                    sentDate.setTypeface(font);
                    receiveDate.setTypeface(font);
                    messageEntry.setTypeface(font);
                }
            } else if (sharedPrefs.getString("run_as", "sliding").equals("sliding")) {
                TextView receivedMessageText = (TextView) view.findViewById(R.id.receivedMessage);
                TextView sentMessageText = (TextView) view.findViewById(R.id.sentMessage);
                ImageButton emojiButton = (ImageButton) view.findViewById(R.id.display_emoji);
                EditText messageEntry = (EditText) view.findViewById(R.id.messageEntry);
                TextView titleBar = (TextView) view.findViewById(R.id.contactNamePreview);
                View headerPadding = view.findViewById(R.id.headerPadding);
                View footerPadding = view.findViewById(R.id.footerPadding);
                View sendbar = view.findViewById(R.id.view1);
                View sentBackground = view.findViewById(R.id.sentBackground);
                View receivedBackground = view.findViewById(R.id.receiveBackground);
                View sentBackgroundBack = view.findViewById(R.id.sentBackgroundBack);
                View receivedBackgroundBack = view.findViewById(R.id.receiveBackgroundBack);
                TextView sentDate = (TextView) view.findViewById(R.id.sentDate);
                TextView receiveDate = (TextView) view.findViewById(R.id.receivedDate);
                View messageDivider = view.findViewById(R.id.messageDivider);
                ImageButton sendButton = (ImageButton) view.findViewById(R.id.sendButton);
                ImageView sentContactPicture = (ImageView) view.findViewById(R.id.sentContactPicture1);
                ImageView receiveContactPicture = (ImageView) view.findViewById(R.id.receivedContactPicture1);

                receivedMessageText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));
                sentMessageText.setTextSize(Integer.parseInt(sharedPrefs.getString("text_size", "14").substring(0,2)));

                if (!sharedPrefs.getBoolean("emoji", false))
                {
                    emojiButton.setVisibility(View.GONE);
                    LayoutParams params = (RelativeLayout.LayoutParams) messageEntry.getLayoutParams();
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    messageEntry.setLayoutParams(params);
                }

                titleBar.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
                titleBar.setBackgroundColor(ThemeChooserActivity.themes.get(position).titleBarColor);
                titleBar.setTextColor(ThemeChooserActivity.themes.get(position).titleBarTextColor);
                headerPadding.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageListBackground);
                footerPadding.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageListBackground);
                sendbar.setBackgroundColor(ThemeChooserActivity.themes.get(position).sendbarBackground);
                sentBackground.setBackgroundColor(ThemeChooserActivity.themes.get(position).sentMessageBackground);
                receivedBackground.setBackgroundColor(ThemeChooserActivity.themes.get(position).receivedMessageBackground);
                sentBackgroundBack.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageListBackground);
                receivedBackgroundBack.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageListBackground);
                sentMessageText.setTextColor(ThemeChooserActivity.themes.get(position).sentTextColor);
                sentDate.setTextColor(ThemeChooserActivity.themes.get(position).sentTextColor);
                receivedMessageText.setTextColor(ThemeChooserActivity.themes.get(position).receivedTextColor);
                receiveDate.setTextColor(ThemeChooserActivity.themes.get(position).receivedTextColor);
                sendButton.setColorFilter(ThemeChooserActivity.themes.get(position).sendButtonColor);
                emojiButton.setColorFilter(ThemeChooserActivity.themes.get(position).emojiButtonColor);

                if (ThemeChooserActivity.themes.get(position).messageDividerVisibility)
                {
                    messageDivider.setBackgroundColor(ThemeChooserActivity.themes.get(position).messageDividerColor);
                } else
                {
                    messageDivider.setBackgroundColor(ThemeChooserActivity.themes.get(position).sentMessageBackground);
                }

                if (ThemeChooserActivity.themes.get(position).darkContactImage)
                {
                    sentContactPicture.setImageResource(R.drawable.ic_contact_dark);
                    receiveContactPicture.setImageResource(R.drawable.ic_contact_dark);
                }

                if (!ThemeChooserActivity.themes.get(position).custom)
                {
                    String titleColor = sharedPrefs.getString("title_color", "blue");
                    String color = sharedPrefs.getString("sent_text_color", "default");

                    if (titleColor.equals("blue"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_blue));
                    } else if (titleColor.equals("orange"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_orange));
                    } else if (titleColor.equals("red"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_red));
                    } else if (titleColor.equals("green"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_green));
                    } else if (titleColor.equals("purple"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.holo_purple));
                    } else if (titleColor.equals("grey"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.grey));
                    } else if (titleColor.equals("black"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.pitch_black));
                    } else if (titleColor.equals("darkgrey"))
                    {
                        titleBar.setBackgroundColor(getResources().getColor(R.color.darkgrey));
                    }

                    if (color.equals("blue"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_blue));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.white));
                        sentDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_green));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_orange));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_red));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.holo_purple));
                        sentDate.setTextColor(getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.pitch_black));
                        sentDate.setTextColor(getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        sentMessageText.setTextColor(getResources().getColor(R.color.grey));
                        sentDate.setTextColor(getResources().getColor(R.color.grey));
                    }

                    color = sharedPrefs.getString("received_text_color", "default");

                    if (color.equals("blue"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_blue));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_blue));
                    } else if (color.equals("white"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.white));
                        receiveDate.setTextColor(getResources().getColor(R.color.white));
                    } else if (color.equals("green"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_green));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_green));
                    } else if (color.equals("orange"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_orange));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_orange));
                    } else if (color.equals("red"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_red));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_red));
                    } else if (color.equals("purple"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.holo_purple));
                        receiveDate.setTextColor(getResources().getColor(R.color.holo_purple));
                    } else if (color.equals("black"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.pitch_black));
                        receiveDate.setTextColor(getResources().getColor(R.color.pitch_black));
                    } else if (color.equals("grey"))
                    {
                        receivedMessageText.setTextColor(getResources().getColor(R.color.grey));
                        receiveDate.setTextColor(getResources().getColor(R.color.grey));
                    }

                    if (sharedPrefs.getBoolean("title_text_color", false))
                    {
                        titleBar.setTextColor(getActivity().getResources().getColor(R.color.black));
                    }
                }

                if (!sharedPrefs.getBoolean("hide_title_bar", true))
                {
                    titleBar.setVisibility(View.GONE);
                }

                if (sharedPrefs.getBoolean("title_caps", true))
                {
                    titleBar.setText(getActivity().getResources().getString(R.string.ct_contact_name).toUpperCase());
                    titleBar.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                }

                if (sharedPrefs.getBoolean("custom_font", false))
                {
                    Typeface font = Typeface.createFromFile(sharedPrefs.getString("custom_font_path", ""));

                    sentMessageText.setTypeface(font);
                    receivedMessageText.setTypeface(font);
                    sentDate.setTypeface(font);
                    receiveDate.setTypeface(font);
                    messageEntry.setTypeface(font);
                }
            }
			
			return view;
		}
	}
}
