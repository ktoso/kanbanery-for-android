package pl.project13.kanbanery.util.features.activitylaunchers;

import android.app.Activity;
import android.content.Intent;
import com.google.inject.Singleton;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.ui.Intents;

import java.util.List;

/**
 * @author Konrad Malawski
 */
@Singleton
public class PremiumActivityLauncher extends FreeActivityLauncher implements ActivityLauncher
{

   @Override
   public int launchNewColumnViewForResult(Activity caller, List<Column> columns)
   {
      Intent intent = Intents.ShowNewColumn.createWith(caller, columns);
      caller.startActivityForResult(intent, Intents.ShowNewColumn.CODE);

      return Intents.ShowNewColumn.CODE;
   }

   @Override
   public int launchNewTaskViewForResult(Activity caller)
   {
      Intent intent = Intents.ShowNewTask.create(caller);
      caller.startActivityForResult(intent, Intents.ShowNewTask.CODE);

      return Intents.ShowNewTask.CODE;
   }

   @Override
   public int launchNewCommentViewForResult(Activity caller, Task task)
   {
      Intent intent = Intents.ShowNewComment.createWith(caller, task);
      caller.startActivityForResult(intent, Intents.ShowNewComment.CODE);

      return Intents.ShowNewComment.CODE;
   }

   @Override
   public int launchNewSubTaskViewForResult(Activity caller, Task task)
   {
      Intent intent = Intents.ShowNewSubTask.createWith(caller, task);
      caller.startActivityForResult(intent, Intents.ShowNewSubTask.CODE);

      return Intents.ShowNewSubTask.CODE;
   }

   @Override
   public int launchAssignTaskViewForResult(Activity caller, Task task)
   {
      Intent intent = Intents.ShowAssignTask.createWith(caller, task);
      caller.startActivityForResult(intent, Intents.ShowAssignTask.CODE);

      return Intents.ShowAssignTask.CODE;
   }

}
