package pl.project13.kanbanery.ui.activities.dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.SubTask;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

@DialogActivity
public class NewSubTaskDialog extends RoboActivity
{

   private static final String TAG = NewSubTaskDialog.class.getSimpleName();

   @Inject
   Janbanery janbanery;

   @Inject
   private ActionExecutor actionExecutor;

   @InjectView(R.id.create_btn)
   Button vCreate;

   @InjectView(R.id.cancel_btn)
   Button vCancel;

   @InjectView(R.id.body_label)
   TextView vBodyLabel;
   @InjectView(R.id.body)
   EditText vBody;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);


      setContentView(R.layout.dialog_with_one_edit_text);

      final Task task = Intents.ShowNewSubTask.getDataFrom(getIntent());

      vBodyLabel.setText(R.string.new_subtask_body_label);

      vCreate.setText(R.string.new_subtask_create);
      vCreate.setOnClickListener(new CreateCommentOnClick(task));
      vCancel.setOnClickListener(new CloseActivityOnClick());
   }

   private class CreateCommentOnClick implements View.OnClickListener
   {
      private final Task task;

      public CreateCommentOnClick(Task task)
      {
         this.task = task;
      }

      @Override
      public void onClick(View view)
      {
         final String text = vBody.getText().toString();

         //noinspection unchecked
         new AsyncTask<Void, Void, Task>()
         {
            private ProgressDialog pleaseWaitDialog;

            @Override
            protected void onPreExecute()
            {
               pleaseWaitDialog = new ProgressDialog(NewSubTaskDialog.this);
               pleaseWaitDialog.setIndeterminate(true);
               pleaseWaitDialog.setMessage("Creating subtask...");
               pleaseWaitDialog.show();
            }

            @Override
            protected Task doInBackground(Void... voids)
            {
               Log.i(TAG, "Creating subtask with text [" + text + "]");
               actionExecutor.onlyForPremium(NewSubTaskDialog.this, new Runnable()
               {
                  @Override
                  public void run()
                  {
                     janbanery.subTasks().of(task)
                              .create(new SubTask(text));
                  }
               });

               Log.i(TAG, "Done creating subtask...");
               return task;
            }

            @Override
            protected void onPostExecute(Task createdTask)
            {
               Log.i(TAG, "Successfully created new subtask for [" + task.getTitle() + "], returning from Activity...");

               pleaseWaitDialog.dismiss();

               Intent intent = Intents.ShowNewSubTask.prepareResponse(createdTask);
               NewSubTaskDialog.this.setResult(Intents.ShowNewSubTask.CODE, intent);
               NewSubTaskDialog.this.finish();
            }

         }.execute();
      }
   }

   private class CloseActivityOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         NewSubTaskDialog.this.finish();
      }
   }
}