package pl.project13.kanbanery.util;

import com.google.common.base.Predicate;
import pl.project13.janbanery.resources.Column;

// todo move to janbanery
public class ColumnByIdPredicate implements Predicate<Column>
{
   private final Long columnId;

   public ColumnByIdPredicate(Long columnId) {
      this.columnId = columnId;
   }

   @Override
   public boolean apply(Column object)
   {
      return object.getId().equals(columnId);
   }
}
