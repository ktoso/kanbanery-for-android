package pl.project13.kanbanery.util;

import android.util.Log;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.util.List;
import java.util.Map;

public class MapQueries<T>
{
   private  static final String TAG = MapQueries.class.getSimpleName();
   
   public static <T> SearchInFor<T> find(T findWhat)
   {
      return new SearchInFor<T>(findWhat);
   }

   public static class SearchInFor<T>
   {
      private T target;

      public SearchInFor(T target)
      {
         this.target = target;
      }


      public <T> OperationOnFoundItem<T> in(Map<Integer, List<T>> columnTasksMap)
      {
         for (Integer key : columnTasksMap.keySet())
         {
            List<T> tasks = columnTasksMap.get(key);

            T t = Iterables.find(tasks, new EqualsPredicate<T>(), null);

            if (t != null)
            {
               return new OperationOnFoundItem<T>(tasks, t);
            }
         }

         //noinspection unchecked
         return new NoOpOperationOnFoundItem();
      }

      private class EqualsPredicate<T> implements Predicate<T>
      {
         @Override
         public boolean apply(T t)
         {
            return t.equals(target);
         }
      }

   }
   
   private static class NoOpOperationOnFoundItem extends OperationOnFoundItem
   {
      public NoOpOperationOnFoundItem()
      {
         super(null, null);
      }

      @Override
      public void andReplaceWith(Object replacement)
      {
         Log.e(TAG, "[NOOP] Unable to replace item, as it was not found in the collection...");
      }
   }
   
   
   public static class OperationOnFoundItem<T>
   {
      private List<T> items;
      private T found;

      public OperationOnFoundItem(List<T> items, T t)
                  {
                     this.items = items;
                     this.found = t;
                  }
      
      public void andReplaceWith(T replacement) {
         int positionOfFound = items.indexOf(found);
         
         items.remove(positionOfFound);
         items.add(positionOfFound, replacement);
      }

      public void andRemoveIt()
      {
         items.remove(found);
      }
   }
}
