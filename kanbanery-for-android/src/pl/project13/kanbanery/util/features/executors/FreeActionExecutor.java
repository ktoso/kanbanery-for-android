package pl.project13.kanbanery.util.features.executors;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.ui.activities.dialog.GetPremiumVersionDialog;
import pl.project13.kanbanery.util.Try;

/**
 * @author Konrad Malawski
 */
public class FreeActionExecutor implements ActionExecutor
{

   @Inject
   @Named("onTablet")
   protected boolean onTablet;

   public void onShowMenu(Activity caller)
   {
      Intent intent = new Intent(caller, GetPremiumVersionDialog.class);
      caller.startActivity(intent);
   }

   @Override
   public void onlyForPremium(Context context, Runnable performActionIfPro)
   {
      Intent intent = Intents.ShowGetPremiumVersionDialog.create(context);
      context.startActivity(intent);
   }

   @Override
   public <T> T onlyForPremium(Context context, Class<T> clazz, Try.Get get)
   {
      Intent intent = Intents.ShowGetPremiumVersionDialog.create(context);
      context.startActivity(intent);
      return null;
   }
}
