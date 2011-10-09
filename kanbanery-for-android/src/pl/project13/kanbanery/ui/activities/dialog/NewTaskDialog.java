package pl.project13.kanbanery.ui.activities.dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Task;
import pl.project13.janbanery.resources.TaskType;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.ui.common.handlers.FinishActivityOnClick;
import pl.project13.kanbanery.util.SimpleAsyncTask;
import pl.project13.kanbanery.util.Try;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.List;

/**
 * @author Konrad Malawski
 */
@DialogActivity
public class NewTaskDialog extends RoboActivity
{

   private final String TAG = getClass().getSimpleName();

   private static final int SIMPLE_SPINNER_ITEM = android.R.layout.simple_spinner_item;

   @InjectView(R.id.task_title)
   EditText vTaskTitle;

   @InjectView(R.id.task_type)
   Spinner vTaskType;

   @InjectView(R.id.task_description)
   EditText vTaskDescription;

   @InjectView(R.id.cancel_btn)
   Button vCancel;

   @InjectView(R.id.create_btn)
   Button vCreate;

   @Inject
   Janbanery janbanery;

   @Inject
   private ActionExecutor actionExecutor;
   
   List<TaskType> allTaskTypes;
   TaskType selectedTaskType;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.dialog_new_task);

      allTaskTypes = getTaskTypes();
      List<String> allTaskTypeNames = getTaskTypeNames(allTaskTypes);
      selectedTaskType = allTaskTypes.get(0);

      vTaskType
            .setAdapter(new ArrayAdapter<String>(NewTaskDialog.this, SIMPLE_SPINNER_ITEM, allTaskTypeNames));
      vTaskType.setOnItemSelectedListener(new OnTaskTypeSelectedListener());

      vCreate.setText(R.string.create_a_new_task);
      vCreate.setOnClickListener(new CreateTaskOnClick());
      vCancel.setOnClickListener(new FinishActivityOnClick(NewTaskDialog.this));
   }

   private List<TaskType> getTaskTypes()
   {
      return new SimpleAsyncTask<List<TaskType>>()
      {
         @Override
         protected List<TaskType> doInBackground()
         {
            List<TaskType> all = janbanery.taskTypes().all();
            return all;
         }
      }.get();
   }

   private List<String> getTaskTypeNames(List<TaskType> allTaskTypes)
   {
      return Lists.transform(allTaskTypes, new Function<TaskType, String>()
      {
         @Override
         public String apply(TaskType taskType)
         {
            return taskType.getName();
         }
      });
   }

   private class OnTaskTypeSelectedListener implements AdapterView.OnItemSelectedListener
   {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
      {
         selectedTaskType = allTaskTypes.get(i);
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView)
      {
         selectedTaskType = allTaskTypes.get(0);
      }
   }

   private class CreateTaskAsyncTask extends AsyncTask<Void, Void, Task>
   {
      private final Task task;

      private ProgressDialog pleaseWaitDialog;

      public CreateTaskAsyncTask(Task task)
      {
         this.task = task;
      }

      @Override
      protected void onPreExecute()
      {
         pleaseWaitDialog = new ProgressDialog(NewTaskDialog.this);
         pleaseWaitDialog.setIndeterminate(true);
         pleaseWaitDialog.setMessage("Creating task...");
         pleaseWaitDialog.show();
      }

      @Override
      protected Task doInBackground(Void... voids)
      {
         return actionExecutor.onlyForPremium(NewTaskDialog.this, Task.class, new Try.Get<Task>() {
            @Override
            public Task get()
            {
               Task createdTask = janbanery.tasks().create(task).get();
               return createdTask;
            }
         });
      }

      @Override
      protected void onPostExecute(Task createdTask)
      {
         Log.i(TAG, "Successfully created new task [" + task.getTitle() + "], returning from Activity...");

         pleaseWaitDialog.dismiss();

         Intent intent = Intents.ShowNewTask.prepareResponse(createdTask);
         NewTaskDialog.this.setResult(Intents.ShowNewTask.CODE, intent);
         NewTaskDialog.this.finish();
      }
   }

   private class CreateTaskOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         String title = vTaskTitle.getText().toString();
         String description = vTaskDescription.getText().toString();

         if (title.trim().length() == 0) // android 2.1 does not have isEmpty....
         {
            Toast.makeText(NewTaskDialog.this, "A task cannot have an empty title!", Toast.LENGTH_LONG).show();
            return;
         }

         Task.Builder taskBuilder = new Task.Builder(title, selectedTaskType);
         Task task = taskBuilder.description(description).build();

         //noinspection unchecked
         new CreateTaskAsyncTask(task).execute();
      }
   }
}
