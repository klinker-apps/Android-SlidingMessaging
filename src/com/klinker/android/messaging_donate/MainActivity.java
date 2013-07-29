package com.klinker.android.messaging_donate;

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

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements LoaderManager.LoaderCallbacks<Cursor> {
	
	public SharedPreferences sharedPrefs;
    public static ArrayList<String> threadIds, msgCount, msgRead, inboxBody, inboxDate, inboxNumber, group;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.loading_screen);
		
		NotificationManager mNotificationManager =
	            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(1);

        if (sharedPrefs.getBoolean("cache_conversations", true) && CacheService.cached) {
            onLoadFinished(null, CacheService.conversationList);
        } else {
            getSupportLoaderManager().initLoader(0, null, this);
        }
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
            Intent wizardintent = new Intent(getApplicationContext(), wizardpager.MainActivity.class);
            wizardintent.setAction(fromIntent.getAction());
            wizardintent.setData(fromIntent.getData());
            wizardintent.putExtra("version", version);
            startActivity(wizardintent);
            finish();
            overridePendingTransition(0, 0);
        } else
        {
            boolean flag = false;

            if (fromIntent.getStringExtra("com.klinker.android.OPEN") != null)
            {
                flag = true;
            }

            if (sharedPrefs.getString("run_as", "sliding").equals("sliding") || sharedPrefs.getString("run_as", "sliding").equals("hangout")  || sharedPrefs.getString("run_as", "sliding").equals("card2"))
            {
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
            } else if (sharedPrefs.getString("run_as", "sliding").equals("card"))
            {
                final Intent intent = new Intent(this, com.klinker.android.messaging_card.MainActivity.class);
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