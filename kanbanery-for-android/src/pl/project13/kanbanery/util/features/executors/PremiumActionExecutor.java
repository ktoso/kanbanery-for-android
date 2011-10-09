package pl.project13.kanbanery.util.features.executors;

import android.app.Activity;
import android.content.Context;
import com.google.inject.Singleton;
import pl.project13.kanbanery.util.Try;

/**
 * @author Konrad Malawski
 */
@Singleton
public class PremiumActionExecutor extends FreeActionExecutor implements ActionExecutor
{

   @Override
   public void onShowMenu(Activity caller)
   {
      throw new RuntimeException("Not implemented");
   }

   @Override
   public void onlyForPremium(Context context, Runnable performActionIfPro)
   {
      performActionIfPro.run();
   }

   @Override
   public <T> T onlyForPremium(Context context, Class<T> clazz, Try.Get get)
   {
      Object got = get.get();

      return clazz.cast(got);
   }
}
