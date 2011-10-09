package pl.project13.kanbanery.guice.helpers;

import android.content.res.Configuration;
import android.content.res.Resources;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Singleton;

/**
 * @author Konrad Malawski
 */
@Singleton
public class TabletDetector
{

   /**
    * Returns true if the devices screen size is at least SCREENLAYOUT_SIZE_XLARGE.
    *
    * @return true if we are running on a tablet (that is, on an XLARGE screen)
    */
   public boolean isTablet()
   {
      return isHoneycombOrNewer();
   }

   @VisibleForTesting
   public Configuration getConfiguration()
   {
      Configuration configuration = null;
      try
      {
         Resources system = Resources.getSystem();
         configuration = system.getConfiguration();
      }
      catch (NullPointerException e)
      {
         // damn, for mocking
      }

      return configuration;
   }

   public boolean isHoneycombOrNewer()
   {
      return android.os.Build.VERSION.SDK_INT >= 11;
   }

   public boolean isMobile()
   {
      return !isTablet();
   }

}
