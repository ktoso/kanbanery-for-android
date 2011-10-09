package pl.project13.kanbanery.util;

import com.google.common.base.Predicate;
import pl.project13.janbanery.resources.Column;

// todo move to janbanery
public class ColumnByPositionPredicate implements Predicate<Column>
{
   private final Integer columnPosition;

   public ColumnByPositionPredicate(Integer columnPosition) {
      this.columnPosition = columnPosition;
   }

   @Override
   public boolean apply(Column object)
   {
      return object.getPosition().equals(columnPosition);
   }
}
