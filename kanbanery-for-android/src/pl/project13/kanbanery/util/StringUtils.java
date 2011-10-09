package pl.project13.kanbanery.util;

import com.google.common.base.Joiner;

/**
 * @author Konrad Malawski
 */
public class StringUtils
{

   /**
    * Create \n separated representation of the passed in strings
    *
    * @param lines string to form the new string
    * @return A \n separated string
    */
   public static CharSequence lines(String... lines)
   {
      return Joiner.on("\n").join(lines);
   }

   public static String yesNo(Boolean bool)
   {
      return bool == null ? "no" : bool ? "yes" : "no";
   }
}
