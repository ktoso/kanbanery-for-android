package pl.project13.kanbanery.ui.activities;

import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.Smoke;
import com.jayway.android.robotium.solo.Solo;
import pl.project13.kanbanery.R;


public class SignInActivityTest extends ActivityInstrumentationTestCase2<SignInActivity>
{

   private Solo solo;

   public SignInActivityTest()
   {
      super("pl.project13.kanbanery", SignInActivity.class);
   }

   public void setUp() throws Exception
   {
      solo = new Solo(getInstrumentation(), getActivity());
   }

   @Smoke
   public void testAddNote() throws Exception
   {
      solo.enterText(R.id.email_edit_text, "kmalawski@project13.pl");
      solo.enterText(R.id.pass_edit_text, "kanbankanbanban");
      
      solo.assertCurrentActivity("Expected Kanbanery activity", "Kanbanery");
      
      solo.clickOnButton(R.id.sign_in_btn);

      solo.waitForActivity("Kanbanery - projects");
   }

   @Override
   @SuppressWarnings("FinalizeCalledExplicitly")
   public void tearDown() throws Exception
   {
      try
      {
         solo.finalize();
      }
      catch (Throwable e)
      {
         e.printStackTrace();
      }
      getActivity().finish();
      super.tearDown();
   }
}
