package com.klinker.android.messaging_sliding;

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.Window;
import com.klinker.android.messaging_donate.R;

public class MainActivityPopup extends MainActivity {

    @Override
    public void setUpWindow() {
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

        // TODO implement show keyboard on startup with handler and posting delayed
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // TODO handle keyboard changes so that padding is set to 0 on bottom when keyboard is shown and 100 when keyboard is hidden
    }
}
