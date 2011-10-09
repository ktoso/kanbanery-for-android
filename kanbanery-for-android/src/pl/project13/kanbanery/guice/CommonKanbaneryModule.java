package pl.project13.kanbanery.guice;

import android.app.Service;
import android.util.Log;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.core.rest.RestClient;
import pl.project13.janbanery.resources.Estimate;
import pl.project13.janbanery.resources.Task;
import pl.project13.janbanery.resources.TaskType;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.guice.app.KanbaneryApplication;
import pl.project13.kanbanery.guice.helpers.TabletDetector;
import pl.project13.kanbanery.guice.providers.FetchAndSetUserIconProvider;
import pl.project13.kanbanery.guice.providers.JanbaneryProvider;
import pl.project13.kanbanery.guice.providers.RestClientProvider;
import pl.project13.kanbanery.guice.providers.ServicesToStartOnBootProvider;
import pl.project13.kanbanery.util.cache.Cache;
import pl.project13.kanbanery.util.cache.DrawablesCache;
import pl.project13.kanbanery.util.cache.SimpleCache;
import pl.project13.kanbanery.util.http.CachingDrawableFetcher;
import pl.project13.kanbanery.util.http.FetchAndSetUserIcon;
import roboguice.config.AbstractAndroidModule;
import roboguice.inject.SharedPreferencesName;

import java.util.List;

import static com.google.inject.name.Names.named;

/**
 * @author Konrad Malawski
 */
public class CommonKanbaneryModule extends AbstractAndroidModule
{

   private static final String TAG = CommonKanbaneryModule.class.getSimpleName();

   private KanbaneryApplication app;

   public CommonKanbaneryModule(KanbaneryApplication kanbaneryApplication)
   {
      app = kanbaneryApplication;
   }

   @Override
   protected void configure()
   {
      Log.i(TAG, "Configuring Guice module using " + currentStage());

      // BUG need a better way to set default preferences context
      bindConstant().annotatedWith(SharedPreferencesName.class).to("pl.project13.kanbanery");
      bindConstant().annotatedWith(Names.named("premiumAddUrl")).to("http://kanbanery.for.android.project13.pl/android/me_want_premium");

      // core stuff
      bind(RestClient.class).toProvider(RestClientProvider.class).in(Singleton.class);
      bind(Janbanery.class).toProvider(JanbaneryProvider.class);//.in(Singleton.class);

      // help deciding if we're running on a tablet
      boolean runningOnTablet = isRunningOnTablet();
      logRunningOn(runningOnTablet);
      bindConstant().annotatedWith(named("onTablet")).to(runningOnTablet);

      // services to start on boot
      bind(new TypeLiteral<List<Class<? extends Service>>>(){})
            .annotatedWith(Names.named("startOnBoot"))
            .toProvider(new ServicesToStartOnBootProvider());
      
      // util
      bind(DrawablesCache.class).asEagerSingleton();
      bind(CachingDrawableFetcher.class).asEagerSingleton();
      bind(FetchAndSetUserIcon.class).toProvider(FetchAndSetUserIconProvider.class).asEagerSingleton();

      // cache
      bind(new TypeLiteral<Cache<TaskType>>(){}).toInstance(SimpleCache.<TaskType>newSimpleCache());
      bind(new TypeLiteral<Cache<Task>>(){}).toInstance(SimpleCache.<Task>newSimpleCache());
      bind(new TypeLiteral<Cache<User>>(){}).toInstance(SimpleCache.<User>newSimpleCache());
      bind(new TypeLiteral<Cache<Estimate>>(){}).toInstance(SimpleCache.<Estimate>newSimpleCache());
   }

   private void logRunningOn(boolean runningOnTablet)
   {
      Log.i(TAG, "Kanbanery is currently running in " + (runningOnTablet ? "TABLET" : "MOBILE") + " mode.");
   }

   private boolean isRunningOnTablet()
   {
      TabletDetector tabletDetector = new TabletDetector();

      return tabletDetector.isTablet();
   }

}
