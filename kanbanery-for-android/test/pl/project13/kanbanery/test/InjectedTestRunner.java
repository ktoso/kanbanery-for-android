package pl.project13.kanbanery.test;

import com.google.inject.Injector;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import org.junit.runners.model.InitializationError;
import pl.project13.kanbanery.guice.app.KanbaneryApplication;
import roboguice.application.RoboApplication;

public class InjectedTestRunner extends RobolectricTestRunner
{
   public InjectedTestRunner(Class<?> testClass) throws InitializationError
   {
      super(testClass);

      KanbaneryApplication.skipACRAInitialization();
   }

   @Override
   public void prepareTest(Object test)
   {
      RoboApplication sampleApplication = (RoboApplication) Robolectric.application;
      Injector injector = sampleApplication.getInjector();
      injector.injectMembers(test);
   }
}
