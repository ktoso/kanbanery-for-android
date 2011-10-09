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

package pl.project13.kanbanery.guice;

import com.google.inject.name.Names;
import pl.project13.kanbanery.guice.app.KanbaneryApplication;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import pl.project13.kanbanery.util.features.activitylaunchers.PremiumActivityLauncher;
import pl.project13.kanbanery.util.features.executors.ActionExecutor;
import pl.project13.kanbanery.util.features.executors.PremiumActionExecutor;

/**
 * @author Konrad Malawski
 */
public class ProKanbaneryModule extends CommonKanbaneryModule
{

   public ProKanbaneryModule(KanbaneryApplication application)
   {
      super(application);
   }

   @Override
   protected void configure()
   {
      super.configure();

      bindConstant().annotatedWith(Names.named("premium")).to(true);

      bind(ActivityLauncher.class).to(PremiumActivityLauncher.class).asEagerSingleton();
      bind(ActionExecutor.class).to(PremiumActionExecutor.class).asEagerSingleton();
   }

}
