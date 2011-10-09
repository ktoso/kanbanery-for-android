package pl.project13.kanbanery.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.core.flow.TaskMoveFlow;
import pl.project13.janbanery.exceptions.WorkspaceNotFoundException;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Estimate;
import pl.project13.janbanery.resources.Project;
import pl.project13.janbanery.resources.Task;
import pl.project13.janbanery.resources.TaskType;
import pl.project13.janbanery.resources.User;
import pl.project13.janbanery.resources.Workspace;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.AssuresUiThread;
import pl.project13.kanbanery.annotation.RequiresMagicVariable;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.ColumnByIdPredicate;
import pl.project13.kanbanery.util.ColumnByPositionPredicate;
import pl.project13.kanbanery.util.MapQueries;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import pl.project13.kanbanery.util.features.runnables.ContextItemSelectedRunnable;
import pl.project13.kanbanery.util.http.CachingDrawableFetcher;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;
import static pl.project13.janbanery.util.JanbaneryAndroidUtils.toAndroidColor;

/**
 * @author Konrad Malawski
 */
public class CommonColumnViewStuff implements CommonTaskActions
{

   private static final String TAG = CommonColumnViewStuff.class.getSimpleName();

   @Inject
   SharedPreferences preferences;

   @Inject
   Janbanery janbanery;

   @Inject
   CachingDrawableFetcher cachingDrawableFetcher;

   @Inject
   ActivityLauncher activityLauncher;

   @Inject
   ActionExecutor actionExecutor;

   /**
    * Assigned when a new context menu is created
    */
   Task mContextMenuSelectedTask;

   /**
    * Very hacky, beware. This will be set if an method overriden from
    * which initiated the action. Use it to change the owner's icon etc.
    */
   View mContextMenuShowSourceView;

   /**
    * column position -> list of tasks in it
    */
   Map<Integer, List<Task>> columnTasksMap = newHashMap(); // todo wrap this as a type, ColumnTasksByItsPosition

   List<Column> columnsList;

   HasTasksActivity activity;

   private Handler handler = new Handler();

   private void setContext(HasTasksActivity activity)
   {
      this.activity = activity;
   }

   public void onCreate(HasTasksActivity activity)
   {
      setContext(activity);

      Workspace workspace;
      Project project;

      try
      {
         workspace = janbanery.workspaces().current();
         project = janbanery.projects().current();
      }
      catch (WorkspaceNotFoundException e)
      {
         Log.e(TAG, "No workspace found! Displaying workspace/project selection activity...");

         activityLauncher.launchWorkspacesAndProjectsView(getActivity());
         getActivity().finish();
         return;
      }

      String title = getActivity().getString(R.string.kanbanery) + " - " + workspace.getName() + " - " + project
            .getName();
      activity.setTitle(title);

      persistCurrentProject(workspace, project);
   }

   private HasTasksActivity getHasTasksActivity()
   {
      return activity;
   }

   protected Activity getActivity()
   {
      return (Activity) activity;
   }

   private void persistCurrentProject(Workspace workspace, Project project)
   {
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString(getActivity().getString(R.string.preference_key_current_project), project.getName());
      editor.putString(getActivity().getString(R.string.preference_key_current_workspace), workspace.getName());
      editor.commit();
   }

   public boolean onCreateOptionsMenu(Menu menu)
   {
      MenuInflater inflater = getActivity().getMenuInflater();

      inflater.inflate(R.menu.columns_menu, menu);
      return true;
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      switch (requestCode)
      {
         case Intents.ShowNewTask.CODE:
         {
            Task task = Intents.ShowNewTask.getDataFrom(data);
            if (task == null)
            {
               break;
            }

            onNewTaskResult(task);
            break;
         }
         case Intents.ShowNewComment.CODE:
         {
            Task task = Intents.ShowNewComment.getDataFrom(data);
            if (task == null)
            {
               break;
            }

            onNewCommentResult(task);
            break;
         }
         case Intents.ShowAssignTask.CODE:
         {
            Task task = Intents.ShowAssignTask.getDataFrom(data);
            if (task == null)
            {
               break;
            }

            onReassignedTaskResult(task);
            break;
         }
         case Intents.ShowNewColumn.CODE:
         {
            Column column = Intents.ShowNewColumn.getDataFromResponse(data);
            if (column == null)
            {
               break;
            }

            initColumns();
         }
         case Intents.ShowNewSubTask.CODE:
         {
            Task task = Intents.ShowNewSubTask.getDataFrom(data);
            if (task == null)
            {
               break;
            }

            onNewSubTaskResult(task);
            break;
         }
         default:
         {
            Log.w(TAG, "Got result from activity, but ignored it...");
         }
      }
   }

   protected void onReassignedTaskResult(Task reassignedTask)
   {
      refreshColumnForUpdatedTask(reassignedTask);
   }

   protected void onNewSubTaskResult(Task taskWithNewSubTask)
   {
      refreshColumnForUpdatedTask(taskWithNewSubTask);
   }

   private void onNewCommentResult(Task taskWithNewComment)
   {
      refreshColumnForUpdatedTask(taskWithNewComment);
   }

   private void refreshColumnForUpdatedTask(Task changedTask)
   {
      MapQueries.find(changedTask)
                .in(columnTasksMap)
                .andReplaceWith(changedTask);

      Long columnId = changedTask.getColumnId();
      Column column = Iterables.find(columnsList, new ColumnByIdPredicate(columnId));

      getHasTasksActivity().refreshColumnData(column);
   }

   private void onNewTaskResult(Task createdTask)
   {
      Column firstColumn = getHasTasksActivity().findColumnByX(10);
      List<Task> firstColumnTasks = columnTasksMap.get(1); // todo or 0?
      firstColumnTasks.add(createdTask);

      getHasTasksActivity().initColumn(firstColumn, firstColumnTasks);
   }

   /**
    * Resets the used APIKey and navigates back to the sign in screen.
    */
   private void signOut()
   {
      // reset api key
      SharedPreferences.Editor editor = preferences.edit();
      editor.remove(getActivity().getString(R.string.preference_key_api_key));
      editor.remove(getActivity().getString(R.string.preference_key_current_workspace));
      editor.remove(getActivity().getString(R.string.preference_key_current_project));
      editor.commit();

      // go back to sign in screen
      activityLauncher.launchSignInView(getActivity());
      getActivity().finish();
   }

   private void openNewTaskScreen()
   {
      activityLauncher.launchNewTaskViewForResult(getActivity());
   }

   private void openNewColumnScreen()
   {
      List<Column> columns = columnsList;

      if (columns == null || columns.isEmpty())
      {
         columns = fetchAllColumns();
      }

      activityLauncher.launchNewColumnViewForResult(getActivity(), columns);
   }

   @Override
   public void onOwnerImageClicked(View source, Task task)
   {
      activityLauncher.launchAssignTaskViewForResult(getActivity(), task);
   }

   @Override
   public void onTaskTypeClicked(View source, Task task)
   {
      openContextMenuForTaskChangeRequest(source, task);
   }

   @Override
   public void onEstimateClicked(View source, Task task)
   {
      openContextMenuForTaskChangeRequest(source, task);
   }

   @Override
   public void onMoveTaskClicked(View source, Task task)
   {
      openContextMenuForMoveTaskRequest(source, task);
   }

   @Override
   public void onDeleteTaskClicked(View source, final Task task)
   {
      new AlertDialog.Builder(getActivity())
            .setTitle(R.string.delete_task_title)
            .setMessage(getActivity().getString(R.string.delete_task_message, task.getTitle()))
            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialogInterface, int i)
               {
                  actionExecutor.onlyForPremium(getActivity(), new Runnable()
                  {
                     @Override
                     public void run()
                     {
                        janbanery.tasks().delete(task);
                     }
                  });

                  deleteTaskInUi(task);
               }
            })
            .setCancelable(true)
            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
            {
               @Override
               public void onClick(DialogInterface dialogInterface, int i)
               {
                  dialogInterface.cancel();
               }
            })
            .show();
   }

   @Override
   public void onTaskDrop(final Task task, float x, float y)
   {

      final Column oldColumn = getCachedColumnById(task.getColumnId());
      final Column targetColumn = getHasTasksActivity().findColumnByX((int) x);

      if (targetColumn.equals(oldColumn))
      {
         return;
      }

      // todo infer position by getTaskHeight() / ....
      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            Task movedTask = janbanery.tasks()
                                      .update(task).position(1)
                                      .move().to(targetColumn).get();

            moveTaskInUi(movedTask, oldColumn, targetColumn);

         }
      });

   }

   @Override
   public void onAddCommentClicked(View source, Task task)
   {
      activityLauncher.launchNewCommentViewForResult(getActivity(), task);
   }

   @Override
   public void onAddSubTask(View source, Task task)
   {
      activityLauncher.launchNewSubTaskViewForResult(getActivity(), task);
   }

   private void deleteTaskInUi(final Task taskToBeRemoved)
   {
      MapQueries.find(taskToBeRemoved)
                .in(columnTasksMap)
                .andRemoveIt();

      Long columnId = taskToBeRemoved.getColumnId();
      Column column = Iterables.find(columnsList, new ColumnByIdPredicate(columnId));

      getHasTasksActivity().refreshColumnData(column);
   }

   @Override
   public Column getCachedColumnByPosition(Integer columnPosition)
   {
      return Iterables.find(columnsList, new ColumnByPositionPredicate(columnPosition));
   }

   private void openContextMenuForMoveTaskRequest(View source, Task task)
   {
      Log.i(TAG, "Move task: " + task.getTitle());
      mContextMenuSelectedTask = task;
      mContextMenuShowSourceView = source;

      getActivity().registerForContextMenu(source);
      getActivity().openContextMenu(source);
   }

   private void openContextMenuForTaskChangeRequest(View source, Task task)
   {
      Log.i(TAG, "Estimate on: " + source.getId() + " was clicked.");
      mContextMenuSelectedTask = task;
      mContextMenuShowSourceView = source;

      getActivity().registerForContextMenu(source);
      getActivity().openContextMenu(source);
   }

   private Task getSelectedTask(View clickedView, ContextMenu.ContextMenuInfo contextMenuInfo)
   {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) contextMenuInfo;

      // get the information about which task was clicked, based on which element on which page it is
      int taskPositionInListView = info.position;

      Integer currentPage = getHasTasksActivity().getCurrentPage(clickedView);
      if (currentPage == null)
      {
         currentPage = getHasTasksActivity().getCurrentPage(clickedView);
      }

      final List<Task> tasksInCurrentColumn = columnTasksMap.get(currentPage);
      Task task = tasksInCurrentColumn.get(taskPositionInListView - 1);

      Log.d(TAG, "Selected task for is: id = " + task.getId() + ", title: " + task.getTitle());

      return task;
   }

   private void deleteTask(final Task task)
   {
      Log.i(TAG, String.format("Deleting task %d...", task.getId()));

      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            janbanery.tasks().delete(task);
         }
      });

      getHasTasksActivity().showToast("Finished deleting task '" + task.getTitle() + "'", Toast.LENGTH_SHORT);
      initColumns();
   }

   private void markTaskAsNotReadyToPull(final Task task)
   {
      Log.i(TAG, String.format("Marking task %d as not ready to pull...", task.getId()));

      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            janbanery.tasks().mark(task).asNotReadyToPull();
         }
      });

      getHasTasksActivity().showToast("Finished marking task '" + task.getTitle() + "'", Toast.LENGTH_SHORT);
      initColumns();
   }

   private void markTaskAsReadyToPull(final Task task)
   {
      Log.i(TAG, String.format("Marking task %d as ready to pull...", task.getId()));

      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            janbanery.tasks().mark(task).asReadyToPull();
         }
      });

      getHasTasksActivity().showToast("Finished marking task '" + task.getTitle() + "'", Toast.LENGTH_SHORT);
      initColumns();
   }

   private void moveTaskToPreviousColumn(final Task task)
   {
      Log.i(TAG, String.format("Moving task %d to previous column...", task.getId()));

      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            janbanery.tasks().move(task).toPreviousColumn();
         }
      });

      getHasTasksActivity().showToast("Finished moving task '" + task.getTitle() + "'", Toast.LENGTH_SHORT);
      initColumns();
   }

   private void moveTaskToNextColumn(final Task task)
   {
      Log.i(TAG, String.format("Moving task %d to next column...", task.getId()));

      actionExecutor.onlyForPremium(getActivity(), new Runnable()
      {
         @Override
         public void run()
         {
            janbanery.tasks().move(task).toNextColumn();
         }
      });

      getHasTasksActivity().showToast("Finished moving task '" + task.getTitle() + "'", Toast.LENGTH_SHORT);
      initColumns();
   }

   /**
    * Called on menu clicks etc, also the action bar clicks fall into here
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.new_task_menu_btn:
            openNewTaskScreen();
            return true;
         case R.id.new_column_menu_btn:
            openNewColumnScreen();
            return true;
         case R.id.columns_refresh:
            initColumns();
            return true;
         case R.id.switch_project:
            activityLauncher.launchWorkspacesAndProjectsView(getActivity());
            return true;
         case R.id.columns_sign_out:
            signOut();
            return true;
         case R.id.open_settings:
            activityLauncher.launchSettingsView(getActivity());
            return true;
         default:
            return false;
      }
   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

      switch (v.getId())
      {
         case R.id.task_type_btn:
         {
            showChangeTaskTypeDialog(menu);
            break;
         }
         case R.id.move_btn:
         {
            showMoveTaskDialog(menu);
            break;
         }
         case R.id.estimate_btn:
         {
            showChangeEstimateDialog(menu);
            break;
         }
         default:
         {
            mContextMenuSelectedTask = getSelectedTask(v, menuInfo);
            showDefaultTaskContextMenu(menu);
            break;
         }
      }
   }

   @RequiresMagicVariable("mContextMenuSelectedTask")
   private void showMoveTaskDialog(ContextMenu menu)
   {
      menu.setHeaderTitle(getActivity().getString(R.string.move_menu_item));

      Long columnId = mContextMenuSelectedTask.getColumnId();
      Column oldColumn = getCachedColumnById(columnId);

      final TaskMoveFlow moveTask = janbanery.tasks().move(mContextMenuSelectedTask);

      if (isNotLastColumn(oldColumn))
      {
         MenuItem nextColumnMenuItem = menu.add(getActivity().getString(R.string.next_column));
         nextColumnMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
         {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
               actionExecutor.onlyForPremium(getActivity(), new Runnable()
               {
                  @Override
                  public void run()
                  {
                     Task movedTask = moveTask.toNextColumn().get();

                     Column oldColumn = getCachedColumnById(mContextMenuSelectedTask.getColumnId());
                     Column newColumn = getCachedColumnById(movedTask.getColumnId());
                     moveTaskInUi(movedTask, oldColumn, newColumn);
                  }
               });

               return true;
            }
         });
      }

      if (isNotFirstColumn(oldColumn))
      {
         MenuItem prevColumnMenuItem = menu.add(getActivity().getString(R.string.previous_column));
         prevColumnMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
         {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
               final Column oldColumn = getCachedColumnById(mContextMenuSelectedTask.getColumnId());
               final Column newColumn = columnsList.get(columnsList.indexOf(oldColumn) - 1);

               actionExecutor.onlyForPremium(getActivity(), new Runnable()
               {
                  @Override
                  public void run()
                  {
                     // todo toPreviousColumn seems to magically not work on last column
                     Task movedTask = moveTask.to(newColumn).get();

                     moveTaskInUi(movedTask, oldColumn, newColumn);
                  }
               });

               return true;
            }
         });
      }

      for (final Column targetColumn : columnsList)
      {
         MenuItem menuItem = menu.add(targetColumn.getName());

         menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
         {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
               Task movedTask = moveTask.to(targetColumn).get();

               Column oldColumn = getCachedColumnById(mContextMenuSelectedTask.getColumnId());
               Column newColumn = getCachedColumnById(targetColumn.getId());

               moveTaskInUi(movedTask, oldColumn, newColumn);

               return true;
            }
         });
      }
   }

   @Override
   public boolean onContextItemSelected(final MenuItem item)
   {
      actionExecutor.onlyForPremium(getActivity(),
                                    new ContextItemSelectedRunnable(getActivity(), item)
                                    {

                                       @Override
                                       public void run()
                                       {
                                          if (mContextMenuSelectedTask == null)
                                          {
                                             getHasTasksActivity().showToast("No task was selected, can not perform operation.", Toast.LENGTH_LONG);
                                          }

                                          switch (item.getItemId())
                                          {
                                             case R.id.mark_as_done_menu_item:
                                                markTaskAsReadyToPull(mContextMenuSelectedTask);
                                                break;
                                             case R.id.mark_as_not_done_menu_item:
                                                markTaskAsNotReadyToPull(mContextMenuSelectedTask);
                                                break;

                                             case R.id.move_to_next_column_menu_item:
                                                moveTaskToNextColumn(mContextMenuSelectedTask);
                                                break;
                                             case R.id.move_to_previous_column_menu_item:
                                                moveTaskToPreviousColumn(mContextMenuSelectedTask);
                                                break;

                                             case R.id.delete_menu_item:
                                                deleteTask(mContextMenuSelectedTask);
                                                break;

                                             default:
                                                Log.d(TAG, "Got unexpected menu item selection code");
                                                break;
                                          }
                                       }
                                    });

      return true;
   }

   @AssuresUiThread
   @VisibleForTesting
   void moveTaskInUi(final Task task, final Column oldColumn, final Column newColumn)
   {
      // todo has bugs...
      getActivity().runOnUiThread(new Runnable()
      {
         @Override
         public void run()
         {
            List<Task> tasksInOldColumn = columnTasksMap.get(oldColumn.getPosition());
            tasksInOldColumn.remove(task);
            getHasTasksActivity().initColumn(oldColumn, tasksInOldColumn);

            List<Task> tasksInTargetColumn = columnTasksMap.get(newColumn.getPosition());
            tasksInTargetColumn.add(task);
            getHasTasksActivity().initColumn(newColumn, tasksInTargetColumn);
         }
      });
   }

   private boolean isNotFirstColumn(Column oldColumn)
   {
      return oldColumn.getPosition() != 1;
   }

   private boolean isNotLastColumn(Column oldColumn)
   {
      return !oldColumn.getPosition().equals(lastColumnInUi().getPosition());
   }

   private Column lastColumnInUi()
   {
      if (columnsList.isEmpty())
      {
         throw new RuntimeException("Unable to return last column, the columnsList is empty!");
      }
      else
      {
         return columnsList.get(columnsList.size() - 1);
      }
   }

   private Column getCachedColumnById(Long columnId)
   {
      return Iterables.find(columnsList, new ColumnByIdPredicate(columnId));
   }

   private void showDefaultTaskContextMenu(ContextMenu menu)
   {
      MenuInflater inflater = getActivity().getMenuInflater();
      inflater.inflate(R.menu.task_context_menu, menu);
   }

   private void showChangeEstimateDialog(ContextMenu menu)
   {
      menu.setHeaderTitle(getActivity().getString(R.string.change_task_estimate));

      final List<Estimate> estimates = janbanery.estimates().all();

      for (int i = 0; i < estimates.size(); i++)
      {
         MenuItem menuItem = menu.add(Menu.NONE, i, i, estimates.get(i).getLabel());

         final int userPos = i;
         menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
         {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
//               Task task = janbanery.tasks().byId(mContextMenuSelectedTask.getId()); // why the fuck?
               Task task = mContextMenuSelectedTask;
               final Estimate estimate = estimates.get(userPos);

               janbanery.tasks()
                        .update(task)
                        .estimate(estimate);

               if (mContextMenuShowSourceView != null && mContextMenuShowSourceView instanceof Button)
               {
                  final Button estimateBtn = (Button) mContextMenuShowSourceView;
                  handler.postDelayed(new Runnable()
                  {
                     @Override
                     public void run()
                     {
                        estimateBtn.setText(estimate.getLabel());
                     }
                  }, 100);
               }

               return true;
            }
         });
      }

   }

   private void showChangeTaskTypeDialog(ContextMenu menu)
   {
      menu.setHeaderTitle(getActivity().getString(R.string.change_task_type));

      final List<TaskType> taskTypes = janbanery.taskTypes().all();

      for (int i = 0; i < taskTypes.size(); i++)
      {
         MenuItem menuItem = menu.add(Menu.NONE, i, i, taskTypes.get(i).getName());

         final int userPos = i;
         menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
         {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem)
            {
               Task task = janbanery.tasks().byId(mContextMenuSelectedTask.getId());
               final TaskType newTaskType = taskTypes.get(userPos);

               janbanery.tasks()
                        .update(task)
                        .taskType(newTaskType);

               if (mContextMenuShowSourceView != null && mContextMenuShowSourceView instanceof Button)
               {
                  final Button taskTypeBtn = (Button) mContextMenuShowSourceView;
                  handler.postDelayed(new Runnable()
                  {
                     @Override
                     public void run()
                     {
                        taskTypeBtn.setText(newTaskType.getName());
                        taskTypeBtn.setBackgroundColor(toAndroidColor(newTaskType.getBackgroundColor()));
                        taskTypeBtn.setTextColor(toAndroidColor(newTaskType.getTextColor()));
                     }
                  }, 100);
               }

               return true;
            }
         });
      }
   }

   private List<Task> fetchTasks(final Column column)
   {
      List<Task> jTasks;

      jTasks = janbanery.tasks().allIn(column);

      if (jTasks == null)
      {
         jTasks = Collections.emptyList();
      }

      Log.i(TAG, "Fetched " + jTasks.size() + " tasks in " + column.getPosition() + " column...");

      return jTasks;
   }

   @AssuresUiThread
   protected void initColumns()
   {
      //noinspection unchecked
      new AsyncTask<Void, Column, Void>()
      {

         private ProgressDialog pleaseWaitDialog;

         @Override
         protected void onPreExecute()
         {
            pleaseWaitDialog = new ProgressDialog(getActivity());
            pleaseWaitDialog.setIndeterminate(false);
            pleaseWaitDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            pleaseWaitDialog.setTitle("Refreshing board...");
            pleaseWaitDialog.setMessage("Loading columns...");
            pleaseWaitDialog.show();

            getHasTasksActivity().clearColumns();
         }

         @Override
         protected Void doInBackground(Void... voids)
         {
            final List<Column> columns = fetchAllColumns();
            Log.i(TAG, "Fetched " + columns.size() + " columns...");

            handler.post(new Runnable()
            {
               @Override
               public void run()
               {
                  pleaseWaitDialog.setMax(columns.size());
               }
            });

            for (Column column : columns)
            {
               Log.i(TAG, "Init column: " + column.getName());

               List<Task> tasks = fetchTasks(column);
               getHasTasksActivity().initColumn(column, tasks);

               publishProgress(column);
            }

            return null;
         }

         @Override
         protected void onProgressUpdate(Column... values)
         {
            pleaseWaitDialog.setMessage(String.format("Loading %s column...", values[0].getName()));

            int progress = pleaseWaitDialog.getProgress() + 1;
            pleaseWaitDialog.setProgress(progress);
         }

         @Override
         protected void onPostExecute(Void aVoid)
         {
            pleaseWaitDialog.dismiss();
         }
      }.execute();
   }

   private List<Column> fetchAllColumns()
   {
      Log.i(TAG, "Fetching all columns...");

      List<Column> columns = Collections.emptyList();

      try
      {
         columns = janbanery.columns().all();
      }
      catch (Exception e)
      {
         getHasTasksActivity().showToast("Unable to fetch columns...", Toast.LENGTH_LONG);
      }

      Log.i(TAG, "Fetched " + columns.size() + " columns...");

      // cache them
      this.columnsList = columns;

      return columns;
   }

   public void preloadUserImages()
   {
      Log.i(TAG, "Preloading user images...");

      //noinspection unchecked
      new AsyncTask<Void, String, Void>()
      {

         @Override
         protected Void doInBackground(Void... voids)
         {
            try
            {
               List<User> all = janbanery.users().all();
               for (User user : all)
               {
                  String gravatarUrl = user.getGravatarUrl();

                  Log.i(TAG, "Loading image for: " + user.getEmail());
                  cachingDrawableFetcher.fetchDrawable(gravatarUrl);
               }
            }
            catch (Exception e)
            {
               Log.e(TAG, "Pre fetching users failed", e);
            }

            return null;
         }
      }.execute();
   }
}
