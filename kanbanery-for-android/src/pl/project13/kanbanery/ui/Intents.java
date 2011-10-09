package pl.project13.kanbanery.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.ui.activities.WorkspacesAndProjectsActivity;
import pl.project13.kanbanery.ui.activities.dialog.AssignTaskDialog;
import pl.project13.kanbanery.ui.activities.dialog.GetPremiumVersionDialog;
import pl.project13.kanbanery.ui.activities.dialog.NewColumnDialog;
import pl.project13.kanbanery.ui.activities.dialog.NewCommentDialog;
import pl.project13.kanbanery.ui.activities.dialog.NewSubTaskDialog;
import pl.project13.kanbanery.ui.activities.dialog.NewTaskDialog;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ags on 8/20/11 at 8:42 PM
 */
public class Intents
{

   private static final String TAG = Intents.class.getSimpleName();

   public static boolean matchesAction(Intent intent, String matchActionName)
   {
      String intentAction = intent.getAction();
      return intentAction.equals(matchActionName);
   }

   public static class CleanImageCache
   {
      public static final String ACTION_NAME = "pl.project13.kanbanery.service.CLEAN_IMAGE_CACHE";
   }

   public static class ShowNewComment
   {
      public static final int CODE = 772;

      private static final String TASK = "task";

      public static Intent createWith(Activity creator, Task task)
      {
         Intent intent = new Intent(creator, NewCommentDialog.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Intent prepareResponse(Task task)
      {
         Intent intent = new Intent();
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Task getDataFrom(Intent intent)
      {
         return getTaskFromIntent(intent);
      }
   }

   public static class ShowNewSubTask
   {
      public static final int CODE = 773;

      private static final String TASK = "task";

      public static Intent createWith(Activity creator, Task task)
      {
         Intent intent = new Intent(creator, NewSubTaskDialog.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Intent prepareResponse(Task task)
      {
         Intent intent = new Intent();
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Task getDataFrom(Intent intent)
      {
         return getTaskFromIntent(intent);
      }
   }

   public static class ShowNewColumn
   {
      public static final int CODE = 775;
      private static final String DATA = "data";
      private static final String COLUMN = "column";

      public static Intent createWith(Activity creator, List<Column> columns)
      {
         Intent intent = new Intent(creator, NewColumnDialog.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         intent.putExtra(DATA, new Data(columns));

         return intent;
      }

      public static Data getDataFrom(Intent intent)
      {
         try
         {
            Bundle extras = intent.getExtras();
            Data data = (Data) extras.getSerializable(DATA);

            return data;
         }
         catch (Exception e)
         {
            Log.e(TAG, "Unable to fetch data from intent, it may just be empty!");
            return null;
         }
      }

      public static Intent prepareResponse(Column column)
      {
         Intent intent = new Intent();
         intent.putExtra(COLUMN, column);

         return intent;
      }

      public static Column getDataFromResponse(Intent data)
      {
         try
         {
            Bundle extras = data.getExtras();
            Column column = (Column) extras.getSerializable(COLUMN);
            return column;
         }
         catch (Exception e)
         {
            Log.e(TAG, "Unable to fetch data from intent, it may just be empty!");
            return null;
         }
      }

      public static class Data implements Serializable
      {
         public final List<Column> columns;

         public Data(List<Column> columns)
         {
            this.columns = columns;
         }
      }
   }

   public static class ShowNewTask
   {
      public static final int CODE = 771;
      private static final String TASK = "task";

      public static Intent create(Activity creator)
      {
         Intent intent = new Intent(creator, NewTaskDialog.class);

         return intent;
      }

      public static Intent prepareResponse(Task task)
      {
         Intent intent = new Intent();
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Task getDataFrom(Intent intent)
      {
         try
         {
            Bundle extras = intent.getExtras();
            Task createdTask = (Task) extras.getSerializable(TASK);
            return createdTask;
         }
         catch (Exception e)
         {
            Log.e(TAG, "Unable to fetch data from intent, it may just be empty!");
            return null;
         }
      }
   }


   public static class ShowWorkspacesAndProjects
   {
      public static Intent create(Activity creator)
      {
         Intent intent = new Intent(creator, WorkspacesAndProjectsActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

         return intent;
      }
   }

   public static class ShowAssignTask
   {
      public static final int CODE = 774;
      private static final String TASK = "task";

      public static Intent createWith(Activity creator, Task task)
      {
         Intent intent = new Intent(creator, AssignTaskDialog.class);
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Intent prepareResponse(Task task)
      {
         Intent intent = new Intent();
         intent.putExtra(TASK, task);

         return intent;
      }

      public static Task getDataFrom(Intent intent)
      {
         try
         {
            Bundle extras = intent.getExtras();
            Task changedTask = (Task) extras.getSerializable(TASK);
            return changedTask;
         }
         catch (Exception e)
         {
            Log.e(TAG, "Unable to fetch data from intent, it may just be empty!");
            return null;
         }
      }
   }

   public static class RequestBoardRefresh
   {
      public static final String ACTION_NAME = "pl.project13.kanbanery.REQUEST_BOARD_REFRESH";

      public static Intent create()
      {
         Intent intent = new Intent(ACTION_NAME);

         return intent;
      }
   }

   public static class ShowGetPremiumVersionDialog
   {
      public static Intent create(Context context)
      {
         Intent intent = new Intent(context, GetPremiumVersionDialog.class);

         return intent;
      }
   }

   public static class GoToProVersionOnAndroidMarket
   {

      private static final String MARKET_DETAILS_ID = "market://details?id=";
      private static final String PRO_VERSION_MARKET_ID = "pl.project13.kanbanery.topsecretunlockerapp";

      public static Intent create(Context context)
      {
         String action = Intent.ACTION_VIEW;
         Uri uri = Uri.parse(MARKET_DETAILS_ID + PRO_VERSION_MARKET_ID);
         Intent intent = new Intent(action, uri);

         return intent;
      }
   }


   public static Task getTaskFromIntent(final Intent intent)
   {
      try
      {
         Bundle extras = intent.getExtras();
         Task returnedTask = (Task) extras.getSerializable("task");

         return returnedTask;
      }
      catch (Exception e)
      {
         Log.e(TAG, "Unable to fetch data from intent, it may just be empty!");
         return null;
      }
   }

   private static void assertValidActionType(String action, Intent intent)
   {
      if (!intent.getAction().equals(action))
      {
         throw new RuntimeException("Tries to extract data from incompatible Intent.");
      }
   }
}
