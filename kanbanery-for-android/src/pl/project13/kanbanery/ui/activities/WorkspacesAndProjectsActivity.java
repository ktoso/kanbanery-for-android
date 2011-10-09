package pl.project13.kanbanery.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.SimpleExpandableListAdapter;
import com.google.inject.Inject;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.resources.Project;
import pl.project13.janbanery.resources.Workspace;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.guice.providers.JanbaneryProvider;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.Try;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static pl.project13.kanbanery.util.StringUtils.lines;

/**
 * @author Konrad Malawski
 */
public class WorkspacesAndProjectsActivity extends RoboActivity
{

   private static final String TAG = WorkspacesAndProjectsActivity.class.getSimpleName();

   @InjectView(R.id.workspaces)
   private ExpandableListView vWorkspaces;

   @InjectResource(R.string.preference_key_api_key)
   private String pkApiKey;
   @InjectResource(R.string.preference_key_current_workspace)
   private String pkWorkspace;
   @InjectResource(R.string.preference_key_current_project)
   private String pkCurrentProject;

   @Inject
   private SharedPreferences preferences;

   @Inject
   private ActivityLauncher activityLauncher;

   @Inject
   LayoutInflater layoutInflater;

   @Inject
   JanbaneryProvider janbaneryProvider;

   //   @Inject
   private Janbanery janbanery;

   private Handler handler = new Handler();

   private List<List<Map<String, Object>>> cachedProjectsInWorkspaces = null;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.workspaces_and_projects);

      forgetCurrentProject();

      janbanery = janbaneryProvider.get();

      //noinspection unchecked
      new AsyncTask<Void, Void, List<Workspace>>()
      {
         ProgressDialog dialog;

         @Override
         protected void onPreExecute()
         {
            dialog = ProgressDialog.show(WorkspacesAndProjectsActivity.this,
                                         "Loading workspaces...",
                                         lines("Please wait..."),
                                         true);
         }

         @Override
         protected List<Workspace> doInBackground(Void... voids)
         {
            try
            {
               List<Workspace> allWorkspaces = Try.times(2).to(getAllWorkspaces());
               return allWorkspaces;
            }
            catch (Exception ex)
            {
               WorkspacesAndProjectsActivity self = WorkspacesAndProjectsActivity.this;

               Intent intent = Intents.ShowWorkspacesAndProjects.create(self);
               startActivity(intent);
               self.finish();
               return null;
            }
         }

         @Override
         protected void onPostExecute(List<Workspace> workspaces)
         {
            if (workspaces != null)
            {
               handler.post(new PopulateWorkspacesList(workspaces));
            }

            dialog.hide();
         }
      }.execute();
   }

   private Try.Get<List<Workspace>> getAllWorkspaces()
   {
      return new Try.Get<List<Workspace>>()
      {
         @Override
         public List<Workspace> get()
         {
            janbanery = janbaneryProvider.get();

            return janbanery.workspaces().all();
         }
      };
   }

   private void forgetCurrentProject()
   {
      SharedPreferences.Editor editor = preferences.edit();
      editor.remove(getString(R.string.preference_key_current_project));
      editor.commit();
   }

   private class PopulateWorkspacesList implements Runnable, ExpandableListView.OnChildClickListener
   {
      private List<Workspace> workspaces;

      public PopulateWorkspacesList(List<Workspace> workspaces)
      {
         this.workspaces = workspaces;
      }

      @Override
      public void run()
      {
         SimpleExpandableListAdapter adapter;
         adapter = new SimpleExpandableListAdapter(WorkspacesAndProjectsActivity.this,

                                                   workspaces(workspaces),
                                                   R.layout.workspaces_workspace,
                                                   new String[]{"name"},
                                                   new int[]{R.id.workspace_name},

                                                   projectsInWorkspace(workspaces),
                                                   R.layout.workspaces_project,
                                                   new String[]{"name"},
                                                   new int[]{R.id.project_name}
         );
         vWorkspaces.setAdapter(adapter);
         expandAllGroups(vWorkspaces, workspaces);
         setNoWorkspacesView(vWorkspaces);
         vWorkspaces.setOnChildClickListener(this);
      }

      private void setNoWorkspacesView(ExpandableListView vWorkspaces)
      {
         View emptyView = layoutInflater.inflate(R.layout.empty_workspaces_create_one, null);
         vWorkspaces.setEmptyView(emptyView);
      }

      private void expandAllGroups(ExpandableListView vWorkspaces1, List<Workspace> workspaces)
      {
         for (int i = 0; i < workspaces.size(); i++)
         {
            vWorkspaces1.expandGroup(i);
         }
      }

      private List<? extends Map<String, Object>> workspaces(List<Workspace> workspaces)
      {
         List<Map<String, Object>> result = newArrayList();

         for (Workspace workspace : workspaces)
         {
            Map<String, Object> m = newHashMap();
            String workspaceName = workspace.getName();

            m.put("name", workspaceName);
            Log.i(TAG, "Mapping workspace name: " + workspaceName);

            result.add(m);
         }

         return result;
      }

      private List<? extends List<? extends Map<String, Object>>> projectsInWorkspace(List<Workspace> workspaces)
      {
         List<List<Map<String, Object>>> result = newArrayList();

         for (Workspace workspace : workspaces)
         {
            List<Map<String, Object>> secList = newArrayList();

            List<Project> projects = workspace.getProjects();
            for (Project project : projects)
            {
               Map<String, Object> projectData = newHashMap();
               String projectName = project.getName();

               projectData.put("name", projectName);
               Log.i(TAG, "Mapping project name: " + projectName);

               secList.add(projectData);
            }
            result.add(secList);
         }

         cachedProjectsInWorkspaces = result;

         return result;
      }

      @Override
      public boolean onChildClick(ExpandableListView expandableListView, View view, int groupId, int childId, long l)
      {
         final Workspace workspace = workspaces.get(groupId);
         final Project project = workspace.getProjects().get(childId);

         try
         {
            //noinspection unchecked
            new AsyncTask<Void, Void, Void>()
            {

               @Override
               protected Void doInBackground(Void... voids)
               {
                  String projectName = project.getName();

                  janbanery.usingProject(workspace, projectName);

                  // persist the current workspace and project name
                  persistCurrentProject(workspace.getName(), projectName);

                  return null;
               }

               @Override
               protected void onPostExecute(Void nothing)
               {
                  activityLauncher.launchTasksView(WorkspacesAndProjectsActivity.this);
               }
            }.execute().get();
         }
         catch (InterruptedException e)
         {
            e.printStackTrace();
         }
         catch (ExecutionException e)
         {
            e.printStackTrace();
         }

         return true;
      }

      private void persistCurrentWorkspace(String workspaceName)
      {
         SharedPreferences.Editor editor = preferences.edit();
         editor.commit();
      }

      private void persistCurrentProject(String workspaceName, String projectName)
      {
         SharedPreferences.Editor editor = preferences.edit();
         editor.putString(getString(R.string.preference_key_current_workspace), workspaceName);
         editor.putString(getString(R.string.preference_key_current_project), projectName);
         editor.commit();
      }

   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu)
   {
      MenuInflater inflater = getMenuInflater();

      inflater.inflate(R.menu.only_settings_menu, menu);
      return true;
   }

   /**
    * Called on menu clicks etc, also the action bar clicks fall into here
    */
   @Override
   public boolean onOptionsItemSelected(MenuItem item)
   {
      switch (item.getItemId())
      {
         case R.id.open_settings:
            goToSettings();
            return true;
         case R.id.columns_sign_out:
            signOut();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   /**
    * Resets the used APIKey and navigates back to the sign in screen.
    */
   private void signOut()
   {
      // reset api key
      SharedPreferences.Editor editor = preferences.edit();
      editor.remove(getString(R.string.preference_key_api_key));
      editor.remove(getString(R.string.preference_key_current_workspace));
      editor.remove(getString(R.string.preference_key_current_project));
      editor.commit();

      // go back to sign in screen
      activityLauncher.launchSignInView(this);
      finish();
   }

   private void goToSettings()
   {
      activityLauncher.launchSettingsView(this);
   }

}
