package pl.project13.kanbanery.util.http;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import com.google.inject.Singleton;
import pl.project13.janbanery.resources.User;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.util.graphics.BitmapUtils;

import java.util.concurrent.ExecutionException;

/**
 * Created by ktoso on 8/20/11 at 12:20 PM
 */
@Singleton
public class FetchAndSetUserIcon
{
   private static final String TAG = FetchAndSetUserIcon.class.getSimpleName();

   private static final String NO_USER_IMAGE_URL = "http://janbanery.kanbanery.com/images/no-user.png";

   private Application app;
   private CachingDrawableFetcher resourceFetcher;
   private Handler handler;

   public FetchAndSetUserIcon(Application app, CachingDrawableFetcher resourceFetcher, Handler handler)
   {
      this.app = app;
      this.resourceFetcher = resourceFetcher;
      this.handler = handler;
   }

   public void apply(User user, ImageView targetView)
   {
      apply(user.getGravatarUrl(), targetView);
   }

   private void apply(final String gravatarUrl, final ImageView targetView)
   {
      Drawable drawable = getUserDrawable(gravatarUrl);

      if (drawable != null)
      {
         BitmapDrawable resizedOwnerGravatar = resizeDrawable((BitmapDrawable) drawable);

         setUserIcon(resizedOwnerGravatar, targetView);
      }
   }

   private BitmapDrawable resizeDrawable(BitmapDrawable drawable)
   {
      Bitmap bitmap = drawable.getBitmap();
      Bitmap resizedBitmap = BitmapUtils.resizeBitmap(bitmap, 48, 48);
      return new BitmapDrawable(resizedBitmap);
   }

   private Drawable getUserDrawable(final String gravatarUrl)
   {
      Drawable drawable = null;

      // no need to fetch the no user image - we have it already
      if (gravatarUrl.equals(NO_USER_IMAGE_URL))
      {
         drawable = getDrawable(R.drawable.ic_contact_picture_2);
      }
      else if (resourceFetcher.cache.isCacheHit(gravatarUrl))
      {
         drawable = resourceFetcher.cache.get(gravatarUrl);
      }
      else
      {
         try
         {
            //noinspection unchecked
            drawable = new AsyncTask<Void, Void, Drawable>()
            {
               @Override
               protected Drawable doInBackground(Void... voids)
               {
                  return resourceFetcher.fetchDrawable(gravatarUrl);
               }
            }.execute().get();
         }
         catch (InterruptedException e)
         {
            Log.e(TAG, "Unable to fetch gravatar user image.", e);
         }
         catch (ExecutionException e)
         {
            Log.e(TAG, "Unable to fetch gravatar user image.", e);
         }
      }
      return drawable;
   }


   private Drawable getDrawable(int ic_no_user)
   {
      return app.getResources().getDrawable(ic_no_user);
   }

   private void setUserIcon(final Drawable ownerGravatar, final ImageView targetView)
   {
      handler.post(new Runnable()
      {
         @Override
         public void run()
         {
            targetView.setImageDrawable(ownerGravatar);
         }
      });
   }

}
