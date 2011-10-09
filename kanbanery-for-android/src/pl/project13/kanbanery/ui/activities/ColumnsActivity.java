package pl.project13.kanbanery.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.deezeapps.widget.HorizontalPager;
import com.google.inject.Inject;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.ui.adapters.TaskAdapter;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import pl.project13.kanbanery.util.kanbanery.ColumnUtils;
import roboguice.activity.RoboActivity;
import roboguice.application.RoboApplication;
import roboguice.inject.InjectView;

import java.util.List;

public class ColumnsActivity extends RoboActivity implements HasTasksActivity
{

   private static final String TAG = ColumnsActivity.class.getSimpleName();

   @Inject
   CommonColumnViewStuff common;

   @Inject
   FetchAndSetUserIcon fetchAndSetUserIcon;

   @Inject
   ActivityLauncher activityLauncher;

   @Inject
   ActionExecutor actionExecutor;

   @InjectView(R.id.columns_pager)
   HorizontalPager columnsPager;

   private Handler handler;

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.columns);

      Log.i(TAG, "Starting " + getClass().getSimpleName() + "...");

      handler = new Handler();

      common.onCreate(this);

      common.initColumns();

      common.preloadUserImages();
   }

   @Override
   public Column findColumnByX(int x)
   {
      Integer taskPosition = getCurrentPage(x);
      return common.getCachedColumnByPosition(taskPosition);
   }

   private Integer getCurrentPage(int x)
   {
      return x / columnsPager.getPageWidth() + 1; // getCurrentPage is 0 based, kanbanery is 1 based
   }

   @Override
   public Integer getCurrentPage(View view)
   {
      return columnsPager.getCurrentPage() + 1;  // getCurrentPage is 0 based, kanbanery is 1 based
   }

   public HasTasksActivity getHasTasksActivity()
   {
      return ColumnsActivity.this;
   }

   private void initServices()
   {
      Log.i(TAG, "Starting services on Activity create...");
   }

   @Override
   public void clearColumns()
   {
      try
      {
         common.columnTasksMap.clear();

         columnsPager.removeAllViews();
      }
      catch (NullPointerException ignore)
      {
         // may happen, ignore
      }
   }

   @Override
   public void refreshColumnData(Column column)
   {
      common.initColumns(); // todo improve this
   }

   @Override
   public void setTitle(String title)
   {
      super.setTitle(title);

   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      return common.onOptionsItemSelected(item);
   }

   @Override
   public Activity getActivity()
   {
      return this;
   }

   @Override
   public void onMoveTaskClicked(View view, Task task)
   {
      common.onMoveTaskClicked(view, task);
   }

   @Override
   public void onDeleteTaskClicked(View view, Task task)
   {
      common.onDeleteTaskClicked(view, task);
   }

   @Override
   public void onTaskTypeClicked(View view, Task task)
   {
      common.onTaskTypeClicked(view, task);
   }

   @Override
   public void onEstimateClicked(View view, Task task)
   {
      common.onEstimateClicked(view, task);
   }

   @Override
   public void onOwnerImageClicked(View view, Task task)
   {
      common.onOwnerImageClicked(view, task);
   }

   @Override
   public void onTaskDrop(Task task, float x, float y)
   {
      // swallow
   }

   @Override
   public void onAddCommentClicked(View view, Task task)
   {
      common.onAddCommentClicked(view, task);
   }

   @Override
   public void onAddSubTask(View view, Task task)
   {
      common.onAddSubTask(view, task);
   }

   @Override
   public Column getCachedColumnByPosition(Integer columnPosition)
   {
      return common.getCachedColumnByPosition(columnPosition);
   }

   /**
    * Should not be called in the UI thread.
    *
    * @param column
    */
   @Override
   public void initColumn(final Column column, final List<Task> tasks)
   {
      common.columnTasksMap.put(column.getPosition(), tasks);

      runOnUiThread(new Runnable()
      {
         @Override
         public void run()
         {
            ListView columnView = (ListView) View.inflate(ColumnsActivity.this, R.layout.column, null);
            columnView.setTag(column.getPosition());

            LinearLayout columnTitle = (LinearLayout) View.inflate(ColumnsActivity.this, R.layout.column_header, null);
            TextView columnTitleText = (TextView) columnTitle.findViewById(R.id.column_title);
            ColumnUtils.columnCapacityText(columnTitleText, column, tasks);

            columnView.addHeaderView(columnTitle, new Object(), false);

            TaskAdapter adapter = new TaskAdapter(((RoboApplication) getApplication())
                                                        .getInjector(), ColumnsActivity.this, R.layout.task_on_board, fetchAndSetUserIcon, tasks);
            columnView.setAdapter(adapter);
            columnView.clearChoices();

            View viewForThisColumn = columnsPager.findViewWithTag(column.getPosition());
            if (viewForThisColumn == null)
            {
               columnsPager.addView(columnView);
            }
            else
            {
               int index = columnsPager.indexOfChild(viewForThisColumn);
               columnsPager.removeView(viewForThisColumn);
               columnsPager.addView(columnView, index);
            }
         }
      });
   }

   /**
    * Move between columns
    */
   @Override
   public boolean onKeyUp(int keyCode, KeyEvent event)
   {
      switch (event.getKeyCode())
      {
         case KeyEvent.KEYCODE_DPAD_RIGHT:
            columnsPager.scrollRight();
            break;
         case KeyEvent.KEYCODE_DPAD_LEFT:
            columnsPager.scrollLeft();
            break;
      }

      return false;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      return common.onCreateOptionsMenu(menu);
   }

   @Override
   public void showToast(String message, int lengthLong)
   {
      Toast.makeText(this, message, lengthLong).show();
   }

   @Override
   public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
   {
      common.onCreateContextMenu(menu, v, menuInfo);
   }

   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      common.onActivityResult(requestCode, resultCode, data);
   }

}