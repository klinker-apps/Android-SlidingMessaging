package com.klinker.android.messaging_sliding.theme;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.widget.*;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_donate.utils.IOUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class PopupChooserActivity extends Activity {

	public SectionsPagerAdapter mSectionsPagerAdapter;

	public ViewPager mViewPager;
	public SharedPreferences sharedPrefs;
	public static ArrayList<CustomPopup> themes;
	public static int NUMBER_DEFAULT_THEMES = 2;

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

	public void refreshThemes()
	{
		themes = new ArrayList<CustomPopup>();

		themes.add(new CustomPopup("White", this));
		themes.add(new CustomPopup("Dark", this));

		String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        File file = new File(root + "/SlidingMessaging");
        file.mkdir();

		File list[] = file.listFiles();
		ArrayList<File> files = new ArrayList<File>();

		if (list != null)
		{
			for (int i = 0; i < list.length; i++)
			{
				if (list[i].toString().endsWith(".theme2"))
				{
					files.add(list[i]);
				}
			}
		}

		for (int i = 0; i < files.size(); i++)
		{
			String data = IOUtil.readPopupTheme(files.get(i).getName());
			themes.add(CustomPopup.themeFromString(data));
		}

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		for (int i = 0; i < themes.size(); i++)
		{
			if (sharedPrefs.getString("cp_theme_name", "Light Theme").equals(themes.get(i).name))
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
				}
			}
		});
	}

	public void saveSettings(boolean toast)
	{
		SharedPreferences.Editor editor = sharedPrefs.edit();
		editor.putString("cp_theme_name", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).name);
		editor.putInt("cp_messageBackground", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).messageBackground);
		editor.putInt("cp_sendBarBackground", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).sendbarBackground);
		editor.putInt("cp_dividerColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).dividerColor);
		editor.putInt("cp_nameTextColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).nameTextColor);
		editor.putInt("cp_numberTextColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).numberTextColor);
		editor.putInt("cp_dateTextColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).dateTextColor);
		editor.putInt("cp_messageTextColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).messageTextColor);
		editor.putInt("cp_draftTextColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).draftTextColor);
		editor.putInt("cp_buttonColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).buttonColor);
		editor.putInt("cp_emojiButtonColor", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).emojiButtonColor);
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
		getMenuInflater().inflate(R.menu.activity_popup_theme, menu);
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
				editor.putString("cp_theme_name", "My Custom Popup Theme");
				editor.commit();

		    	Intent intent = new Intent(this, CustomPopupActivity.class);
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

		    	try
		       	 {
		       		 PackageManager pm = this.getPackageManager();
		       		 pm.getPackageInfo("com.klinker.android.messaging_theme", PackageManager.GET_ACTIVITIES);

		       		 saveSettings(false);

		       		 if (mViewPager.getCurrentItem() < NUMBER_DEFAULT_THEMES)
		       		 {
			       		SharedPreferences.Editor editor = sharedPrefs.edit();
			    		editor.putString("cp_theme_name", PopupChooserActivity.themes.get(mViewPager.getCurrentItem()).name + " 2");
			    		editor.commit();
		       		 }

			    	 Intent intent2 = new Intent(this, CustomPopupActivity.class);
			    	 startActivity(intent2);
                     overridePendingTransition(R.anim.activity_slide_in_right, R.anim.activity_slide_out_left);
		       	 } catch (PackageManager.NameNotFoundException e)
		       	 {
		       		 Intent intent = new Intent(Intent.ACTION_VIEW);
		       		 intent.setData(Uri.parse("market://details?id=com.klinker.android.messaging_theme"));
		       		 startActivity(intent);
		       	 }

	    	return true;
	    case R.id.menu_delete_theme:
	    	if (mViewPager.getCurrentItem() < NUMBER_DEFAULT_THEMES)
	    	{
	    		Toast.makeText(this, getResources().getString(R.string.cannot_delete), Toast.LENGTH_SHORT).show();
	    	} else
	    	{
	    		File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SlidingMessaging/" + themes.get(mViewPager.getCurrentItem()).name.replace(" ", "") + ".theme2");
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
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}

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
			return PopupChooserActivity.themes.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return PopupChooserActivity.themes.get(position).name;
		}
	}

	public static class DummySectionFragment extends android.app.Fragment {

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

			this.position = args.getInt("position");

			view = inflater.inflate(R.layout.popup_preview, container, false);
			sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

			return refreshTheme();
		}

		public View refreshTheme()
		{
			View expandedOptions = view.findViewById(R.id.expandedOptions);
            ImageButton deleteButton = (ImageButton) view.findViewById(R.id.deleteButton);
            Button viewConversation = (Button) view.findViewById(R.id.viewConversationButton);
            ImageButton readButton = (ImageButton) view.findViewById(R.id.readButton);

            View sendBar = view.findViewById(R.id.sendBar);
            ImageButton emojiButton = (ImageButton) view.findViewById(R.id.emojiButton);
            EditText messageEntry = (EditText) view.findViewById(R.id.messageEntry);
            ImageButton sendButton = (ImageButton) view.findViewById(R.id.sendButton);

            View messageBackground = view.findViewById(R.id.view1);
            TextView name = (TextView) view.findViewById(R.id.contactName);
            View divider1 = view.findViewById(R.id.contactLine);
            TextView number = (TextView) view.findViewById(R.id.contactNumber);
            View divider2 = view.findViewById(R.id.messageDivider1);
            TextView date = (TextView) view.findViewById(R.id.date);
            View divider3 = view.findViewById(R.id.messageDivider2);
            TextView body = (TextView) view.findViewById(R.id.body);
            ImageButton close = (ImageButton) view.findViewById(R.id.closeButton);

            Drawable sendBack = getActivity().getResources().getDrawable(R.drawable.card_background);
            sendBack.setColorFilter(themes.get(position).sendbarBackground, PorterDuff.Mode.MULTIPLY);
            Drawable msgBack = getActivity().getResources().getDrawable(R.drawable.card_background);
            msgBack.setColorFilter(themes.get(position).messageBackground, PorterDuff.Mode.MULTIPLY);

            expandedOptions.setBackgroundColor(themes.get(position).sendbarBackground);
            sendBar.setBackgroundColor(themes.get(position).sendbarBackground);
            messageBackground.setBackgroundColor(themes.get(position).messageBackground);

            deleteButton.setColorFilter(themes.get(position).buttonColor);
            readButton.setColorFilter(themes.get(position).buttonColor);
            sendButton.setColorFilter(themes.get(position).buttonColor);
            close.setColorFilter(themes.get(position).buttonColor);
            viewConversation.setTextColor(themes.get(position).buttonColor);

            emojiButton.setColorFilter(themes.get(position).emojiButtonColor);

            messageEntry.setTextColor(themes.get(position).draftTextColor);
            name.setTextColor(themes.get(position).nameTextColor);
            number.setTextColor(themes.get(position).numberTextColor);
            date.setTextColor(themes.get(position).dateTextColor);
            body.setTextColor(themes.get(position).messageTextColor);

            divider1.setBackgroundColor(themes.get(position).dividerColor);
            divider2.setBackgroundColor(themes.get(position).dividerColor);
            divider3.setBackgroundColor(themes.get(position).dividerColor);

            name.setText(getActivity().getResources().getString(R.string.ct_contact_name));
            number.setText("+1-333-555-4444");
            body.setText(getActivity().getResources().getString(R.string.message_body));
            date.setText("8:00 AM");
			
			return view;
		}
	}

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.activity_slide_in_left, R.anim.activity_slide_out_right);
    }
}
