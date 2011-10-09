package pl.project13.kanbanery.ui.widgets.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import pl.project13.janbanery.resources.Project;
import pl.project13.kanbanery.R;

import java.util.Collections;
import java.util.List;

/**
 * @author Konrad Malawski
 */
public class SelectCurrentProjectPreference extends Preference
{

   private static final String TAG = SelectCurrentProjectPreference.class.getSimpleName();

   public SelectCurrentProjectPreference(Context context)
   {
      super(context);

      init();
   }

   public SelectCurrentProjectPreference(Context context, AttributeSet attrs)
   {
      super(context, attrs);

      init();
   }

   private void init()
   {
      setPersistent(true);
      setWidgetLayoutResource(R.layout.select_project_preference);
      setEnabled(true);
      setSelectable(true);
   }

   @Override
   protected View onCreateView(ViewGroup parent)
   {
      LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

      LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.select_project_preference, null, false);

      LinearLayout repoListView = (LinearLayout) layout.findViewById(R.id.select_project_layout);

      List<Project> projects = getMyProjects();

      for (Project project : projects)
      {
         View projectView = layoutInflater.inflate(R.layout.select_project_item, null);
         Log.d(TAG, "Adding project '" + project.getName() + "' to preferences view.");

         TextView projectName = (TextView) projectView.findViewById(R.id.project_name);
         projectName.setText(project.getName());

         TextView workspaceName = (TextView) projectView.findViewById(R.id.workspace_name);
         workspaceName.setText("Resource id: " + project.getResourceId());

         CheckBox repoWatchCheckBox = (CheckBox) projectView.findViewById(R.id.watch_this_repository_check_box);
         repoWatchCheckBox.setChecked(false);

         ImageView image = (ImageView) projectView.findViewById(R.id.image);
         image.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ic_kanbanery));

         repoListView.addView(projectView);
      }

      layout.setId(android.R.id.widget_frame);
      return layout;
   }

   private List<Project> getMyProjects()
   {
      return Collections.emptyList();
   }

}
