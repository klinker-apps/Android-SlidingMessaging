package com.klinker.android.messaging_sliding;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.PagerTitleStrip;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;

import com.klinker.android.messaging_donate.R;

public class MainActivityPopup extends MainActivity {

    public boolean fromHalo = false;

    @Override
    public void setUpWindow() {
        com.klinker.android.messaging_donate.MainActivity.group = null;
        com.klinker.android.messaging_donate.MainActivity.inboxBody = null;
        com.klinker.android.messaging_donate.MainActivity.inboxDate = null;
        com.klinker.android.messaging_donate.MainActivity.inboxNumber = null;
        com.klinker.android.messaging_donate.MainActivity.msgCount = null;
        com.klinker.android.messaging_donate.MainActivity.msgRead = null;
        com.klinker.android.messaging_donate.MainActivity.threadIds = null;
        
        isPopup = true;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        if (sharedPrefs.getBoolean("ct_light_action_bar", false))
        {
            setTheme(R.style.HangoutsThemeDialog);
        }

        setContentView(R.layout.activity_main);

        setTitle(null);

        ColorDrawable background = new ColorDrawable();
        background.setColor(getResources().getColor(android.R.color.transparent));
        getWindow().setBackgroundDrawable(background);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        if (width > height) {
            getWindow().getDecorView().setPadding(0,height/12,0,height/12);
        } else {
            getWindow().getDecorView().setPadding(width / 20, height / 8, width / 20, height / 8);
        }
    }
    
    @Override
    public void setUpIntentStuff() {
        fromHalo = getIntent().getBooleanExtra("fromHalo", false);
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

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        if (width > height) {
            getWindow().getDecorView().setPadding(0,height/12,0,height/12);
        } else {
            getWindow().getDecorView().setPadding(width / 20, height / 8, width / 20, height / 8);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (!fromHalo) {
            menu.showContent();
        }

        if (sharedPrefs.getBoolean("show_keyboard_popup", true) && !fromHalo) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                        InputMethodManager keyboard = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(messageEntry, 0);
                }
            }, 500);
        }
    }
    
    @Override
    public void onStop() {
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
