package pl.project13.kanbanery.ui.activities.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.google.common.io.PatternFilenameFilter;
import pl.project13.kanbanery.annotation.DialogActivity;

import java.io.File;

import static pl.project13.kanbanery.util.StringUtils.lines;

@DialogActivity
public class CleanCacheDialog extends Activity
{

   private static final String TAG = CleanCacheDialog.class.getSimpleName();

   private Handler handler;

   @Override
   protected void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      this.setVisible(false);

      handler = new Handler();

      //noinspection unchecked
      new AsyncTask<Void, Integer, CleanupSummary>()
      {

         private ProgressDialog dialog;

         private UpdateDialogProgress pingMeOnFileDeleted;

         @Override
         protected void onPreExecute()
         {
            dialog = new ProgressDialog(CleanCacheDialog.this);
            dialog.setTitle("Cleaning image cache...");
            dialog.setMessage("Please wait, this might take a few seconds...");
            dialog.setIndeterminate(false);
            dialog.show();

            pingMeOnFileDeleted = new UpdateDialogProgress(dialog);
         }

         @Override
         protected CleanupSummary doInBackground(Void... voids)
         {
            File cacheDir = getFilesDir();
            File[] cachedImages = cacheDir.listFiles(new PatternFilenameFilter(".*\\.jpg"));

            dialog.setMax(cachedImages.length);

            CleanupSummary summary = deleteAll(cachedImages, pingMeOnFileDeleted);
            return summary;
         }

         @Override
         protected void onPostExecute(final CleanupSummary summary)
         {
            handler.post(new Runnable()
            {
               @Override
               public void run()
               {
                  dialog.dismiss();

                  String title = "Image cache cleanup successful!";
                  CharSequence message = lines(summary.removedFiles + " files where removed, ",
                                               "and a total of " + humanReadableByteCount(summary.reclaimedBytes, true) + " was reclaimed.");

                  new AlertDialog.Builder(CleanCacheDialog.this)
                        .setTitle(title)
                        .setMessage(message)
                        .setCancelable(false)
                        .setPositiveButton("Cool, thanks!", new CloseActivityOnClickListener())
                        .show();
               }
            });
         }

      }.execute();
   }

   private CleanupSummary deleteAll(File[] files, UpdateDialogProgress pingMeOnFileDeleted)
   {
      int reclaimedBytes = 0;

      for (File file : files)
      {
         Log.i(TAG, "Deleting cache file: '" + file + "'");
         long fileLength = file.length();
         boolean success = file.delete();

         pingMeOnFileDeleted.updateProgress();

         if (success)
         {
            reclaimedBytes += fileLength;
         }
         else
         {
            Log.e(TAG, "Unable to delete file: '" + file + "'");
         }
      }

      return new CleanupSummary(files.length, reclaimedBytes);
   }

   private class CleanupSummary
   {
      int reclaimedBytes;
      int removedFiles;

      CleanupSummary(int removedFiles, int reclaimedBytes)
      {
         this.reclaimedBytes = reclaimedBytes;
         this.removedFiles = removedFiles;
      }
   }

   class UpdateDialogProgress
   {
      private ProgressDialog dialog;

      int currentlyAt = 0;

      public UpdateDialogProgress(ProgressDialog dialog)
      {
         this.dialog = dialog;
      }

      void updateProgress()
      {
         handler.post(new Runnable()
         {
            @Override
            public void run()
            {
               dialog.setProgress(currentlyAt++);
            }
         });
      }
   }

   public static String humanReadableByteCount(long bytes, boolean si)
   {
      int unit = si ? 1000 : 1024;
      if (bytes < unit) return bytes + " B";
      int exp = (int) (Math.log(bytes) / Math.log(unit));
      String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
      return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
   }


   private class CloseActivityOnClickListener implements DialogInterface.OnClickListener
   {
      @Override
      public void onClick(DialogInterface dialogInterface, int i)
      {
         CleanCacheDialog.this.finish();
      }
   }
}
