package pl.project13.kanbanery.util;

import android.util.Log;

public class Try
{
   private static final String TAG = Try.class.getSimpleName();

   public static interface Get<T>
   {
      T get();
   }

   public static ToGet times(int times)
   {
      return new ToGet(times);
   }

   public static class ToGet
   {
      private int times;

      public ToGet(int times)
      {
         this.times = times;
      }

      public <T> T to(Get<T> getter)
      {
         for (int i = 1; i <= times; i++)
         {
            try
            {
               return getter.get();
            }
            catch (Exception ex)
            {
               Log.e(TAG, "Tried to get " + i + "th time, but got " + ex.getClass().getSimpleName(), ex);
            }
         }
         throw new RuntimeException("Unable to get, even after " + times + " tries!");
      }
   }
}
