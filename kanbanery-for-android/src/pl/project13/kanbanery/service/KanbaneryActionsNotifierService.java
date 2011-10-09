package pl.project13.kanbanery.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.xml.ProjectLogEntry;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.ui.activities.ColumnsActivity;
import roboguice.service.RoboService;

import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Konrad Malawski
 */
public class KanbaneryActionsNotifierService extends RoboService
{

   private static final int KANBANERY_ACTION_ID = 1;
   private static final String REFRESH_INTERVAL = "refresh.interval";

   @Inject
   private Janbanery janbanery;

   @Inject
   private NotificationManager notificationManager;

   @Inject
   private SharedPreferences preferences;

   private Timer timer = new Timer();

   private static final int sec = 1000;
   private static final int min = 60 * sec;

   private List<Integer> alreadyDisplayedActionHashCodes = newArrayList();

   @Override
   public void onCreate()
   {
      super.onCreate();

      start();
   }

   @Override
   public void onDestroy()
   {
      super.onDestroy();

      stop();
   }

   public IBinder onBind(Intent intent)
   {
      return null;
   }

   private void start()
   {
      // todo implement me
//      long refreshInterval = preferences.getLong(REFRESH_INTERVAL, min);
//      FetchKanbaneryActionsTimerTask fetchActionsTask = new FetchKanbaneryActionsTimerTask();
//      timer.scheduleAtFixedRate(fetchActionsTask, min, refreshInterval);
   }

   private void stop()
   {
//      timer.cancel();
   }

   public void showNotification(String title, String message)
   {
      int icon = R.drawable.ic_kanbanery;
      long when = System.currentTimeMillis();

      Notification notification = new Notification(icon, title, when);

      Context context = getApplicationContext();

      Intent notificationIntent = new Intent(this, ColumnsActivity.class); // open columns on click
      PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

      notification.setLatestEventInfo(context, title, message, contentIntent);
      notification.flags = Notification.FLAG_AUTO_CANCEL;

      notificationManager.notify(KANBANERY_ACTION_ID, notification);
   }

   private class FetchKanbaneryActionsTimerTask extends TimerTask
   {

      @Override
      public void run()
      {
         List<ProjectLogEntry> lastTenActions = janbanery.log().last(10);
         filterOutNewActions(alreadyDisplayedActionHashCodes, lastTenActions);
      }

      private List<ProjectLogEntry> filterOutNewActions(final List<Integer> alreadyDisplayedActionHashCodes, List<ProjectLogEntry> newActions)
      {
         Collection<ProjectLogEntry> onlyNewActions = Collections2.filter(newActions, new Predicate<ProjectLogEntry>()
         {
            @Override
            public boolean apply(ProjectLogEntry entry)
            {
               return !alreadyDisplayedActionHashCodes.contains(entry.hashCode());
            }
         });

         return newArrayList(onlyNewActions);
      }
   }

   @VisibleForTesting
   void notifyAbout(ProjectLogEntry action)
   {
      String title = action.getTitle();
      String description = action.getDescription();
      showNotification(title, description);
   }
}
