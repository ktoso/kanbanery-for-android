package pl.project13.kanbanery.util.features.activitylaunchers;

import android.app.Activity;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;

import java.util.List;

/**
 * @author Konrad Malawski
 */
public interface ActivityLauncher
{

   void launchTasksView(Activity caller);

   void launchColumnsView(Activity caller);

   void launchSettingsView(Activity caller);

   void launchSignInView(Activity caller);

   int launchNewColumnViewForResult(Activity activity, List<Column> columns);

   void launchWorkspacesAndProjectsView(Activity caller);

   int launchAssignTaskViewForResult(Activity caller, Task task);

   int launchNewTaskViewForResult(Activity caller);

   int launchNewCommentViewForResult(Activity caller, Task task);

   int launchNewSubTaskViewForResult(Activity caller, Task task);

   void launchBoardView(Activity caller);
}
