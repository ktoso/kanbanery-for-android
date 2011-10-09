package pl.project13.kanbanery.ui.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.ui.activities.ColumnsActivity;

/**
 * @author Konrad Malawski
 */
public class KanbaneryWidgetProvider extends AppWidgetProvider
{

   public static final String UPDATE_ON_DEMAND = "pl.project13.kanbanery.intent.appwidget.UPDATE_ON_DEMAND";

   @Override
   public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
   {
      final int N = appWidgetIds.length;

      // Perform this loop procedure for each App Widget that belongs to this providers
      for (int i = 0; i < N; i++)
      {
         int appWidgetId = appWidgetIds[i];
         populateView(context, appWidgetManager, appWidgetId);
      }
   }

   @Override
   public void onReceive(Context context, Intent intent)
   {
      if (intent.getAction().equals(UPDATE_ON_DEMAND))
      {
         Bundle extras = intent.getExtras();

         AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
         int[] appWidgetIds = appWidgetManager.getAppWidgetIds(intent.getComponent());
         onUpdate(context, appWidgetManager, appWidgetIds);
      }
      else
      {
         super.onReceive(context, intent);
      }
   }

   private void populateView(Context context, AppWidgetManager appWidgetManager, int appWidgetId)
   {
      // Create an Intent to launch ColumnsActivity
      Intent intent = new Intent(context, ColumnsActivity.class);
      PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

      // Get the layout for the App Widget and attach an on-click listener to the button
      RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
      views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

      // Tell the AppWidgetManager to perform an update on the current app widget
      appWidgetManager.updateAppWidget(appWidgetId, views);
   }
}
