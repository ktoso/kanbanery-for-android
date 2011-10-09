package pl.project13.kanbanery.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import java.util.List;

import static com.google.common.collect.Lists.newLinkedList;

public class GoogleAccountUtils
{
   public String getUsername(Context context)
   {
      AccountManager manager = AccountManager.get(context);
      Account[] accounts = manager.getAccountsByType("com.google");
      List<String> possibleEmails = newLinkedList();

      for (Account account : accounts)
      {
         // TODO: Check possibleEmail against an email regex or treat
         // account.name as an email address only for certain account.type values.
         possibleEmails.add(account.name);
      }

      if (!possibleEmails.isEmpty() && possibleEmails.get(0) != null)
      {
         String email = possibleEmails.get(0);
         String[] parts = email.split("@");
         if (parts.length > 0 && parts[0] != null)
         {
            return parts[0];
         }
         else
         {
            return null;
         }
      }
      else
      {
         return null;
      }
   }

   public String getDeviceId(ContentResolver contentResolver)
   {
      return Settings.System.getString(contentResolver, Settings.System.ANDROID_ID);
   }
}
