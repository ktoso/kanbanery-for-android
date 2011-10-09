package pl.project13.kanbanery.ui.activities;

import android.content.SharedPreferences;
import com.google.inject.Inject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import pl.project13.janbanery.exceptions.kanbanery.UnauthorizedKanbaneryException;
import pl.project13.kanbanery.guice.providers.JanbaneryProvider;
import pl.project13.kanbanery.test.InjectedTestRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.doThrow;
import static org.powermock.api.mockito.PowerMockito.spy;
import static pl.project13.kanbanery.test.performer.Perform.click;
import static pl.project13.kanbanery.test.performer.Perform.type;

@RunWith(InjectedTestRunner.class)
public class SignInActivityTest
{
   @Inject
   private SignInActivity activity;

   private SignInActivity a;

   @Inject
   private SharedPreferences sharedPreferences;
   private JanbaneryProvider janbaneryProvider;

   @Before
   public void setUp() throws Exception
   {
      a = spy(activity);

      sharedPreferences = spy(sharedPreferences);
      when(sharedPreferences.contains(anyString())).thenReturn(false);

      janbaneryProvider = spy(a.janbaneryProvider);

      a.onTestCreate(sharedPreferences, janbaneryProvider);
   }

   @Test
   public void shouldLoginValidUserAndGoToProjectSelection() throws Exception
   {
      // given
      String username = "ktoso@project13.pl";
      String pass = "sample_pas";

      doReturn("some_sample_key")
            .when(a.janbaneryProvider).obtainApiKeyFor(username, pass);

      // when
      performSignIn(username, pass);

      // then
      verify(a.janbaneryProvider).signIn(eq(username), eq(pass));
      verify(a).goToProjects();
   }

   @Test
   public void shouldShowUnauthorizedMessageForInvalidUser() throws Exception
   {
      // given
      String username = "ktoso@project13.pl";
      String pass = "sample_pas";

      UnauthorizedKanbaneryException toBeThrown = new UnauthorizedKanbaneryException("Wrong password");

      doThrow(toBeThrown)
            .when(a.janbaneryProvider).signIn(username, pass);

      // when
      performSignIn(username, pass);

      // then
      verify(a.janbaneryProvider).signIn(eq(username), eq(pass));
      verify(a).showUnauthorizedMessage(toBeThrown);
   }

   private void performSignIn(String username, String pass)
   {
      type(username).into(a.vEmail);
      type(pass).into(a.vPass);

      click(a.vSignIn);
   }

}
