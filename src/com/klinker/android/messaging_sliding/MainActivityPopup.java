package com.klinker.android.messaging_sliding;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.klinker.android.messaging_donate.R;

public class MainActivityPopup extends MainActivity {

    @Override
    public void setUpWindow() {
        com.klinker.android.messaging_donate.MainActivity.group = null;
        com.klinker.android.messaging_donate.MainActivity.inboxBody = null;
        com.klinker.android.messaging_donate.MainActivity.inboxDate = null;
        com.klinker.android.messaging_donate.MainActivity.inboxNumber = null;
        com.klinker.android.messaging_donate.MainActivity.msgCount = null;
        com.klinker.android.messaging_donate.MainActivity.msgRead = null;
        com.klinker.android.messaging_donate.MainActivity.threadIds = null;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsThemeDialog);
        }

        String pinType = sharedPrefs.getString("pin_conversation_list", "1");
        if (!pinType.equals("1")) {
            if (pinType.equals("2")) {
                setContentView(R.layout.activity_main_phone);
            } else if (pinType.equals("3")) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                    setContentView(R.layout.activity_main_phablet2);
                } else {
                    setContentView(R.layout.activity_main_phablet);
                }
            } else {
                setContentView(R.layout.activity_main_tablet);
            }
        } else {
            setContentView(R.layout.activity_main);
        }

        setTitle(null);

        ColorDrawable background = new ColorDrawable();
        background.setColor(getResources().getColor(R.color.black));
        background.setAlpha(20);
        getWindow().setBackgroundDrawable(background);
        int scale1 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
        int scale2 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        findViewById(R.id.pager).getRootView().setPadding(scale1, scale2, scale1, scale2);
    }
    
    @Override
    public void setUpIntentStuff() {
        // TODO test to make sure working to open correct conversation through quick send and full app popup
        // Do nothing, just open to the first conversation no matter what is sent into the activity
    }
    
    @Override
    public void setUpTitleBar() {
		title = (PagerTitleStrip) findViewById(R.id.pager_title_strip);
		
		if (sharedPrefs.getString("page_or_menu2", "2").equals("1"))
		{
			title.setTextSpacing(5000);
		}
		
		if (!sharedPrefs.getBoolean("custom_theme", false))
        {
        	if (sharedPrefs.getBoolean("title_text_color", false))
        	{
        		title.setTextColor(getResources().getColor(R.color.black));
        	}
        } else
        {
        	title.setTextColor(sharedPrefs.getInt("ct_titleBarTextColor", getResources().getColor(R.color.white)));
        }
        
        if (!sharedPrefs.getBoolean("title_caps", true))
        {
        	title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        }
		
        if (!sharedPrefs.getBoolean("custom_theme", false))
        {
            String titleColor = sharedPrefs.getString("title_color", "blue");
            
            if (titleColor.equals("blue"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.holo_blue));
            } else if (titleColor.equals("orange"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.holo_orange));
            } else if (titleColor.equals("red"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.holo_red));
            } else if (titleColor.equals("green"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.holo_green));
            } else if (titleColor.equals("purple"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.holo_purple));
            } else if (titleColor.equals("grey"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.grey));
            } else if (titleColor.equals("black"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.pitch_black));
            } else if (titleColor.equals("darkgrey"))
            {
                title.setBackgroundColor(getResources().getColor(R.color.darkgrey));
            }
        } else
        {
            title.setBackgroundColor(sharedPrefs.getInt("ct_titleBarColor", getResources().getColor(R.color.holo_blue)));
        }
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // TODO handle keyboard changes so that padding is set to 0 on bottom when keyboard is shown and 100 when keyboard is hidden
    }
    
    @Override
    public void onResume() {
        super.onResume();
        
        // TODO test keyboard popping up
        if (sharedPrefs.getBoolean("show_keyboard_popup", true)) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(messageEntry), 0);
                }
            }, 200);
        }
    }
    
    @Override
    public void onStop() {
        // TODO test if conversations updated when returning to full app
        super.onStop();
        MainActivity.newMessage = true;
        com.klinker.android.messaging_donate.MainActivity.group = null;
        com.klinker.android.messaging_donate.MainActivity.inboxBody = null;
        com.klinker.android.messaging_donate.MainActivity.inboxDate = null;
        com.klinker.android.messaging_donate.MainActivity.inboxNumber = null;
        com.klinker.android.messaging_donate.MainActivity.msgCount = null;
        com.klinker.android.messaging_donate.MainActivity.msgRead = null;
        com.klinker.android.messaging_donate.MainActivity.threadIds = null;
        finish();
    }
}
