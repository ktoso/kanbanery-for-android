package pl.project13.kanbanery.ui.activities;

import android.content.Intent;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;

public interface CommonTaskActions
{

   boolean onOptionsItemSelected(MenuItem item);

   void onDeleteTaskClicked(View source, Task task);

   void onMoveTaskClicked(View source, Task task);

   void onEstimateClicked(View source, Task task);

   void onTaskTypeClicked(View source, Task task);

   void onOwnerImageClicked(View source, Task task);

   void onTaskDrop(Task task, float x, float y);

   void onAddCommentClicked(View source, Task task);

   void onAddSubTask(View source, Task task);

   Column getCachedColumnByPosition(Integer columnPosition);

   void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);
   boolean onContextItemSelected(final MenuItem item);

   void onActivityResult(int requestCode, int resultCode, Intent data);
}
