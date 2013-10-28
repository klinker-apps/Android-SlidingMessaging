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
import android.support.v4.view.PagerTitleStrip;
import android.util.TypedValue;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.receivers.NotificationRepeaterService;
import com.klinker.android.messaging_sliding.slide_over.SlideOverService;

public class MainActivityPopup extends MainActivity {

    public boolean fromHalo = false;
    public boolean fromWidget = false;
    public boolean secondaryAction = false;
    public boolean multipleNew = false;
    public int openTo = 0;
    public int originalHeight = 0;

    private LinearLayout.LayoutParams params;

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
        isPopup = true;
        attachOnSend = true;

        if (sharedPrefs.getBoolean("disable_backgrounds", true)) {
            settings.customBackground = false;
            settings.customBackground2 = false;
        }

        if (sharedPrefs.getBoolean("unlock_screen", false)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            unlockDevice = true;
        }

        requestWindowFeature(Window.FEATURE_ACTION_BAR);

        if (settings.lightActionBar) {
            setTheme(R.style.HangoutsThemeDialog);
        } else {
            setTheme(R.style.AppBaseThemeDialog);
        }

        setContentView(R.layout.activity_main);

        setTitle(getResources().getString(R.string.app_name_in_app));

        ColorDrawable background = new ColorDrawable();
        background.setColor(resources.getColor(android.R.color.transparent));
        getWindow().setBackgroundDrawable(background);

        setUpBounds();

        ab = getActionBar();

        ab.setDisplayUseLogoEnabled(false);
        ab.setDisplayShowHomeEnabled(false);
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

        if (settings.pageorMenu2) {
            title.setTextSpacing(5000);
        }

        if (!settings.customTheme) {
            if (settings.titleTextColor) {
                title.setTextColor(resources.getColor(R.color.black));
            }
        } else {
            title.setTextColor(settings.titleBarTextColor);
        }

        if (!settings.titleCaps) {
            title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        }

        if (settings.useTitleBar) {
            if (!settings.customTheme) {
                String titleColor = sharedPrefs.getString("title_color", "blue");

                if (titleColor.equals("blue")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_blue));
                } else if (titleColor.equals("orange")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_orange));
                } else if (titleColor.equals("red")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_red));
                } else if (titleColor.equals("green")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_green));
                } else if (titleColor.equals("purple")) {
                    title.setBackgroundColor(resources.getColor(R.color.holo_purple));
                } else if (titleColor.equals("grey")) {
                    title.setBackgroundColor(resources.getColor(R.color.grey));
                } else if (titleColor.equals("black")) {
                    title.setBackgroundColor(resources.getColor(R.color.pitch_black));
                } else if (titleColor.equals("darkgrey")) {
                    title.setBackgroundColor(resources.getColor(R.color.darkgrey));
                }
            } else {
                title.setBackgroundColor(settings.titleBarColor);
            }
        } else {
            title.setVisibility(View.GONE);
        }
    }

    public void setUpBounds() {
        /*Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        originalHeight = getWindow().getAttributes().height;

        if (width > height) {
            getWindow().getDecorView().setPadding(width/7, height / 12, width/7, height / 12);
        } else {
            int dividend = (int) (16 * (sharedPrefs.getInt("slideover_padding", 50) / 100.0));
            int sidePadding = (int) ((width * ((double)sharedPrefs.getInt("slideover_width_padding", 10) / 100)) / 2);

            try {
                getWindow().getDecorView().setPadding(sidePadding, height / dividend, sidePadding, height / dividend);
            } catch (Exception e) {

            }
        }*/

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        if (height > width) {
            getWindow().setLayout((width * sharedPrefs.getInt("slideover_width", 90) / 100), (height * sharedPrefs.getInt("slideover_height", 80)/100));
        } else {
            getWindow().setLayout((int) (width * .7), (int) (height * .8));
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        setUpBounds();

    }

    @Override
    public void onResume() {
        super.onResume();

        SlideOverService.restartHalo(this);

        //mDrawerLayout.setLayoutParams(params);

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
                                    mDrawerLayout.openDrawer(Gravity.RIGHT);
                                }
                            }, 500);
                        }
                    } else {
                        openTo = getIntent().getIntExtra("openToPage", 0);

                        try {
                            menu.showContent();
                            mViewPager.setCurrentItem(openTo);
                        } catch (Exception e) {
                            // doesn't want to open to the correct page. null pointer on the menu object, don't know why i guess
                        }

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
            if (menu != null) {
                menu.showContent();
            }
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (imageAttach.getVisibility() == View.VISIBLE) {
                    int page = sharedPrefs.getInt("slideover_attaching_to", 0);
                    menu.showContent();

                    mViewPager.setCurrentItem(page);
                }
            }
        }, 500);

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

    public int toDP(int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, SlideOverService.displayMatrix);
    }
}
