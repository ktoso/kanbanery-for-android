package pl.project13.kanbanery.ui.activities;

import android.os.Bundle;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import pl.project13.kanbanery.R;
import roboguice.activity.RoboPreferenceActivity;

public class SettingsActivity extends RoboPreferenceActivity
{

   @Inject
   @Named("premium")
   Boolean isPremium;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      if (isPremium)
      {
         addPreferencesFromResource(R.xml.pro_general_preferences);
      }
      else
      {
         addPreferencesFromResource(R.xml.not_pro_general_preferences);
      }

   }
}