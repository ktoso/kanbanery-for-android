package pl.project13.kanbanery.util.features;

import static pl.project13.kanbanery.util.StringUtils.lines;

/**
 * Thrown when an user tries to get a feature which is only supported by the paid version of the app
 *
 * @author Konrad Malawski
 */
public class FeatureUnsupportedInFreeVersionException extends RuntimeException
{

   private static final long serialVersionUID = 6078966279684594636L;

   public static final String MESSAGE = lines("This feature is not available in the FREE version of Kanbanery for Android.",
                                              "Please upgrade to the PRO version.",
                                              "Thanks!").toString();

   public FeatureUnsupportedInFreeVersionException()
   {
      super(MESSAGE);
   }

   public FeatureUnsupportedInFreeVersionException(String detailMessage)
   {
      super(detailMessage);
   }

}
