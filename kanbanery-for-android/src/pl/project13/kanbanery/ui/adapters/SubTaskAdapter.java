package pl.project13.kanbanery.ui.adapters;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.inject.Inject;
import com.google.inject.Injector;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.core.flow.SubTaskFlow;
import pl.project13.janbanery.resources.SubTask;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.util.cache.Cache;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;

import java.util.List;

class SubTaskAdapter extends ArrayAdapter<SubTask>
{

   private static final String TAG = SubTaskAdapter.class.getSimpleName();

   @Inject
   private LayoutInflater layoutInflater;

   @Inject
   private Janbanery janbanery;

   @Inject
   private Cache<User> userCache;

   @Inject
   private ActionExecutor actionExecutor;

   public SubTaskAdapter(Context context, Injector injector, List<SubTask> subTasks)
   {
      super(context, R.layout.subtask, subTasks);

      injector.injectMembers(this);
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent)
   {
      View v = convertView;

      if (v == null)
      {
         v = layoutInflater.inflate(R.layout.subtask, null);
      }

      SubTask subTask = getItem(position);
      if (subTask != null)
      {
         populateCommentView(v, subTask);
      }

      return v;
   }

   private void populateCommentView(View v, SubTask subTask)
   {
      TextView vBody = (TextView) v.findViewById(R.id.subtask_body);
      CheckBox vCheckBox = (CheckBox) v.findViewById(R.id.subtask_status_checkbox);

      vBody.setText(subTask.getBody());

      vCheckBox.setChecked(subTask.getCompleted());
      vCheckBox.setOnClickListener(new ToggleTaskStatusOnClick(subTask));
   }

   private class ToggleTaskStatusOnClick implements View.OnClickListener
   {
      private SubTask subTask;

      public ToggleTaskStatusOnClick(SubTask subTask)
      {
         this.subTask = subTask;
      }

      @Override
      public void onClick(View view)
      {
         subTask.setCompleted(!subTask.getCompleted());
         markAs();
      }

      private void markAs()
      {
         final String message = getLoadingMessage(subTask);

         //noinspection unchecked
         new AsyncTask<Void, Void, SubTaskFlow>()
         {
            private ProgressDialog pleaseWaitDialog;

            @Override
                  protected void onPreExecute()
                  {
                     pleaseWaitDialog = new ProgressDialog(getContext());
                     pleaseWaitDialog.setIndeterminate(true);
                     pleaseWaitDialog.setMessage(message);
                     pleaseWaitDialog.show();
                  }

            @Override
            protected SubTaskFlow doInBackground(Void ... voids)
            {
               actionExecutor.onlyForPremium(getContext(), new Runnable()
               {
                  @Override
                  public void run()
                  {
                     janbanery.subTasks().update(subTask, subTask);
                  }
               });

               return null;
            }

            @Override
            protected void onPostExecute(SubTaskFlow ignore)
            {
               pleaseWaitDialog.dismiss();
            }

         }.execute();
      }

      private String getLoadingMessage(SubTask subTask)
      {
         String completedOrNotYet = (subTask.getCompleted() ? "" : "not yet ") + "completed";
         String message = String.format("Marking '%s' as %s...", subTask.getBody(), completedOrNotYet);

         Log.i(TAG, message);

         return message;
      }
   }
}
