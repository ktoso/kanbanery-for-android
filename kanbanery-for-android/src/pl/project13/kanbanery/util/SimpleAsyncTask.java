package pl.project13.kanbanery.util;

import android.os.AsyncTask;
import android.util.Log;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Konrad Malawski
 */
public abstract class SimpleAsyncTask<T>
{
   private final String TAG = SimpleAsyncTask.class.getSimpleName();

   protected abstract T doInBackground();

   @SuppressWarnings("unchecked")
   public T get()
   {
      T result = null;

      try
      {
         result = new AsyncTask<Void, Void, T>()
         {
            @Override
            protected void onPreExecute()
            {
               SimpleAsyncTask.this.onPreExecute();
            }

            @Override
            protected void onPostExecute(T t)
            {
               SimpleAsyncTask.this.onPostExecute(t);
            }

            @Override
            protected void onProgressUpdate(Void... values)
            {
               super.onProgressUpdate(values);    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            protected void onCancelled()
            {
               SimpleAsyncTask.this.onCancelled();
            }

            @Override
            protected T doInBackground(Void... voids)
            {
               return SimpleAsyncTask.this.doInBackground();
            }
         }.execute()
          .get(20, TimeUnit.SECONDS);

      }
      catch (InterruptedException e)
      {
         Log.e(TAG, "InterruptedException while SimpleAsyncTask get()...\n" + e.toString());
      }
      catch (ExecutionException e)
      {
         Log.e(TAG, "ExecutionException while SimpleAsyncTask get()...\n" + e.toString());
      }
      catch (TimeoutException e)
      {
         e.printStackTrace();
      }

      return result;
   }

   protected void onPostExecute(T t)
   {
   }

   protected void onPreExecute()
   {
   }

   protected void onCancelled(T t)
   {
   }

   protected void onCancelled()
   {
   }
}
