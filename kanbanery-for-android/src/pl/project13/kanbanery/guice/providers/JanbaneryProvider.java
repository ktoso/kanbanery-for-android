package pl.project13.kanbanery.guice.providers;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import pl.project13.janbanery.config.DefaultConfiguration;
import pl.project13.janbanery.core.Janbanery;
import pl.project13.janbanery.core.JanbaneryFactory;
import pl.project13.janbanery.core.rest.RestClient;
import pl.project13.janbanery.exceptions.kanbanery.UnauthorizedKanbaneryException;
import pl.project13.janbanery.resources.Workspace;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.service.InternetStatusManager;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import roboguice.inject.InjectResource;

import java.util.List;

/**
 * @author Konrad Malawski
 */
@Singleton
public class JanbaneryProvider implements Provider<Janbanery>
{

   public static final String TAG = JanbaneryProvider.class.getSimpleName();

   @InjectResource(R.string.preference_key_api_key)
   private String pkApiKey;

   @InjectResource(R.string.preference_key_current_workspace)
   private String pkWorkspace;

   @Inject
   Application application;
   
   @Inject
   RestClient restClient;

   @Inject
   SharedPreferences preferences;

   @Inject
   InternetStatusManager internetStatusManager;

   @Inject
   ActivityLauncher activityLauncher;

   private Janbanery instance;


   public JanbaneryProvider()
   {
      Log.i(TAG, "Instantiated Janbanery provider...");
   }

   @Override
   public Janbanery get()
   {
      if (preferences.getString(pkApiKey, null) == null)
      {
         Log.i(TAG, "ApiKey is not set. Not building janbanery instance.");
         return null;
      }
      else if (instance == null)
      {
         instance = connectJanbanery();
      }

      Log.i(TAG, "Returning Janbanery configured with: " + instance.getAuthMode());
      return instance;
   }

   private Janbanery connectJanbanery()
   {
      String apiKey = preferences.getString(pkApiKey, null);
      String workspace = preferences.getString(pkWorkspace, null);

      if (apiKey == null)
      {
         // not yet ready to provide instance
         if (instance != null)
         {
            instance.close();
         }

         instance = null;
         return instance;
      }

      DefaultConfiguration configuration = new DefaultConfiguration(apiKey);

      try
      {
         JanbaneryFactory.JanbaneryToWorkspace factory;
         factory = new JanbaneryFactory(restClient).connectUsing(configuration);

         instance = factory.notDeclaringWorkspaceYet();

         if (workspace == null)
         {
//            Workspace firstWorkspace = getFirstWorkspace(instance);
//            workspace = firstWorkspace.getName();
//            workspace = "";
         }
         else
         {
            instance = instance.usingWorkspace(workspace);
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create Janbanery instance", e);
      }

      Log.d(TAG, String.format("Providing instance %s, for key: %s", instance, instance.getAuthMode().getAuthHeader()));

      return instance;
   }

   private Workspace getFirstWorkspace(final Janbanery finalInstance)
   {
      try
      {
         Workspace workspace = new AsyncTask<Void, Void, Workspace>()
         {
            @Override
            protected Workspace doInBackground(Void... voids)
            {
               List<Workspace> allWorkspaces = finalInstance.workspaces().all();
               Workspace workspace = allWorkspaces.get(0);
               return workspace;
            }
         }.get();

         return workspace;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to get default workspace.", e);
      }
   }

   public String obtainApiKeyFor(final String username, final String password)
   {
      Janbanery janbanery = new JanbaneryFactory(restClient).connectUsing(username, password)
                                                            .notDeclaringWorkspaceYet();
      String apiKey = janbanery.users().current().getApiToken();

      if (apiKey == null)
      {
         throw new UnauthorizedKanbaneryException("Unable to obtain Kanbanery API key " +
                                                        "for given credentials.");
      }
      janbanery.close();

      return apiKey;
   }

   /**
    * Sign in into a specific user account, get it's key and switch the provided
    * Janbanery instance to work using this configuration.
    *
    * @param username
    * @param password
    * @throws UnauthorizedKanbaneryException
    */
   public void signIn(String username, String password) throws UnauthorizedKanbaneryException
   {
      String apiKey = obtainApiKeyFor(username, password);

      persistApiKey(apiKey);

      DefaultConfiguration conf = new DefaultConfiguration(apiKey);
      get().setConf(conf);
   }

   private void persistApiKey(String apiKey)
   {
      SharedPreferences.Editor editor = preferences.edit();

      editor.putString(pkApiKey, apiKey);
      Log.d(TAG, "Persisting APIKey: " + apiKey);
      editor.commit();
   }
}