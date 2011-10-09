package pl.project13.kanbanery.util;

import android.os.AsyncTask;

/**
 * @author Konrad Malawski
 */
public abstract class SimpleVoidAsyncTask
{

   protected abstract void execute();

   protected void postExecute()
   {
   }

   @SuppressWarnings({"unchecked"})
   public void go()
   {
      new AsyncTask<Void, Void, Void>()
      {
         @Override
         protected Void doInBackground(Void... voids)
         {
            SimpleVoidAsyncTask.this.execute();

            return null;
         }

         @Override
         protected void onPostExecute(Void aVoid)
         {
            SimpleVoidAsyncTask.this.postExecute();
         }
      }.execute();
   }
}

