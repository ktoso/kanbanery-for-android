package pl.project13.kanbanery.ui.activities.dialog;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import com.google.common.base.Charsets;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import pl.project13.kanbanery.R;
import pl.project13.kanbanery.annotation.DialogActivity;
import pl.project13.kanbanery.ui.Intents;
import pl.project13.kanbanery.util.GoogleAccountUtils;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

@DialogActivity
public class GetPremiumVersionDialog extends RoboActivity
{

   @InjectView(R.id.why_should_i_go_pro_text)
   WebView vWhyWebView;

   @InjectView(R.id.lets_get_it_now_btn)
   Button vConfirm;

   @InjectView(R.id.cancel)
   Button vCancel;

   @Inject
   GoogleAccountUtils googleAccountUtils;

   @Inject
   @Named("premiumAddUrl")
   private String getPremiumUrl;

   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.get_full_version_dialog);

      String callUrl = prepareCallUrl(getPremiumUrl);

      vWhyWebView.loadUrl(callUrl);
      vWhyWebView.getSettings().setJavaScriptEnabled(true);

      vConfirm.setOnClickListener(new GoProOnClick());
      vCancel.setOnClickListener(new FinishOnClick());
   }

   private String prepareCallUrl(String callUrl)
   {
      callUrl += "?u=" + googleAccountUtils.getUsername(this);
      callUrl += "&d=" + googleAccountUtils.getDeviceId(getContentResolver());
      return callUrl;
   }

   private class FinishOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         GetPremiumVersionDialog.this.finish();
      }
   }

   private class GoProOnClick implements View.OnClickListener
   {
      @Override
      public void onClick(View view)
      {
         Intent goToMarket = Intents.GoToProVersionOnAndroidMarket.create(GetPremiumVersionDialog.this);
         startActivity(goToMarket);
      }
   }
}