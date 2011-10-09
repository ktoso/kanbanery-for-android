package pl.project13.kanbanery.service;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.google.inject.Inject;

/**
 * @author Konrad Malawski
 */
public class InternetStatusManager
{

   public static final String NETWORK_NAME_MOBILE = "MOBILE";
   public static final String NETWORK_NAME_WIFI = "WIFI";

   private ConnectivityManager connectivityManager;

   @Inject
   public InternetStatusManager(ConnectivityManager connectivityManager)
   {
      this.connectivityManager = connectivityManager;
   }

   public boolean isOnWifi()
   {
      return isConnectedTo(NETWORK_NAME_WIFI);
   }

   public boolean isOnMobileNetwork()
   {
      return isConnectedTo(NETWORK_NAME_MOBILE);
   }

   private boolean isConnectedTo(String networkName)
   {
      NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
      boolean connectedOrConnecting = activeNetworkInfo.isConnectedOrConnecting();

      return connectedOrConnecting && networkName.equals(activeNetworkInfo.getTypeName());
   }

   public boolean isOnline()
   {
      boolean weAreOnline;
      try
      {
         NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
         weAreOnline = activeNetworkInfo.isConnectedOrConnecting();
      }
      catch (Exception e)
      {
         return false;
      }

      return weAreOnline;
   }

   public boolean isOffline()
   {
      return !isOnline();
   }
}
