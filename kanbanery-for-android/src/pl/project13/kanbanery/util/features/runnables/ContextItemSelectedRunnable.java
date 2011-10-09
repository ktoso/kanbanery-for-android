package pl.project13.kanbanery.util.features.runnables;

import android.app.Activity;
import android.view.MenuItem;

public abstract class ContextItemSelectedRunnable implements Runnable
{

   private Activity caller;
   private MenuItem menuItem;

   public ContextItemSelectedRunnable(Activity caller, MenuItem menuItem)
   {
      this.caller = caller;
      this.menuItem = menuItem;
   }
}