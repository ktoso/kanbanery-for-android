package pl.project13.kanbanery.ui.activities;

import android.app.Activity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;

import java.util.List;

public interface HasTasksActivity extends CommonTaskActions
{
   Column findColumnByX(int x);

   Integer getCurrentPage(View clickedView);

   void initColumn(Column column, List<Task> tasks);

   void clearColumns();

   void refreshColumnData(Column column);

   void setTitle(String title);

   Activity getActivity();

   void onMoveTaskClicked(View view, Task task);

   void onDeleteTaskClicked(View view, Task task);

   void onTaskTypeClicked(View view, Task task);

   void onEstimateClicked(View view, Task task);

   void onOwnerImageClicked(View view, Task task);

   void onAddCommentClicked(View view, Task task);

   void onAddSubTask(View view, Task task);

   boolean onContextItemSelected(MenuItem item);

   void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

   boolean onCreateOptionsMenu(Menu menu);

   void showToast(String message, int lengthLong);
}
