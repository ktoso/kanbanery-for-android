package pl.project13.kanbanery.guice.providers;

import android.app.Application;
import android.os.Handler;
import com.google.inject.Inject;
import com.google.inject.Provider;
import pl.project13.kanbanery.util.http.CachingDrawableFetcher;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;

/**
 * Created by ags on 8/20/11 at 3:34 PM
 */
public class FetchAndSetUserIconProvider implements Provider<FetchAndSetUserIcon>
{
   @Inject
   CachingDrawableFetcher cachingDrawableFetcher;

   @Inject
   Application app;

   @Override
   public FetchAndSetUserIcon get()
   {
      return new FetchAndSetUserIcon(app, cachingDrawableFetcher, new Handler());
   }
}
