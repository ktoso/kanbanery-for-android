package pl.project13.kanbanery.util.features.activitylaunchers;

import android.app.Activity;
import android.content.Intent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.ui.activities.BoardActivity;
import pl.project13.kanbanery.ui.activities.ColumnsActivity;
import pl.project13.kanbanery.ui.activities.SettingsActivity;
import pl.project13.kanbanery.ui.activities.SignInActivity;
import pl.project13.kanbanery.ui.activities.WorkspacesAndProjectsActivity;
import pl.project13.kanbanery.ui.activities.dialog.GetPremiumVersionDialog;

import java.util.List;

/**
 * @author Konrad Malawski
 */
@Singleton
public class FreeActivityLauncher implements ActivityLauncher
{

   @Inject
   @Named("onTablet")
   protected boolean onTablet;

   @Override
   public void launchTasksView(Activity caller)
   {
      if (onTablet)
      {
         launchBoardView(caller);
      }
      else
      {
         launchColumnsView(caller);
      }
   }

   @Override
   public void launchBoardView(Activity caller)
   {
      Intent intent = new Intent(caller, BoardActivity.class);
      caller.startActivity(intent);
   }

   @Override
   public void launchColumnsView(Activity caller)
   {
      Intent intent = new Intent(caller, ColumnsActivity.class);
      caller.startActivity(intent);
   }

   @Override
   public void launchSettingsView(Activity caller)
   {
      Intent intent = new Intent(caller, SettingsActivity.class);
      caller.startActivity(intent);
   }

   @Override
   public void launchSignInView(Activity caller)
   {
      Intent intent = new Intent(caller, SignInActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      caller.startActivity(intent);
   }

   @Override
   public void launchWorkspacesAndProjectsView(Activity caller)
   {
      Intent intent = new Intent(caller, WorkspacesAndProjectsActivity.class);
      caller.startActivity(intent);
   }

   @Override
   public int launchAssignTaskViewForResult(Activity caller, Task task)
   {
      return launchGetPremiumVersionView(caller);
   }

   @Override
   public int launchNewTaskViewForResult(Activity caller)
   {
      return launchGetPremiumVersionView(caller);
   }

   @Override
   public int launchNewColumnViewForResult(Activity caller, List<Column> columns)
   {
      return launchGetPremiumVersionView(caller);
   }

   @Override
   public int launchNewCommentViewForResult(Activity caller, Task task)
   {
      return launchGetPremiumVersionView(caller);
   }

   @Override
   public int launchNewSubTaskViewForResult(Activity caller, Task task)
   {
      return launchGetPremiumVersionView(caller);
   }

   private int launchGetPremiumVersionView(Activity caller)
   {
      Intent intent = new Intent(caller, GetPremiumVersionDialog.class);
      caller.startActivity(intent);

      return 0;
   }
}
