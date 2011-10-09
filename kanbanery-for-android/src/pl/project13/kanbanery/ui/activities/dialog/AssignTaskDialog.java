package pl.project13.kanbanery.ui.activities.dialog;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Task;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.ui.adapters.SelectionActivity;
import pl.project13.kanbanery.ui.adapters.UserAdapter;
import pl.project13.kanbanery.util.cache.Cache;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import pl.project13.kanbanery.util.kanbanery.UserUtils;
import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectView;

import java.util.List;

@DialogActivity
public class AssignTaskDialog extends RoboListActivity implements SelectionActivity<User>
{

   private static final String TAG = AssignTaskDialog.class.getSimpleName();

   @Inject
   private Janbanery janbanery;

   @Inject
   private Cache<User> userCache;

   @Inject
   private FetchAndSetUserIcon fetchAndSetUserIcon;

   @Inject
   private ActionExecutor actionExecutor;

   @InjectView(R.id.create_btn)
   private Button vCreate;

   @InjectView(R.id.cancel_btn)
   private Button vCancel;

   private User selectedUser;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.dialog_assign_task);

      final Task task = Intents.ShowNewComment.getDataFrom(getIntent());

      vCreate.setText("Assign");
      vCreate.setOnClickListener(new AssignTaskOnClick(task));

      vCancel.setOnClickListener(new CloseActivityOnClick());

      //noinspection unchecked
      new AsyncTask<Void, Void, List<User>>()
      {
         private ProgressDialog pleaseWaitDialog;

         @Override
         protected void onPreExecute()
         {
            pleaseWaitDialog = new ProgressDialog(AssignTaskDialog.this);
            pleaseWaitDialog.setIndeterminate(true);
            pleaseWaitDialog.setMessage("Loading users...");
            pleaseWaitDialog.show();
         }

         @Override
         protected List<User> doInBackground(Void... voids)
         {
            List<User> users = janbanery.users().allWithNobody();
            return users;
         }

         @Override
         protected void onPostExecute(List<User> users)
         {
            pleaseWaitDialog.dismiss();

            setListAdapter(new UserAdapter(AssignTaskDialog.this, getInjector(), users));
         }
      }.execute();
   }

   @Override
   protected void onListItemClick(ListView l, View v, int position, long id)
   {
      selectedUser = (User) getListAdapter().getItem(position);
      Log.i(TAG, "Selected user: " + selectedUser);
   }

   // todo remove this method, it SUCKS
   @Override
   public void onSelected(User selectionMade)
   {
      this.selectedUser = selectionMade;
   }

   @Override
   public Context getContext()
   {
      return this;
   }

   private class AssignTaskOnClick implements View.OnClickListener
   {
      private final Task task;

      public AssignTaskOnClick(Task task)
      {
         this.task = task;
      }

      @Override
      public void onClick(View view)
      {
         if (selectedUser == null)
         {
            selectedUser = new User.NoOne();
         }

         AlertDialog.Builder builder = new AlertDialog.Builder(AssignTaskDialog.this);
         String userName = UserUtils.prepareDisplayableUsername(selectedUser);

         final User finalSelectedUser = selectedUser;


         builder.setMessage("Assign task '" + task.getTitle() + "' to: " + userName + "?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                   public void onClick(DialogInterface dialog, int id)
                   {
                      assignIt(finalSelectedUser);
                   }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                   public void onClick(DialogInterface dialog, int id)
                   {
                      dialog.cancel();
                   }
                })
                .show();
      }

      private void assignIt(final User selectedUser)
      {
         //noinspection unchecked
         new AsyncTask<Void, Void, Task>()
         {
            private ProgressDialog pleaseWaitDialog;

            @Override
            protected void onPreExecute()
            {
               pleaseWaitDialog = new ProgressDialog(AssignTaskDialog.this);
               pleaseWaitDialog.setIndeterminate(true);
               pleaseWaitDialog.setMessage("Assigning task...");
               pleaseWaitDialog.show();
            }

            @Override
            protected Task doInBackground(Void... voids)
            {
               actionExecutor.onlyForPremium(AssignTaskDialog.this, new Runnable()
               {
                  @Override
                  public void run()
                  {
                     janbanery.tasks().assign(task).to(selectedUser);
                     task.setOwnerId(selectedUser.getId());
                  }
               });


               return task;
            }

            @Override
            protected void onPostExecute(Task createdTask)
            {
               Log.i(TAG, "Successfully assigned task [" + task.getTitle() + "] "
                     + "to [" + task.getOwnerId() + "], returning from Activity...");

               pleaseWaitDialog.dismiss();

               Intent intent = Intents.ShowAssignTask.prepareResponse(createdTask);
               AssignTaskDialog.this.setResult(Intents.ShowAssignTask.CODE, intent);
               AssignTaskDialog.this.finish();
            }

         }.execute();
      }
   }

   private class CloseActivityOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         AssignTaskDialog.this.finish();
      }
   }
}