package pl.project13.kanbanery.ui.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import pl.project13.janbanery.exceptions.kanbanery.UnauthorizedKanbaneryException;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.guice.providers.JanbaneryProvider;
import pl.project13.kanbanery.service.InternetStatusManager;
import pl.project13.kanbanery.util.features.activitylaunchers.ActivityLauncher;
import proguard.annotation.Keep;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

import static pl.project13.kanbanery.util.StringUtils.lines;
import static pl.project13.kanbanery.util.StringUtils.yesNo;

/**
 * @author Konrad Malawski
 */
public class SignInActivity extends RoboActivity implements View.OnClickListener
{

   private static final String TAG = SignInActivity.class.getSimpleName();

   Handler handler = new Handler();

   @InjectView(R.id.email_edit_text)
   EditText vEmail;
   @InjectView(R.id.dont_have_an_account_yet_text)
   TextView vDontHaveAnAccountYetText;

   @InjectView(R.id.pass_edit_text)
   EditText vPass;

   @InjectView(R.id.sign_in_btn)
   Button vSignIn;

   @Inject
   SharedPreferences preferences;

   @Inject
   JanbaneryProvider janbaneryProvider;

   @Inject
   ActivityLauncher activityLauncher;

   @Inject
   InternetStatusManager internetStatusManager;

   public void onTestCreate(SharedPreferences preferences, JanbaneryProvider janbaneryProvider)
   {
      super.onCreate(null);

      this.preferences = preferences;
      this.janbaneryProvider = janbaneryProvider;

      init();
   }

   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      init();
   }

   private void init()
   {
      setContentView(R.layout.sign_in);

      boolean wereOffline = internetStatusManager.isOffline();
      if (wereOffline)
      {
         showOfflineDialog();
         return;
      }

      vDontHaveAnAccountYetText.setAutoLinkMask(Linkify.WEB_URLS);

      boolean hasApiKey = hasStoredApiKey(preferences);
      boolean hasSelectedProject = hasSelectedProject(preferences);

      if (hasApiKey && hasSelectedProject)
      {
         goToBoard();
         return;
      }

      if (hasApiKey)
      {
         goToProjects();
         return;
      }

      vSignIn.setOnClickListener(this);
   }

   private void showOfflineDialog()
   {
      AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(SignInActivity.this);
      dialogBuilder.setCancelable(false);
      dialogBuilder.setTitle("You're offline!");
      dialogBuilder.setMessage(lines("Kanbanery for Android needs an internet connection, ",
                                     "in order to talk to the kanbanery.com API.",
                                     "Please connect to the internet and come back! :-)"));
      dialogBuilder.setCancelable(false);
      dialogBuilder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
      {
         @Override
         public void onClick(DialogInterface dialogInterface, int i)
         {
            SignInActivity.this.finish();

         }
      });
      dialogBuilder.create()
                   .show();
   }

   @VisibleForTesting
   boolean hasSelectedProject(SharedPreferences preferences)
   {
      String selectedProjectKey = getString(R.string.preference_key_current_project);
      boolean hasSelectedProject = preferences.contains(selectedProjectKey);
      Log.d(TAG, "Has selected project? " + yesNo(hasSelectedProject));

      return hasSelectedProject;
   }

   @VisibleForTesting
   boolean hasStoredApiKey(SharedPreferences preferences)
   {
      String apiKeyId = getString(R.string.preference_key_api_key);
      boolean hasStoredApiKey = preferences.contains(apiKeyId);
      Log.d(TAG, "Has stored Api Key? " + yesNo(hasStoredApiKey));

      return hasStoredApiKey;
   }

   @VisibleForTesting
   void goToBoard()
   {
      activityLauncher.launchTasksView(SignInActivity.this);
      finish();
   }

   @VisibleForTesting
   void goToProjects()
   {
      activityLauncher.launchWorkspacesAndProjectsView(SignInActivity.this);
      finish();
   }

   /**
    * Is listening for clicks of the "sign in" button
    *
    * @param view the clicked button
    */
   @Override
   public void onClick(View view)
   {


      final String username = vEmail.getText().toString();
      final String password = vPass.getText().toString();


      //noinspection unchecked
      new AsyncTask<Void, Void, UnauthorizedKanbaneryException>()
      {

         private ProgressDialog dialog;

         @Override
         protected void onPreExecute()
         {
            dialog = new ProgressDialog(SignInActivity.this);
            dialog.setTitle("Signing in.");
            dialog.setMessage(lines("Signing in,",
                                    "please wait..."));
            dialog.show();
         }

         @Override
         protected UnauthorizedKanbaneryException doInBackground(Void... voids)
         {
            try
            {
               janbaneryProvider.signIn(username, password);
            }
            catch (UnauthorizedKanbaneryException ex)
            {
               return ex;
            }

            return null;
         }

         @Override
         protected void onPostExecute(UnauthorizedKanbaneryException e)
         {
            dialog.dismiss();

            if (e != null)
            {
               showUnauthorizedMessage(e);
            }
            else
            {
               goToProjects();
            }

         }
      }.execute();


   }

   @VisibleForTesting
   void showUnauthorizedMessage(UnauthorizedKanbaneryException ex)
   {
      Toast.makeText(this, ex.getMessage(), Toast.LENGTH_LONG).show();
   }
}