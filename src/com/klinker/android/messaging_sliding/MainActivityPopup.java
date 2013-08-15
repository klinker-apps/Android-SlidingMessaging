package com.klinker.android.messaging_sliding;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;

import java.util.ArrayList;

public class MainActivityPopup extends MainActivity {

    public boolean fromHalo = false;
    public boolean fromWidget = false;
    public boolean secondaryAction = false;
    public boolean multipleNew = false;
    public int openTo = 0;

    private BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (!multipleNew) {
                finish();
                unregisterReceiver(this);
            }
        }
    };

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
        attachOnSend = true;

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (sharedPrefs.getBoolean("unlock_screen", false))
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            unlockDevice = true;
        }

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
            int dividend = (int)(16 * (sharedPrefs.getInt("slideover_padding", 50)/100.0));

            try {
                getWindow().getDecorView().setPadding(width / 20, height / dividend, width / 20, height / dividend);
            } catch (Exception e) {

            }
        }
    }
    
    @Override
    public void setUpIntentStuff() {
        fromHalo = getIntent().getBooleanExtra("fromHalo", false);
        secondaryAction = getIntent().getBooleanExtra("secAction", false);
        fromWidget = getIntent().getBooleanExtra("fromWidget", false);
        openTo = getIntent().getIntExtra("openToPage", 0);
        multipleNew = getIntent().getBooleanExtra("multipleNew", false);
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
            int dividend = (int)(16 * (sharedPrefs.getInt("slideover_padding", 50)/100.0));
            getWindow().getDecorView().setPadding(width / 20, height / dividend, width / 20, height / dividend);
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();

        if (!getIntent().getBooleanExtra("fromNotification", false)) {
            if (!fromWidget) {
                if (!fromHalo) {
                    menu.showContent();
                } else {
                    if (secondaryAction) {
                        if (getIntent().getStringExtra("secondaryType").equals("conversations")) {
                            menu.showMenu();
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    menu.showSecondaryMenu();
                                }
                            }, 500);
                        }
                    } else
                    {
                        openTo = getIntent().getIntExtra("openToPage", 0);

                        menu.showContent();
                        mViewPager.setCurrentItem(openTo);
                    }
                }
            } else {
                new Handler().postDelayed(new Runnable() {
                   @Override
                   public void run() {
                       menu.showSecondaryMenu();
                   }
                }, 500);
            }
        } else {
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

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.klinker.android.messaging.CLOSE_POPUP");
        registerReceiver(closeReceiver, filter);
    }
    
    @Override
    public void onStop() {
        super.onStop();

        try {
            unregisterReceiver(closeReceiver);
        } catch (Exception e) {
            // already closed?
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        final Context context = this;

        // be sure that notifications are always dismissed after closing slideover
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManager mNotificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.cancel(1);
                mNotificationManager.cancel(2);

                Intent intent = new Intent("com.klinker.android.messaging.CLEARED_NOTIFICATION");
                context.sendBroadcast(intent);

                Intent stopRepeating = new Intent(context, NotificationRepeaterService.class);
                PendingIntent pStopRepeating = PendingIntent.getService(context, 0, stopRepeating, 0);
                AlarmManager alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                alarm.cancel(pStopRepeating);
            }
        }, 500);
    }
}
