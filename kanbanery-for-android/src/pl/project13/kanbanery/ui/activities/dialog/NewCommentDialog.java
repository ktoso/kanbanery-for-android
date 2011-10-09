package pl.project13.kanbanery.ui.activities.dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Comment;
import pl.project13.janbanery.resources.Task;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

@DialogActivity
public class NewCommentDialog extends RoboActivity
{

   private static final String TAG = NewCommentDialog.class.getSimpleName();

   @Inject
   private Janbanery janbanery;

   @Inject
   private ActionExecutor actionExecutor;

   @InjectView(R.id.create_btn)
   private Button vCreate;

   @InjectView(R.id.cancel_btn)
   private Button vCancel;
   @InjectView(R.id.body)
   private EditText vCommentBody;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.dialog_with_one_edit_text);

      final Task task = Intents.ShowNewComment.getDataFrom(getIntent());

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
         final String text = vCommentBody.getText().toString();

         //noinspection unchecked
         new AsyncTask<Void, Void, Task>()
         {
            private ProgressDialog pleaseWaitDialog;

            @Override
            protected void onPreExecute()
            {
               pleaseWaitDialog = new ProgressDialog(NewCommentDialog.this);
               pleaseWaitDialog.setIndeterminate(true);
               pleaseWaitDialog.setMessage("Creating comment...");
               pleaseWaitDialog.show();
            }

            @Override
            protected Task doInBackground(Void... voids)
            {
               actionExecutor.onlyForPremium(NewCommentDialog.this, new Runnable()
               {
                  @Override
                  public void run()
                  {
                     janbanery.comments().of(task)
                              .create(new Comment(text));
                  }
               });

               return janbanery.tasks().refresh(task);
            }

            @Override
            protected void onPostExecute(Task createdTask)
            {
               Log.i(TAG, "Successfully created new comment for [" + task.getTitle() + "], returning from Activity...");

               pleaseWaitDialog.dismiss();

               Intent intent = Intents.ShowNewComment.prepareResponse(createdTask);
               NewCommentDialog.this.setResult(Intents.ShowNewComment.CODE, intent);
               NewCommentDialog.this.finish();
            }

         }.execute();
      }
   }

   private class CloseActivityOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         NewCommentDialog.this.finish();
      }
   }
}