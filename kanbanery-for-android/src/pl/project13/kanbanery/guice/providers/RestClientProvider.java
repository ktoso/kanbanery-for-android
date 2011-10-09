package pl.project13.kanbanery.guice.providers;

import com.google.inject.Provider;
import pl.project13.janbanery.android.rest.AndroidCompatibleRestClient;
import pl.project13.janbanery.core.rest.RestClient;

/**
 * @author Konrad Malawski
 */
public class RestClientProvider implements Provider<RestClient>
{
   @Override
   public RestClient get()
   {
      return new AndroidCompatibleRestClient();
   }
}
