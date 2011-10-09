package pl.project13.kanbanery.util.graphics;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * Created by ags on 8/20/11 at 4:09 PM
 */
public class BitmapUtils
{
   private BitmapUtils()
   {
      // hidden
   }

   public static Bitmap resizeBitmap(Bitmap bitmap, int targetHeight, int targetWidth)
   {
      int width = bitmap.getWidth();
      int height = bitmap.getHeight();

      float scaleWidth = ((float) targetWidth) / width;
      float scaleHeight = ((float) targetHeight) / height;

      // create a matrix for the manipulation
      Matrix matrix = new Matrix();

      // resize the bit map
      matrix.postScale(scaleWidth, scaleHeight);

      // recreate the new Bitmap
      Bitmap resized = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false);
      return resized;
   }
}
