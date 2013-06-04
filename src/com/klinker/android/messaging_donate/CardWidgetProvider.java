package com.klinker.android.messaging_donate;

import android.app.IntentService;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.Toast;

public class CardWidgetProvider extends AppWidgetProvider {

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	Intent updateWidget = new Intent(context, CardWidgetService2.class);
        context.startService(updateWidget);

        SharedPreferences sharedPrefs  = PreferenceManager.getDefaultSharedPreferences(context);

        RemoteViews views = new RemoteViews(context.getPackageName(),
                R.layout.card_widget);

        if (sharedPrefs.getBoolean("widget_background", true))
        {
            if(sharedPrefs.getBoolean("dark_background", false))
            {
                views.setImageViewResource(R.id.widget_background,
                        R.drawable.widget_background_dark);

                if (sharedPrefs.getBoolean("widget_dark_theme", false))
                {
                    views.setImageViewResource(R.id.widget_card,
                            R.drawable.widget_card_dark);
                } else
                {
                    views.setImageViewResource(R.id.widget_card,
                            R.drawable.widget_card);
                }

            } else
            {
                views.setImageViewResource(R.id.widget_background,
                        R.drawable.widget_background);

                if (sharedPrefs.getBoolean("widget_dark_theme", false))
                {
                    views.setImageViewResource(R.id.widget_card,
                            R.drawable.widget_card_dark);
                } else
                {
                    views.setImageViewResource(R.id.widget_card,
                            R.drawable.widget_card);
                }
            }
        } else
        {
            views.setImageViewResource(R.id.widget_background,
                    R.drawable.widget_background_transparent);

            if (sharedPrefs.getBoolean("widget_dark_theme", false))
            {
                views.setImageViewResource(R.id.widget_card,
                        R.drawable.widget_card_dark);
            } else
            {
                views.setImageViewResource(R.id.widget_card,
                        R.drawable.widget_card);
            }
        }

        for (int i = 0; i < appWidgetIds.length; i++) {
            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }
    
	@Override
    public void onReceive(Context context, Intent intent) {
    	if (intent.getAction().equals("com.klinker.android.messaging.UPDATE_WIDGET"))
    	{
	        Intent updateWidget = new Intent(context, CardWidgetService2.class);
	        context.startService(updateWidget);
    	} else if (intent.getAction().equals("OPEN_APP")) {
            String conversation = intent.getStringExtra("CONVERSATION_TO_OPEN");

            Intent open = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+conversation));
            open.setClass(context, com.klinker.android.messaging_donate.WidgetProxyActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
//            final KeyguardManager.KeyguardLock keyguardLock =  keyguardManager.newKeyguardLock("TAG");
//            keyguardLock.disableKeyguard();
//
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try
//                    {
//                        Thread.sleep(1000);
//                    } catch (Exception e)
//                    {
//
//                    }
//
//                    keyguardLock.reenableKeyguard();
//                }
//            }).start();
            
            context.startActivity(open);
        } else
        {
        	super.onReceive(context, intent);
        }
    }

    public static class CardWidgetService2 extends IntentService
    {
    	public CardWidgetService2() {
    		super("card_widget_service");
    	}

		@Override
		public IBinder onBind(Intent arg0) {
			return null;
		}

		@Override
		protected void onHandleIntent(Intent arg0) {
			AppWidgetManager mgr = AppWidgetManager.getInstance(this);
	        ComponentName thisAppWidget = new ComponentName(this.getPackageName(), CardWidgetProvider.class.getName());
	        int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);
	          
	        for (int i=0; i<appWidgetIds.length; i++) {
	        	int appWidgetId = appWidgetIds[i];
	        	
	            Intent quickText = new Intent(this, com.klinker.android.messaging_sliding.SendMessage.class);
	            PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent settings = new Intent(this, com.klinker.android.messaging_donate.CardWidgetSettingsActivity.class);
                PendingIntent settingsPending = PendingIntent.getActivity(this, 0, settings, 0);

	            Intent intent2 = new Intent(this, com.klinker.android.messaging_donate.CardWidgetService.class);
	            intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
	            intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

	            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.card_widget);
	            views.setRemoteAdapter(R.id.widgetList, intent2);
	            views.setEmptyView(R.id.widgetList, R.drawable.widget_background);
	            
	            views.setOnClickPendingIntent(R.id.replyButton, quickPending);
                views.setOnClickPendingIntent(R.id.settingsButton, settingsPending);
	            
	            Intent openIntent = new Intent(this, CardWidgetProvider.class);
	            openIntent.setAction("OPEN_APP");
	            openIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
	            openIntent.setData(Uri.parse(openIntent.toUri(Intent.URI_INTENT_SCHEME)));
	            PendingIntent openPendingIntent = PendingIntent.getBroadcast(this, 0, openIntent,
	                PendingIntent.FLAG_UPDATE_CURRENT);
	            views.setPendingIntentTemplate(R.id.widgetList, openPendingIntent);

	            mgr.updateAppWidget(appWidgetId, views);
	            
	            try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	            
	            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widgetList);
	        }
			
	        stopSelf();
		}
    	
    }
}