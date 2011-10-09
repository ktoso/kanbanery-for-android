/*
 * This file is part of kanbanery-android.
 *
 * kanbanery-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * kanbanery-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

package pl.project13.kanbanery.guice.app;

import android.content.pm.PackageManager;
import android.util.Log;
import com.google.inject.Module;
import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.guice.FreeKanbaneryModule;
import pl.project13.kanbanery.guice.ProKanbaneryModule;
import pl.project13.kanbanery.util.hash.Base64;
import pl.project13.kanbanery.util.hash.Base64DecoderException;
import roboguice.application.RoboApplication;

import java.util.List;

/**
 * The "Application" class used to instanciate RoboGuice
 *
 * @author Konrad Malawski
 */
@ReportsCrashes(formKey = "dDNreWprcENYbmJPck44ODN4UG4wR1E6MQ",
                mode = ReportingInteractionMode.NOTIFICATION,
                resToastText = R.string.crash_toast_text, // optional, displayed as soon as the crash occurs, before collecting data which can take a few seconds
                resNotifTickerText = R.string.crash_notif_ticker_text,
                resNotifTitle = R.string.crash_notif_title,
                resNotifText = R.string.crash_notif_text,
                resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
                resDialogText = R.string.crash_dialog_text,
                resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
                resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
                resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
                resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class KanbaneryApplication extends RoboApplication
{

   private static final String TAG = KanbaneryApplication.class.getSimpleName();

   public static boolean skipACRAInitialization = false;

   public static void skipACRAInitialization()
   {
      skipACRAInitialization = true;
   }

   @Override
   public void onCreate()
   {
      if (skipACRAInitialization)
      {
         return;
      }

      ACRA.init(this);
      super.onCreate();
   }

   @Override
   protected void addApplicationModules(List<Module> modules)
   {

      if (bindProVersion())
      {
         modules.add(new ProKanbaneryModule(this));
      }
      else
      {
         modules.add(new FreeKanbaneryModule(this));
      }
   }

   private boolean bindProVersion()
   {
      String unlockAppNameBase64 = getString(R.string.kanbanery_unlock_app_name_base64);

      boolean unlockerInstalled = isUnlockerInstalled(unlockAppNameBase64);
      Log.i(TAG, "Running as " + (unlockerInstalled ? "PREMIUM" : "FREE") + " version");

      return unlockerInstalled;
   }

   private boolean isUnlockerInstalled(String unlockAppNameBase64)
   {
      try
      {
         byte[] realUnlockerNameBytes = Base64.decode(unlockAppNameBase64);
         String realUnlockerName = new String(realUnlockerNameBytes);

         PackageManager packageManager = getPackageManager();
         packageManager.getApplicationInfo(realUnlockerName, PackageManager.GET_ACTIVITIES);
         return true;
      }
      catch (Base64DecoderException e)
      {
         return false;
      }
      catch (PackageManager.NameNotFoundException e)
      {
         return false;
      }
   }
}
