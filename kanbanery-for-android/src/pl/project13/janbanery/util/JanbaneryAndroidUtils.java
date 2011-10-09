package pl.project13.janbanery.util;

import android.graphics.Color;

/**
 * @author Konrad Malawski
 */
public class JanbaneryAndroidUtils
{
   private JanbaneryAndroidUtils()
   {
   }

   /**
    * Converts a color, as given by Kanbanery, to a Android readable format.
    * This is needed as sometimes Kanbanery returns "#222", which should be "#222222".
    * Then, {@link Color.parseColor(String)} is being called
    *
    * @param colorString the string to assure that it's parseable by android
    * @return the parsed color
    */
   public static int toAndroidColor(String colorString)
   {
      if (colorString.matches("\\#[0-9A-Fa-f]{3}"))
      {
         colorString = new StringBuilder().append("#")
                                          .append(colorString.charAt(1)).append(colorString.charAt(1))
                                          .append(colorString.charAt(2)).append(colorString.charAt(2))
                                          .append(colorString.charAt(3)).append(colorString.charAt(3))
                                          .toString();
      }

      return Color.parseColor(colorString);
   }
}
