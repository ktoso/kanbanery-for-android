package pl.project13.kanbanery.util.features.executors;

import android.content.Context;
import pl.project13.kanbanery.util.Try;

/**
 * @author Konrad Malawski
 */
public interface ActionExecutor
{
   void onlyForPremium(Context context, Runnable performActionIfPro);

   <T> T onlyForPremium(Context context, Class<T> clazz, Try.Get get);
}
