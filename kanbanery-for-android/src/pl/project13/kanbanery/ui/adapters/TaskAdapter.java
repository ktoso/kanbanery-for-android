package pl.project13.kanbanery.ui.adapters;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Injector;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.core.flow.TaskMarkFlow;
import pl.project13.janbanery.resources.Comment;
import pl.project13.janbanery.resources.Estimate;
import pl.project13.janbanery.resources.SubTask;
import pl.project13.janbanery.resources.Task;
import pl.project13.janbanery.resources.TaskType;
import pl.project13.janbanery.resources.User;
import pl.project13.janbanery.util.JanbaneryAndroidUtils;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.ui.activities.HasTasksActivity;
import pl.project13.kanbanery.ui.common.handlers.ShowContextMenuOnClick;
import pl.project13.kanbanery.util.SimpleAsyncTask;
import pl.project13.kanbanery.util.cache.Cache;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import roboguice.util.RoboAsyncTask;

import java.util.List;

/**
 * @author Konrad Malawski
 */
public class TaskAdapter extends ArrayAdapter<Task>
{

   private static final String TAG = TaskAdapter.class.getSimpleName();

   @Inject
   private LayoutInflater layoutInflater;

   @Inject
   private ActivityLauncher activityLauncher;

   @Inject
   private Cache<User> userCache;

   @Inject
   private Cache<TaskType> taskTypeCache;

   @Inject
   private Cache<Estimate> estimateCache;

   @Inject
   ActionExecutor actionExecutor;

   @Inject
   private Janbanery janbanery;

   private Activity activity;
   private HasTasksActivity hasTasksActivity;

   private Injector injector;
   private FetchAndSetUserIcon fetchAndSetUserIcon;

   public TaskAdapter(Injector injector, HasTasksActivity hasTasksActivity, int textViewResourceId, FetchAndSetUserIcon fetchAndSetUserIcon, List<Task> tasks)
   {
      super(hasTasksActivity.getActivity(), textViewResourceId, tasks);

      injector.injectMembers(this);

      this.injector = injector;
      this.hasTasksActivity = hasTasksActivity;
      this.activity = hasTasksActivity.getActivity();
      this.fetchAndSetUserIcon = fetchAndSetUserIcon;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = convertView;

      if (v == null)
      {
         v = layoutInflater.inflate(R.layout.task_on_board, null);
      }

      Task task = getItem(position);
      if (task != null)
      {
         populateTaskView(v, task);
      }

      return v;
   }

   private void populateTaskView(final View v, final Task task)
   {
      LinearLayout middleContents = (LinearLayout) v.findViewById(R.id.task_middle_contents);

      final LinearLayout detailsContainer = (LinearLayout) v.findViewById(R.id.details_layout);
      hideDetailsContainer(detailsContainer);

      Button taskTypeBtn = (Button) v.findViewById(R.id.task_type_btn);
      Button estimateBtn = (Button) v.findViewById(R.id.estimate_btn);
      Button moveBtn = (Button) v.findViewById(R.id.move_btn);
      ImageButton deleteBtn = (ImageButton) v.findViewById(R.id.delete_btn);
      TextView taskTitleText = (TextView) v.findViewById(R.id.top_text);
      ImageButton vShowMoreDetails = (ImageButton) v.findViewById(R.id.details_btn);
      TextView hideDetails = (TextView) v.findViewById(R.id.hide_details_text_view);
      TextView description = (TextView) v.findViewById(R.id.description_txt);
//      TextView bottomText = (TextView) v.findViewById(R.id.bottom_text);
      TextView taskIdText = (TextView) v.findViewById(R.id.task_id);
      ImageButton ownerIconBtn = (ImageButton) v.findViewById(R.id.task_owner_image_btn);
      ImageButton vAddComment = (ImageButton) v.findViewById(R.id.new_comment_btn);
      Button vAddCommentInDetailsView = (Button) v.findViewById(R.id.new_comment_under_task_details_btn);
      Button vAddSubTaskInDetailsView = (Button) v.findViewById(R.id.new_subtask_under_task_details_btn);
      LinearLayout vCommentsLinearLayout = (LinearLayout) v.findViewById(R.id.comments_linear_layout);
      LinearLayout vSubTasksLinearLayout = (LinearLayout) v.findViewById(R.id.subtasks_linear_layout);


      final ImageView isReadyToPullImgBtn = (ImageView) v.findViewById(R.id.is_ready_to_pull);

      taskTitleText.setText(task.getTitle());

      moveBtn.setOnClickListener(new MoveTaskOnClick(task));

      // drag setup
      moveBtn.setOnLongClickListener(new DragTaskOnLongClick(v, task));

      deleteBtn.setOnClickListener(new DeleteOnClick(task));

      setTaskType(task, taskTypeBtn);
      taskTypeBtn.setOnClickListener(new ChangeTaskTypeOnClick(task));

      setEstimate(task, estimateBtn);
      estimateBtn.setOnClickListener(new ChangeEstimateOnClick(task));

      taskIdText.setText(String.valueOf(task.getId()));

      setUserIcon(ownerIconBtn, task.getOwnerId());
      ownerIconBtn.setOnClickListener(new ChangeOwnerOnClick(task));

      isReadyToPullImgBtn.setImageDrawable(getReadyToPullImage(task));
      isReadyToPullImgBtn.setOnClickListener(new ToggleReadyToPullOnClick(isReadyToPullImgBtn, task));

      vAddComment.setOnClickListener(new ShowAddCommentOnClick(task));
      // only visible in details view
      if (vAddCommentInDetailsView != null)
      {
         vAddCommentInDetailsView.setOnClickListener(new ShowAddCommentOnClick(task));
      }

      vAddSubTaskInDetailsView.setOnClickListener(new ShowAddSubTaskOnClick(task));
      vShowMoreDetails.setOnClickListener(new ShowDetailsOnClick(task, detailsContainer, vCommentsLinearLayout,
                                                                 vSubTasksLinearLayout));

      hideDetails.setOnClickListener(new HideDetailsOnClick(detailsContainer));

      Linkify.addLinks(description, Linkify.ALL);
      description.setText(task.getDescription());

      middleContents.setOnClickListener(new ShowContextMenuOnClick());
      taskTitleText.setOnClickListener(new ShowContextMenuOnClick());
      taskIdText.setOnClickListener(new ShowContextMenuOnClick());

      v.setTag(task.getId());
   }

   private void showDetailsContainer(LinearLayout detailsContainer)
   {
      detailsContainer.setVisibility(View.VISIBLE);
   }

   private void hideDetailsContainer(LinearLayout detailsContainer)
   {
      detailsContainer.setVisibility(View.GONE);
   }

   private void startDrag(View v, Task task)
   {
//      String title = task.getTitle();
//      ClipData clipData = ClipData.newPlainText(title, String.valueOf(task.getId()));
//
//      // Instantiates the drag shadow builder.
//      View.DragShadowBuilder shadowBuilder = new DragTaskShadowBuilder(v);
//
//      v.startDrag(clipData, shadowBuilder, task, 0);
   }

   private void setEstimate(Task task, TextView estimateTest)
   {
      Estimate estimate;

      final Long estimateId = task.getEstimateId();

      if (estimateId == null)
      {
         return;
      }

      if (estimateCache.isCacheHit(estimateId))
      {
         estimate = estimateCache.get(estimateId);
      }
      else
      {
         estimate = new SimpleAsyncTask<Estimate>()
         {
            @Override
            public Estimate doInBackground()
            {
               Estimate estimate = janbanery.estimates().byId(estimateId);
               return estimate;
            }
         }.get();

         estimateCache.cache(estimateId.toString(), estimate);
      }

      estimateTest.setText(estimate.getLabel());
   }

   private void setTaskType(Task task, TextView taskTypeText)
   {
      TaskType taskType = null;

      final Long taskTypeId = task.getTaskTypeId();
      if (taskTypeCache.isCacheHit(taskTypeId))
      {
         taskType = taskTypeCache.get(taskTypeId);
      }
      else
      {
         taskType = new SimpleAsyncTask<TaskType>()
         {
            @Override
            public TaskType doInBackground()
            {
               TaskType taskType = janbanery.taskTypes().byId(taskTypeId);
               return taskType;
            }
         }.get();

         taskTypeCache.cache(taskTypeId.toString(), taskType);
      }

      taskTypeText.setText(taskType.getName());
      taskTypeText.setTextColor(JanbaneryAndroidUtils.toAndroidColor(taskType.getTextColor()));
      taskTypeText.setBackgroundColor(JanbaneryAndroidUtils.toAndroidColor(taskType.getBackgroundColor()));
   }

   private void setUserIcon(final ImageView taskIcon, final Long ownerId)
   {
      String ownerKey = String.valueOf(ownerId);

      if (userCache.isCacheHit(ownerKey))
      {
         User user = userCache.get(ownerKey);

         fetchAndSetUserIcon.apply(user, taskIcon);
      }
      else
      {
         User user = new SimpleAsyncTask<User>()
         {
            @Override
            public User doInBackground()
            {
               User user = janbanery.users().byId(ownerId);
               return user;
            }
         }.get();

         userCache.cache(ownerKey, user);

         fetchAndSetUserIcon.apply(user, taskIcon);
      }
   }

   // todo fix this to net relay on exceptions
   private void toggleReadyToPullIcon(ImageView readyToPullImgBtn)
   {
      try
      {
         // todo no such animator on android 2.2??????
//         ObjectAnimator fadeOut = ObjectAnimator.ofFloat(readyToPullImgBtn, "alpha", 0f);
//         ObjectAnimator fadeIn = ObjectAnimator.ofFloat(readyToPullImgBtn, "alpha", 1f);
//         AnimatorSet animatorSet = new AnimatorSet();
//         animatorSet.playSequentially(fadeOut, fadeIn);
//         animatorSet.start();
      }
      catch (NoClassDefFoundError error)
      {
         Log.w(TAG, "Unable to use ObjectAnimator.ofFloat", error);
      }
   }

   /**
    * Toggle the Tasks "ready to pull" state and also push this change to Kanbanery
    *
    * @param task
    */
   private void toggleTaskReadyToPull(final Task task)
   {
      task.setReadyToPull(!task.getReadyToPull());

      pushReadyToPullState(task);
   }

   /**
    * Calls Kanbanery to tell it about the new state of the passed in task
    *
    * @param task
    */
   private void pushReadyToPullState(final Task task)
   {

      final boolean newStateIsReadyToPull = task.getReadyToPull();

      new RoboAsyncTask<Void>()
      {

         @Inject
         protected Janbanery janbanery;

         @Override
         public Void call() throws Exception
         {

            TaskMarkFlow markFlow = janbanery.tasks().mark(task);

            if (newStateIsReadyToPull)
            {
               markFlow.asReadyToPull();
            }
            else
            {
               markFlow.asNotReadyToPull();
            }

            return null;
         }
      }.execute();
   }

   private Drawable getReadyToPullImage(Task task)
   {
      Boolean readyToPull = task.getReadyToPull();
      int rDrawable = readyToPull != null && readyToPull ? R.drawable.ic_ready_to_pull : R.drawable.ic_not_ready_to_pull;

      return getContext().getResources().getDrawable(rDrawable);
   }

   private class MoveTaskOnClick implements View.OnClickListener
   {
      private final Task task;

      public MoveTaskOnClick(Task task) {this.task = task;}

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onMoveTaskClicked(view, task);
      }
   }

   private class DragTaskOnLongClick implements View.OnLongClickListener
   {
      private final View v;
      private final Task task;

      public DragTaskOnLongClick(View v, Task task)
      {
         this.v = v;
         this.task = task;
      }

      @Override
      public boolean onLongClick(View view)
      {
         startDrag(v, task);
         return true;
      }
   }

   private class DeleteOnClick implements View.OnClickListener
   {
      private final Task task;

      public DeleteOnClick(Task task) {this.task = task;}

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onDeleteTaskClicked(view, task);
      }
   }

   private class ChangeTaskTypeOnClick implements View.OnClickListener
   {
      private final Task task;

      public ChangeTaskTypeOnClick(Task task) {this.task = task;}

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onTaskTypeClicked(view, task);
      }
   }

   private class ChangeEstimateOnClick implements View.OnClickListener
   {
      private final Task task;

      public ChangeEstimateOnClick(Task task) {this.task = task;}

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onEstimateClicked(view, task);
      }
   }

   private class ChangeOwnerOnClick implements View.OnClickListener
   {
      private final Task task;

      public ChangeOwnerOnClick(Task task)
      {
         this.task = task;
      }

      @Override
      public void onClick(View view)
      {
         Log.i(TAG, "Owner image clicked.");
         hasTasksActivity.onOwnerImageClicked(view, task);
      }
   }

   private class ToggleReadyToPullOnClick implements View.OnClickListener
   {
      private final ImageView readyToPullImgBtn;
      private final Task task;

      public ToggleReadyToPullOnClick(ImageView readyToPullImgBtn, Task task)
      {
         this.readyToPullImgBtn = readyToPullImgBtn;
         this.task = task;
      }

      @Override
      public void onClick(View view)
      {
         actionExecutor.onlyForPremium(getContext(), new Runnable()
         {
            @Override
            public void run()
            {
               toggleReadyToPullIcon(readyToPullImgBtn);

               toggleTaskReadyToPull(task);
               readyToPullImgBtn.setImageDrawable(getReadyToPullImage(task));
            }
         });
      }
   }

   private class HideDetailsOnClick implements View.OnClickListener
   {
      private final LinearLayout detailsContainer;

      public HideDetailsOnClick(LinearLayout detailsContainer) {this.detailsContainer = detailsContainer;}

      @Override
      public void onClick(View view)
      {
         hideDetailsContainer(detailsContainer);
      }
   }

   private class ShowDetailsOnClick implements View.OnClickListener
   {
      private Task task; // will be "refreshed"

      private final LinearLayout detailsContainer;
      private final LinearLayout commentsLinearLayout;
      private final LinearLayout subTasksLinearLayout;

      public ShowDetailsOnClick(Task task,
                                LinearLayout detailsContainer,
                                LinearLayout commentsLinearLayout,
                                LinearLayout subTasksLinearLayout)
      {
         this.task = task;
         this.detailsContainer = detailsContainer;
         this.commentsLinearLayout = commentsLinearLayout;
         this.subTasksLinearLayout = subTasksLinearLayout;
      }

      @Override
      public void onClick(final View view)
      {
         if (detailsContainer.isShown())
         {
            hideDetailsContainer(detailsContainer);
            ImageButton.class.cast(view).setImageResource(R.drawable.icon_expand);
            return;
         }

         //noinspection unchecked
         new AsyncTask<Void, Void, TaskDetails>()
         {
            private ProgressDialog pleaseWaitDialog;

            @Override
            protected void onPreExecute()
            {
               pleaseWaitDialog = new ProgressDialog(activity);
               pleaseWaitDialog.setIndeterminate(true);
               pleaseWaitDialog.setMessage("Loading task details...");
               pleaseWaitDialog.show();
            }

            @Override
            protected TaskDetails doInBackground(Void... voids)
            {
               task = janbanery.tasks().refresh(task);

               List<Comment> taskTypes = janbanery.comments().of(task).all();
               List<SubTask> subTasks = janbanery.subTasks().of(task).all();

               return new TaskDetails(taskTypes, subTasks);
            }

            @Override
            protected void onPostExecute(TaskDetails taskDetails)
            {
               pleaseWaitDialog.dismiss();

               processTaskDetails(view, taskDetails);
            }
         }.execute();
      }

      private void processTaskDetails(View view, TaskDetails taskDetails)
      {
         showDetailsContainer(detailsContainer);
         ImageButton.class.cast(view).setImageResource(R.drawable.icon_collapse);

         reloadListView(commentsLinearLayout,
                        taskDetails.comments,
                        new CommentAdapter(getContext(), injector, taskDetails.comments));

         reloadListView(subTasksLinearLayout,
                        taskDetails.subTasks,
                        new SubTaskAdapter(getContext(), injector, taskDetails.subTasks));
      }

      private <T> void reloadListView(LinearLayout commentsLinearLayout1, List<T> items, ArrayAdapter<T> commentAdapter)
      {
         commentsLinearLayout1.removeAllViews();
         for (int i = 0; i < items.size(); i++)
         {
            View commentView = commentAdapter.getView(i, null, null);
            commentsLinearLayout1.addView(commentView);
         }
      }

      private class TaskDetails
      {

         final List<Comment> comments;
         final List<SubTask> subTasks;

         private TaskDetails(List<Comment> comments, List<SubTask> subTasks)
         {
            this.comments = comments;
            this.subTasks = subTasks;
         }
      }
   }

   private class ShowAddCommentOnClick implements View.OnClickListener
   {
      private Task task;

      public ShowAddCommentOnClick(Task task)
      {
         this.task = task;
      }

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onAddCommentClicked(view, task);
      }
   }

   private class ShowAddSubTaskOnClick implements View.OnClickListener
   {
      private final Task task;

      public ShowAddSubTaskOnClick(Task task) {this.task = task;}

      @Override
      public void onClick(View view)
      {
         hasTasksActivity.onAddSubTask(view, task);
      }
   }
}
