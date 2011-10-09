package pl.project13.kanbanery.util;

import android.app.ProgressDialog;
import android.content.Context;

public abstract class SimplePleaseWaitAsyncTask<T> extends SimpleAsyncTask<T>
{

   private final CharSequence message;
   private final Context context;

   private ProgressDialog progressDialog;

   public SimplePleaseWaitAsyncTask(Context context, String message)
   {
      this.context = context;
      this.message = message;
   }

   @Override
   protected void onPreExecute()
   {
      progressDialog = new ProgressDialog(context);
      progressDialog.setIndeterminate(true);
      progressDialog.setMessage(message);
   }

   @Override
   protected void onPostExecute(T t)
   {
      progressDialog.dismiss();
   }
}
