package pl.project13.kanbanery.test.performer;

import android.widget.EditText;

public class TypeTextIntoFieldFlow
{
   private String value;

   public TypeTextIntoFieldFlow(String value)
   {
      this.value = value;
   }

   public void into(EditText targetField)
   {
      targetField.setText(value);
   }
}
