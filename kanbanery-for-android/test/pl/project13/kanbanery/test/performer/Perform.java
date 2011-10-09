package pl.project13.kanbanery.test.performer;

import android.widget.Button;
import android.widget.EditText;

public class Perform
{
   public static TypeTextIntoFieldFlow type(String value)
   {
      return new TypeTextIntoFieldFlow(value);
   }

   public static void clear(EditText editText)
   {
      editText.setText("");
   }

   public static void click(Button button)
   {
      button.performClick();
   }

   public static void clickLong(Button button)
   {
      button.performLongClick();
   }
}
