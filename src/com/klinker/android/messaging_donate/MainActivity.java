package com.klinker.android.messaging_donate;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.WindowManager;
import com.klinker.android.messaging_donate.receivers.UnlockReceiver;
import com.klinker.android.messaging_sliding.receivers.CacheService;
import com.klinker.android.messaging_sliding.slide_over.SlideOverService;
import wizardpager.ChangeLogMain;
import wizardpager.InitialSetupMain;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	public SharedPreferences sharedPrefs;
    public static ArrayList<String> threadIds, msgCount, msgRead, inboxBody, inboxDate, inboxNumber, group;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        if(sharedPrefs.getBoolean("slideover_enabled",false))
        {
            if(!isSlideOverRunning())
            {
                Intent service = new Intent(getApplicationContext(), com.klinker.android.messaging_sliding.slide_over.SlideOverService.class);
                startService(service);
            }
        }

        setContentView(R.layout.loading_screen);
		
		NotificationManager mNotificationManager =
	            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);
        
        Map<Long, String[]> fnMessages = com.klinker.android.messaging_donate.floating_notifications.FNReceiver.messages;
        
        if (fnMessages != null) {
            if (fnMessages.size() > 0) {
                Set<Long> keys = fnMessages.keySet();
                
                for (Long ii: keys) {
                    robj.floating.notifications.Extension.remove(ii, this);
                }  
            }
        }

        if (sharedPrefs.getBoolean("cache_conversations", true) && CacheService.cached) {
            onLoadFinished(null, CacheService.conversationList);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
	}

    private boolean isSlideOverRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (SlideOverService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void launchActivity()
    {
        UnlockReceiver.openApp = false;

        Intent fromIntent = getIntent();

        String version = "";

        try {
            version = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (!sharedPrefs.getString("current_version", "0").equals(version))
        {
            if(sharedPrefs.getString("current_version", "0").equals("0"))
            {
                Intent initialSetupIntent = new Intent(getApplicationContext(), InitialSetupMain.class);
                initialSetupIntent.setAction(fromIntent.getAction());
                initialSetupIntent.setData(fromIntent.getData());
                startActivity(initialSetupIntent);
            } else
            {
                Intent changeLogIntent = new Intent(getApplicationContext(), ChangeLogMain.class);
                changeLogIntent.setAction(fromIntent.getAction());
                changeLogIntent.setData(fromIntent.getData());
                changeLogIntent.putExtra("version", version);
                startActivity(changeLogIntent);
            }

            finish();
            overridePendingTransition(0, 0);
        } else
        {
            boolean flag = false;

            if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
            {
                flag = true;
            }

            final Intent intent = new Intent(this, com.klinker.android.messaging_sliding.MainActivity.class);
            intent.setAction(fromIntent.getAction());
            intent.setData(fromIntent.getData());

            try
            {
                intent.putExtras(fromIntent.getExtras());
            } catch (Exception e)
            {

            }

            if (flag)
            {
                intent.putExtra("com.klinker.android.OPEN", intent.getStringExtra("com.klinker.android.OPEN"));
            }

            startActivity(intent);
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle)
    {
        String[] projection = new String[]{"_id", "date", "message_count", "recipient_ids", "snippet", "read"};
        Uri uri = Uri.parse("content://mms-sms/conversations/?simple=true");

        return new CursorLoader(
                getBaseContext(),
                uri,
                projection,
                null,
                null,
                "date desc"
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor query)
    {
        inboxNumber = new ArrayList<String>();
        inboxDate = new ArrayList<String>();
        inboxBody = new ArrayList<String>();
        threadIds = new ArrayList<String>();
        group = new ArrayList<String>();
        msgCount = new ArrayList<String>();
        msgRead = new ArrayList<String>();

        if (query.moveToFirst())
        {
            do
            {
                threadIds.add(query.getString(query.getColumnIndex("_id")));
                msgCount.add(query.getString(query.getColumnIndex("message_count")));
                msgRead.add(query.getString(query.getColumnIndex("read")));

                inboxBody.add(" ");

                try
                {
                    inboxBody.set(inboxBody.size() - 1, query.getString(query.getColumnIndex("snippet")).replaceAll("\\\n", " "));
                } catch (Exception e)
                {
                }

                inboxDate.add(query.getString(query.getColumnIndex("date")));

                inboxNumber.add(query.getString(query.getColumnIndex("recipient_ids")));

                if (query.getString(query.getColumnIndex("recipient_ids")).split(" ").length > 1)
                {
                    group.add("yes");
                } else
                {
                    group.add("no");
                }
            } while (query.moveToNext());
        }

        launchActivity();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}