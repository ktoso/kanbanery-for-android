package pl.project13.kanbanery.ui.adapters;

import android.content.Context;

public interface SelectionActivity<T>
{
   void onSelected(T selectionMade);

   Context getContext();
}
