package pl.project13.kanbanery.util.hash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AeSimpleMD5
{

   private static String convertToHex(byte[] data)
   {
      StringBuilder buf = new StringBuilder();
      for (byte aData : data)
      {
         int halfByte = (aData >>> 4) & 0x0F;
         int twoHalfs = 0;
         do
         {
            if ((0 <= halfByte) && (halfByte <= 9))
            {
               buf.append((char) ('0' + halfByte));
            }
            else
            {
               buf.append((char) ('a' + (halfByte - 10)));
            }
            halfByte = aData & 0x0F;
         } while (twoHalfs++ < 1);
      }
      return buf.toString();
   }

   public static String md5(String text)
         throws NoSuchAlgorithmException, UnsupportedEncodingException
   {
      MessageDigest md;
      md = MessageDigest.getInstance("MD5");
      byte[] md5hash;
      md.update(text.getBytes("iso-8859-1"), 0, text.length());
      md5hash = md.digest();
      return convertToHex(md5hash);
   }
} 