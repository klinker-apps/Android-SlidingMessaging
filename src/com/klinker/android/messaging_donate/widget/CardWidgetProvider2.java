package com.klinker.android.messaging_donate.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.view.View;
import android.widget.RemoteViews;
import com.klinker.android.messaging_donate.MainActivity;
import com.klinker.android.messaging_donate.R;

import java.io.*;
import java.util.ArrayList;

public class CardWidgetProvider2 extends AppWidgetProvider {

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Intent updateWidget = new Intent(context, CardWidgetService3.class);
        context.startService(updateWidget);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.klinker.android.messaging.UPDATE_WIDGET"))
        {
            Intent updateWidget = new Intent(context, CardWidgetService3.class);
            context.startService(updateWidget);
        } else
        {
            super.onReceive(context, intent);
        }
    }

    public static class CardWidgetService3 extends IntentService
    {
        public CardWidgetService3() {
            super("card_widget_service");
        }

        @Override
        public IBinder onBind(Intent arg0) {
            return null;
        }

        @Override
        protected void onHandleIntent(Intent arg0) {
            AppWidgetManager mgr = AppWidgetManager.getInstance(this);
            ComponentName thisAppWidget = new ComponentName(this.getPackageName(), CardWidgetProvider2.class.getName());
            int[] appWidgetIds = mgr.getAppWidgetIds(thisAppWidget);

            for (int i=0; i<appWidgetIds.length; i++) {
                int appWidgetId = appWidgetIds[i];

                Intent quickText = new Intent(this, MainActivity.class);
                quickText.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent quickPending = PendingIntent.getActivity(this, 0, quickText, 0);

                ArrayList<String> newMessages = readFromFile(this);

                RemoteViews views = new RemoteViews(this.getPackageName(), R.layout.unread_widget);

                views.setOnClickPendingIntent(R.id.launcher, quickPending);
                views.setTextViewText(R.id.unread, newMessages.size() + "");
                views.setTextColor(R.id.unread, getResources().getColor(R.color.white));

                if (newMessages.size() == 0)
                {
                    views.setViewVisibility(R.id.unread, View.INVISIBLE);
                } else
                {
                    views.setViewVisibility(R.id.unread, View.VISIBLE);
                }

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

        private ArrayList<String> readFromFile(Context context) {

            ArrayList<String> ret = new ArrayList<String>();

            try {
                InputStream inputStream = context.openFileInput("newMessages.txt");

                if ( inputStream != null ) {
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String receiveString = "";

                    while ( (receiveString = bufferedReader.readLine()) != null ) {
                        ret.add(receiveString);
                    }

                    inputStream.close();
                }
            }
            catch (FileNotFoundException e) {

            } catch (IOException e) {

            }

            return ret;
        }

    }
}