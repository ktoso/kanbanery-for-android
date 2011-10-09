package pl.project13.kanbanery.ui.common.handlers;

import android.app.Activity;
import android.view.View;

public class FinishActivityOnClick implements View.OnClickListener
{
   private Activity activity;

   public FinishActivityOnClick(Activity activity)
   {
      this.activity = activity;
   }

   @Override
   public void onClick(View view)
   {
      activity.finish();
   }
}
