package pl.project13.kanbanery.guice.providers;

import android.app.Service;
import com.google.inject.Provider;
import pl.project13.kanbanery.service.KanbaneryActionsNotifierService;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @author Konrad Malawski
 */
public class ServicesToStartOnBootProvider implements Provider<List<Class<? extends Service>>>
{

   List<Class<? extends Service>> classes = newArrayList();

   public ServicesToStartOnBootProvider()
   {
      classes.add(KanbaneryActionsNotifierService.class);
   }

   @Override
   public List<Class<? extends Service>> get()
   {
      return classes;
   }
}
