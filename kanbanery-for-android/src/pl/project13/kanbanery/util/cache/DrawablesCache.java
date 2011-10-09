package pl.project13.kanbanery.util.cache;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.common.io.Closeables;
import com.google.common.io.Flushables;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import pl.project13.kanbanery.util.hash.AeSimpleMD5;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import static pl.project13.kanbanery.util.StringUtils.lines;

@Singleton
public class DrawablesCache implements Cache<Drawable>
{
   private static final String TAG = DrawablesCache.class.getSimpleName();

   public static final String CACHE_FILE_SUFFIX = ".jpg";

   @Inject
   Application app;

   @Override
   public boolean isCacheHit(String checkIfCached)
   {
      String filename = filename(checkIfCached);

      FileInputStream is;
      try
      {
         is = app.openFileInput(filename);
      }
      catch (FileNotFoundException e)
      {
         return false;
      }

      Closeables.closeQuietly(is);

      return true;
   }

   @Override
   public boolean isCacheHit(Object checkIfCachedByToString)
   {
      return isCacheHit(checkIfCachedByToString.toString());
   }

   @Override
   public Drawable get(Object getMeByToString)
   {
      return get(getMeByToString.toString());
   }

   @Override
   public Drawable get(String getMe)
   {
      String filename = filename(getMe);

      try
      {
         FileInputStream is = app.openFileInput(filename);
         Drawable drawable = BitmapDrawable.createFromStream(is, filename);

         Closeables.closeQuietly(is);

         return drawable;
      }
      catch (FileNotFoundException e)
      {
         Log.e(TAG, "Unable to to get resource", e);
         return null;
      }
      catch (Exception e)
      {
         Log.e(TAG, lines("Something terrible happened.",
                          "Unable to get resource from cache: " + filename).toString(), e);
         return null;
      }
   }

   @Override
   public void cache(String name, Drawable cacheMe)
   {
      String filename = filename(name);

      try
      {
         FileOutputStream os = app.openFileOutput(filename, Context.MODE_PRIVATE);

         Bitmap bitmap = ((BitmapDrawable) cacheMe).getBitmap();
         bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

         Flushables.flushQuietly(os);
         Closeables.closeQuietly(os);

         Log.d(TAG, "Successfully compressed and saved drawable '" + name + "', as '" + filename + "'");
      }
      catch (IOException e)
      {
         Log.e(TAG, "Unable to save drawable as bitmap to file: " + filename, e);
      }

   }

   private String filename(String getMe)
   {
      return md5Hash(getMe) + CACHE_FILE_SUFFIX;
   }

   private String md5Hash(String checkIfCached)
   {
      String keyHash;
      try
      {
         keyHash = AeSimpleMD5.md5(checkIfCached);
      }
      catch (NoSuchAlgorithmException e)
      {
         return "no_hash";
      }
      catch (UnsupportedEncodingException e)
      {
         Log.e(TAG, "Unable to calculate MD5 hash for: " + checkIfCached);
         return "no_hash";
      }
      return keyHash;
   }
}
