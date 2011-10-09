package pl.project13.kanbanery.util.kanbanery;

import pl.project13.janbanery.resources.User;

/**
 * Created by ags on 8/20/11 at 11:08 PM
 */
public class UserUtils
{
   public static String prepareDisplayableUsername(User user)
   {
      String firstName = user.getFirstName();
      if (firstName == null || firstName.equals("null"))
      {
         firstName = "";
      }
      String lastName = user.getLastName();
      if (lastName == null || lastName.equals("null"))
      {
         lastName = "";
      }

      String setNameTo = firstName + " " + lastName;

      if (setNameTo.trim().equals(""))
      {
         setNameTo = user.getEmail();
      }
      return setNameTo;
   }
}
