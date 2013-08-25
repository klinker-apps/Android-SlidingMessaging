package com.klinker.android.messaging_donate.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import com.klinker.android.messaging_donate.R;

/**
 * Created by luke on 8/12/13.
 */
public class SlideoverWidget extends AppWidgetProvider {

    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds )
    {
        RemoteViews remoteViews;
        ComponentName watchWidget;

        Intent slideOver = new Intent(context, com.klinker.android.messaging_sliding.slide_over.SlideOverSettings.class);
        PendingIntent slideoverPending = PendingIntent.getActivity(context, 0, slideOver, 0);

        remoteViews = new RemoteViews(context.getPackageName(), R.layout.halo_widget);
        watchWidget = new ComponentName( context, SlideoverWidget.class );
        remoteViews.setOnClickPendingIntent(R.id.button, slideoverPending);
        appWidgetManager.updateAppWidget( watchWidget, remoteViews );
    }
}
