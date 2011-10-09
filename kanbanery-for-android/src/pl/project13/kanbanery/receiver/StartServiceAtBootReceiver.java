package pl.project13.kanbanery.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import roboguice.service.RoboService;

import java.util.List;

/**
 * Will start notification services etc.
 *
 * @author Konrad Malawski
 */
public class StartServiceAtBootReceiver extends BroadcastReceiver
{

   public static final String TAG = StartServiceAtBootReceiver.class.getSimpleName();

   @Inject
   @Named("startOnBoot")
   List<Class<? extends RoboService>> servicesToStartOnBoot;

   @Override
   public void onReceive(Context context, Intent intent)
   {
      Log.i(TAG, "Starting boot services...");

      if (servicesToStartOnBoot == null)
      {
         Log.d(TAG, "No services to start on boot found...");
         return;
      }

      for (Class serviceClass : servicesToStartOnBoot)
      {
         startService(context, serviceClass);
      }
   }

   private void startService(Context context, Class<?> serviceClass)
   {
      Log.i(TAG, "Starting '" + serviceClass.getSimpleName() + "' using context: " + context);

      Intent startServiceIntent = new Intent(context, serviceClass);
      context.startService(startServiceIntent);
   }

}
