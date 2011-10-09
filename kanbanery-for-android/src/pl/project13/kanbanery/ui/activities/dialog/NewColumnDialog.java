package pl.project13.kanbanery.ui.activities.dialog;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Column;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.Try;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;

@DialogActivity
public class NewColumnDialog extends RoboActivity
{

   @Inject
   private Janbanery janbanery;

   @Inject
   private ActionExecutor actionExecutor;

   @InjectView(R.id.body)
   private EditText vColumnName;

   @InjectView(R.id.after_column)
   private Spinner vSelectedColumn;

   @InjectView(R.id.capacity)
   private Spinner vCapacity;

   @InjectView(R.id.create_new_column_btn)
   private Button vCreate;

   @InjectView(R.id.cancel_btn)
   private Button vCancel;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.dialog_new_column);

      Intents.ShowNewColumn.Data data = Intents.ShowNewColumn.getDataFrom(getIntent());
      initData(data);

      vCreate.setOnClickListener(new CreateColumnOnClick());
      vCancel.setOnClickListener(new FinishOnClick());
   }

   private void initData(Intents.ShowNewColumn.Data data)
   {
      Collection<String> columnNames = Collections2.transform(data.columns, new Function<Column, String>()
      {
         @Override
         public String apply(Column column)
         {
            return column.getName();
         }
      });

      ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, newArrayList(columnNames));
      vSelectedColumn.setAdapter(adapter);
   }

   private class CreateColumnOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         String name = vColumnName.getText().toString();
         int capacity = vCapacity.getSelectedItemPosition();

         int selectedPosition = vSelectedColumn.getSelectedItemPosition() + 1; // kanbanery is 1 based, android is 0
         final Column columnOnPositionSelected = new Column();
         columnOnPositionSelected.setPosition(selectedPosition);

         if (name.trim().length() == 0)
         {
            Toast.makeText(NewColumnDialog.this, "The column's name cannot be empty!", Toast.LENGTH_LONG).show();
            return;
         }

         final Column newColumn = new Column(name);
         if (capacity != 0)
         {
            newColumn.setCapacity(capacity);
         }

         //noinspection unchecked
         new AsyncTask<Void, Void, Column>()
         {
            ProgressDialog pleaseWaitDialog;

            @Override
            protected void onPreExecute()
            {
               pleaseWaitDialog = new ProgressDialog(NewColumnDialog.this);
               pleaseWaitDialog.setIndeterminate(true);
               pleaseWaitDialog.setMessage("Creating column...");
               pleaseWaitDialog.show();
            }

            @Override
            protected Column doInBackground(Void... voids)
            {
               return actionExecutor.onlyForPremium(NewColumnDialog.this, Column.class, new Try.Get()
               {
                  @Override
                  public Object get()
                  {
                     return janbanery.columns()
                                     .create(newColumn)
                                     .after(columnOnPositionSelected)
                                     .get();
                  }
               });
            }

            @Override
            protected void onPostExecute(Column column)
            {
               pleaseWaitDialog.dismiss();

               Intent intent = Intents.ShowNewColumn.prepareResponse(column);
               NewColumnDialog.this.setResult(Intents.ShowNewColumn.CODE, intent);
               NewColumnDialog.this.finish();
            }
         }.execute();
      }
   }

   private class FinishOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         NewColumnDialog.this.finish();
      }
   }
}