package pl.project13.kanbanery.util.cache;

/**
 * Created by ktoso on 8/20/11 at 12:38 PM
 */
public interface Cache<T>
{
   boolean isCacheHit(String checkIfCached);

   boolean isCacheHit(Object checkIfCachedByToString);

   T get(Object getMeByToString);

   T get(String getMe);

   void cache(String name, T cacheMe);
}
