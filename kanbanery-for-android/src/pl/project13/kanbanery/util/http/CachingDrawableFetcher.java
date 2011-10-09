package pl.project13.kanbanery.util.http;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import pl.project13.kanbanery.util.cache.DrawablesCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

/**
 * @author Konrad Malawski
 */
@Singleton
public class CachingDrawableFetcher
{

   private static final String TAG = CachingDrawableFetcher.class.getSimpleName();

   @Inject
   private Application app;

   @Inject
   DrawablesCache cache;

   public Drawable fetchDrawable(String urlString)
   {
      if (cache.isCacheHit(urlString))
      {
         return cache.get(urlString);
      }

      Log.d(TAG, "image url: " + urlString);

      Drawable drawable = null;
      try
      {
         InputStream is = fetch(urlString);
         drawable = Drawable.createFromStream(is, "src");
         drawable.setBounds(0, 0, 35, 35);

         cache.cache(urlString, drawable);

         Log.d(TAG, "got a thumbnail drawable: " + drawable.getBounds() + ", "
               + drawable.getIntrinsicHeight() + "," + drawable.getIntrinsicWidth() + ", "
               + drawable.getMinimumHeight() + "," + drawable.getMinimumWidth());
      }
      catch (MalformedURLException e)
      {
         Log.e(TAG, "fetchDrawable failed", e);
      }
      catch (IOException e)
      {
         Log.e(TAG, "fetchDrawable failed", e);
      }

      return drawable;
   }

   private InputStream fetch(String urlString) throws IOException
   {
      DefaultHttpClient httpClient = new DefaultHttpClient();
      HttpGet request = new HttpGet(urlString);
      HttpResponse response = httpClient.execute(request);
      return response.getEntity().getContent();
   }

}
