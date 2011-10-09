package pl.project13.kanbanery.util.kanbanery;

import android.graphics.Color;
import android.widget.TextView;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;

import java.util.List;

/**
 * @author Konrad Malawski
 */
public class ColumnUtils
{
   public static void columnCapacityText(TextView columnText, Column column, List<Task> tasks)
   {
      ColumnCapacity columnCapacity = getColumnCapacity(column, tasks);

      columnText.setText(columnCapacity.title);

      if (columnCapacity.isOverLimit)
      {
         columnText.setTextColor(Color.RED);
      }
      else
      {
         columnText.setTextColor(Color.WHITE);
      }
   }

   public static ColumnCapacity getColumnCapacity(Column column, List<Task> tasks)
   {
      StringBuilder sb = new StringBuilder(column.getName());
      boolean isOverLimit = false;

      Integer columnCapacity = column.getCapacity();
      Integer taskCount = tasks.size();

      sb.append(" (").append(taskCount);
      if (columnCapacity != null)
      {
         sb.append(" / ").append(columnCapacity);
         isOverLimit = taskCount > columnCapacity;
      }
      sb.append(")");
      String title = sb.toString();

      return new ColumnCapacity(title, isOverLimit);
   }

   public static List<String> namesFor(List<Column> columns)
   {
      return ImmutableList.copyOf(Collections2.transform(columns, new Function<Column, String>()
      {
         @Override
         public String apply(Column column)
         {
            return column.getName();
         }
      }));
   }

   public static class ColumnCapacity
   {
      public final String title;
      public final boolean isOverLimit;

      public ColumnCapacity(String title, boolean overLimit)
      {
         this.title = title;
         isOverLimit = overLimit;
      }


   }
}
