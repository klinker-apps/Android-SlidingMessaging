package com.klinker.android.messaging_donate.widget;

import android.app.IntentService;
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
import android.view.View;
import android.widget.RemoteViews;

import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;
import com.klinker.android.messaging_sliding.quick_reply.SendMessage;

public class CardWidgetProvider extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, CardWidgetService2.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.messaging.UPDATE_WIDGET")) {
            Intent updateWidget = new Intent(context, CardWidgetService2.class);
            context.startService(updateWidget);
        } else if (intent.getAction().equals("OPEN_APP")) {
            String conversation = intent.getStringExtra("CONVERSATION_TO_OPEN");

            Intent open = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + conversation));
            open.setClass(context, WidgetProxyActivity.class);
            open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            context.startActivity(open);
        } else {
            super.onReceive(context, intent);
        }
    }

    public static class CardWidgetService2 extends IntentService {
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

            RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.card_widget);

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

            // color of the card at the top of the widget (white or black)
            // and hiding the icons
            if (sharedPrefs.getBoolean("widget_title_dark_theme", false)) {
                views.setImageViewResource(R.id.widget_card,
                        R.drawable.widget_card_dark);

                views.setViewVisibility(R.id.settingsButton, View.INVISIBLE);
                views.setViewVisibility(R.id.replyButton, View.INVISIBLE);
                views.setViewVisibility(R.id.slideOverButton, View.INVISIBLE);

                views.setViewVisibility(R.id.settingsButtonDark, View.VISIBLE);
                views.setViewVisibility(R.id.replyButtonDark, View.VISIBLE);
                views.setViewVisibility(R.id.slideOverButtonDark, View.VISIBLE);

                views.setTextColor(R.id.textView1, getResources().getColor(R.color.white));
            } else {
                views.setImageViewResource(R.id.widget_card,
                        R.drawable.widget_card);

                views.setViewVisibility(R.id.settingsButtonDark, View.INVISIBLE);
                views.setViewVisibility(R.id.replyButtonDark, View.INVISIBLE);
                views.setViewVisibility(R.id.slideOverButtonDark, View.INVISIBLE);

                views.setViewVisibility(R.id.settingsButton, View.VISIBLE);
                views.setViewVisibility(R.id.replyButton, View.VISIBLE);
                views.setViewVisibility(R.id.slideOverButton, View.VISIBLE);

                views.setTextColor(R.id.textView1, getResources().getColor(R.color.light_grey));
            }

            // background color of the widget (transparent, white, or black)
            if (sharedPrefs.getBoolean("widget_background", true)) {
                if (sharedPrefs.getBoolean("dark_background", false)) {
                    views.setImageViewResource(R.id.widget_background,
                            R.drawable.widget_background_dark);
                } else {
                    views.setImageViewResource(R.id.widget_background,
                            R.drawable.widget_background);
                }
            } else {
                views.setImageViewResource(R.id.widget_background,
                        R.drawable.widget_background_transparent);
            }

            for (int i = 0; i < appWidgetIds.length; i++) {
                mgr.updateAppWidget(appWidgetIds[i], views);
            }

            for (int i = 0; i < appWidgetIds.length; i++) {
                int appWidgetId = appWidgetIds[i];

                Intent quickText = new Intent(this, SendMessage.class);

                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                Intent settings = new Intent(this, CardWidgetSettingsActivity.class);
                PendingIntent settingsPending = PendingIntent.getActivity(this, 0, settings, 0);

                Intent slideOver = new Intent(this, com.klinker.android.messaging_sliding.slide_over.SlideOverSettings.class);
                PendingIntent slideoverPending = PendingIntent.getActivity(this, 0, slideOver, 0);

                Intent intent2 = new Intent(this, CardWidgetService.class);
                intent2.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                intent2.setData(Uri.parse(intent2.toUri(Intent.URI_INTENT_SCHEME)));

                Intent openApp = new Intent(this, MainActivity.class);
                PendingIntent openAppPending = PendingIntent.getActivity(this, 0, openApp, 0);


                views.setRemoteAdapter(R.id.widgetList, intent2);
                views.setEmptyView(R.id.widgetList, R.drawable.widget_background);

                views.setOnClickPendingIntent(R.id.textView1, openAppPending);

                views.setOnClickPendingIntent(R.id.replyButton, quickPending);
                views.setOnClickPendingIntent(R.id.settingsButton, settingsPending);
                views.setOnClickPendingIntent(R.id.slideOverButton, slideoverPending);

                views.setOnClickPendingIntent(R.id.replyButtonDark, quickPending);
                views.setOnClickPendingIntent(R.id.settingsButtonDark, settingsPending);
                views.setOnClickPendingIntent(R.id.slideOverButtonDark, slideoverPending);

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