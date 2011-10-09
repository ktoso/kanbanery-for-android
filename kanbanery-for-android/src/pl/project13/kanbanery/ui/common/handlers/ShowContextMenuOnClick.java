package pl.project13.kanbanery.ui.common.handlers;

import android.view.View;

/**
 * @author Konrad Malawski
 */
public class ShowContextMenuOnClick implements View.OnClickListener
{
   @Override
   public void onClick(View view)
   {
      view.showContextMenu();
   }
}
