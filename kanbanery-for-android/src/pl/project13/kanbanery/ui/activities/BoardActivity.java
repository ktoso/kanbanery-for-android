package pl.project13.kanbanery.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.inject.Inject;
import com.google.inject.internal.Nullable;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.ui.adapters.TaskAdapter;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import pl.project13.kanbanery.util.kanbanery.ColumnUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * @author Konrad Malawski
 */
public class BoardActivity extends RoboActivity implements HasTasksActivity
{

   private static final String TAG = BoardActivity.class.getSimpleName();

   @Inject
   CommonColumnViewStuff common;

   @Inject
   ActivityLauncher activityLauncher;

   @Inject
   SharedPreferences preferences;

   @Inject
   FetchAndSetUserIcon fetchAndSetUserIcon;

   @Nullable
   @InjectView(R.id.board_scroller)
   HorizontalScrollView boardScroller;

   @Nullable
   @InjectView(R.id.columns_container)
   LinearLayout columnsContainer;

   @Inject
   WindowManager windowManager;

   private Handler handler;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      handler = new Handler();

      common.onCreate(this);

      Log.i(TAG, "Loading BoardActivity...");

//      if (shouldShowTutorial())
//      {
//         common.startupProgressDialog.dismiss();
//
//         setContentView(R.layout.board_with_tutorial_overlay);
//         ImageView tutorialOverlay = (ImageView) findViewById(R.id.tutorial_overlay);
//         tutorialOverlay.setOnClickListener(new HideTutorialOverlayOnClick());
//      }
//      else
//      {
         setContentView(R.layout.board);

         common.initColumns();

      common.preloadUserImages();
//      }
   }

   private void markTutorialAsDisplayed()
   {
      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean(getString(R.string.preference_key_show_tutorial), false);
      editor.commit();
   }

   private boolean shouldShowTutorial()
   {
      String key = getString(R.string.preference_key_show_tutorial);

      boolean wasNeverShown = !preferences.contains(key);
      boolean requestedToShow = preferences.getBoolean(key, true);

      return wasNeverShown || requestedToShow;
   }

   protected boolean isInDualPaneMode()
   {
      return true;
   }

   @Override
   public Column findColumnByX(int x)
   {
      Integer taskPosition = getCurrentPage(x);
      return common.getCachedColumnByPosition(taskPosition);
   }

   @Override
   public Integer getCurrentPage(View view)
   {
      int left = view.getLeft();
      return getCurrentPage(left);
   }

   private Integer getCurrentPage(int left)
   {
      return left / getColumnWidth() + 1; // as kanbanery column IDs are 1-based
   }

   @Override
   public void clearColumns()
   {
      try
      {
         common.columnTasksMap.clear();

         columnsContainer.removeAllViews();
      }
      catch (NullPointerException ignore)
      {
         // may happen, ignore
      }
   }

   @Override
   public void refreshColumnData(final Column column)
   {
      ListView viewForThisColumn = getViewForColumn(column);
      ListAdapter adapter = viewForThisColumn.getAdapter();

      if (ArrayAdapter.class.isAssignableFrom(adapter.getClass()))
      {
         ArrayAdapter arrayAdapter = (ArrayAdapter) adapter;
         arrayAdapter.notifyDataSetChanged();
      }
      else if (HeaderViewListAdapter.class.isAssignableFrom(adapter.getClass()))
      {
         // todo oh man... why do I have to do this in the first place...?
         HeaderViewListAdapter headerViewListAdapter = (HeaderViewListAdapter) adapter;
         ListAdapter wrappedAdapter = headerViewListAdapter.getWrappedAdapter();

         if (ArrayAdapter.class.isAssignableFrom(wrappedAdapter.getClass()))
         {
            ArrayAdapter arrayAdapter = (ArrayAdapter) wrappedAdapter;

            arrayAdapter.notifyDataSetInvalidated();
         }
         else
         {
            String wrappedAdapterCanonicalName = wrappedAdapter.getClass().getCanonicalName();
            Log.e(TAG, "Unable to handle adapter with known methods... It's a HeaderViewListAdapter, but wraps: " + wrappedAdapterCanonicalName);
         }
      }
      else
      {
         String adapterCanonicalName = adapter.getClass().getCanonicalName();
         Log.e(TAG, "Unable to treat the found adapter with any known method... It's a [" + adapterCanonicalName + "]");
      }

   }

   @Override
   public void setTitle(String title)
   {
      super.setTitle(title);
   }

   @Override
   public Activity getActivity()
   {
      return BoardActivity.this;
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
      common.onTaskDrop(task, x, y);
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

   private ListView getViewForColumn(Column column)
   {
      View viewWithTag = columnsContainer.findViewWithTag(column.getPosition());

      return ListView.class.cast(viewWithTag);
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
            ListView columnView = (ListView) View.inflate(BoardActivity.this, R.layout.column, null);
            columnView.setTag(column.getPosition());
            columnView.setLayoutParams(new ViewGroup.LayoutParams(getColumnWidth(), ViewGroup.LayoutParams.FILL_PARENT));

            LinearLayout columnTitle = (LinearLayout) View.inflate(BoardActivity.this, R.layout.column_header, null);
            TextView columnTitleText = (TextView) columnTitle.findViewById(R.id.column_title);
            ColumnUtils.columnCapacityText(columnTitleText, column, tasks);

            columnView.addHeaderView(columnTitle, new Object(), false);

            TaskAdapter adapter = new TaskAdapter(getInjector(), BoardActivity.this, R.layout.task_on_board, fetchAndSetUserIcon, tasks);
            columnView.setAdapter(adapter);
            columnView.clearChoices();

            ListView tasksList = (ListView) columnView.findViewById(android.R.id.list);

            AdapterView.OnItemClickListener listener = new ShowTaskDetailsOnClickListener();
            tasksList.setOnItemClickListener(listener);

//            registerForContextMenu(tasksList);

            View viewForThisColumn = getViewForColumn(column);
            if (viewForThisColumn == null)
            {
               columnsContainer.addView(columnView);
            }
            else
            {
               int index = columnsContainer.indexOfChild(viewForThisColumn);
               columnsContainer.removeView(viewForThisColumn);
               columnsContainer.addView(columnView, index);
            }
         }
      });
   }

   public boolean onCreateOptionsMenu(Menu menu){
      return common.onCreateOptionsMenu(menu);
   }

   @Override
   public void showToast(String message, int lengthLong)
   {
      Toast.makeText(this, message, lengthLong).show();
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      return common.onOptionsItemSelected(item);
   }

   private int getColumnWidth()
   {
      int columnsVisibleOnScreen = 3;
      Display display = windowManager.getDefaultDisplay();

      return display.getWidth() / columnsVisibleOnScreen;
   }

   private class ShowTaskDetailsOnClickListener implements AdapterView.OnItemClickListener
   {
      public static final String CURRENT_FRAGMENT = "cur_task_detail_frag";

      protected LinearLayout taskLayout;
      protected TextView taskIdText;
      protected TextView taskDescription;

      protected Long taskId;

      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
      {
         initFields(view);

//         FragmentManager fragmentManager = getFragmentManager();
//         FragmentTransaction transaction = fragmentManager.beginTransaction();
//         transaction.setCustomAnimations(R.anim.slide_in_from_right, R.anim.slide_out_right); // todo animations

//      Fragment currentFragment = fragmentManager.findFragmentByTag(CURRENT_FRAGMENT);

//         transaction.commit();
      }

      protected void initFields(View view)
      {
         // widget fields
         taskLayout = (LinearLayout) view;
         taskIdText = (TextView) taskLayout.findViewById(R.id.task_id);
         taskDescription = (TextView) taskLayout.findViewById(R.id.bottom_text);

         // properties
         taskId = Long.valueOf((String) taskIdText.getText());
      }
   }

   private class HideTutorialOverlayOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View tutorialOverlay)
      {
         markTutorialAsDisplayed();
//         tutorialOverlay.setAlpha(0);

         // restart
         activityLauncher.launchTasksView(BoardActivity.this);
      }
   }

//   public boolean onDrag(View view, DragEvent dragEvent)
//      {
//         final int action = dragEvent.getAction();
//
//         switch (action)
//         {
//            // todo add highlight of target column
//            case DragEvent.ACTION_DROP:
//               Task task = (Task) dragEvent.getLocalState(); // todo it's misused a little here... should be "light data"
//               float x = this.boardScroller.getScrollX() + dragEvent.getX();
//               float y = this.boardScroller.getScrollY() + dragEvent.getY();
//
//               Log.i(TAG, "Task \"" + task.getTitle() + "\" moved to: (X=" + x + ", Y=" + y + ")");
//
//               onTaskDrop(task, x, y);
//
//               break;
//         }
//
//         return true;
//      }

//   @Override
//   public boolean onOptionsItemSelected(MenuItem item)
//   {
//      switch (item.getItemId())
//      {
//         case android.R.id.home:
//            Intent intent = new Intent(this, WorkspacesAndProjectsActivity.class);
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            return true;
//         default:
//            return super.onOptionsItemSelected(item);
//      }
//   }


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