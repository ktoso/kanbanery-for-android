package pl.project13.kanbanery.util.cache;

import android.util.Log;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

/**
 * Created by ktoso on 8/20/11 at 3:13 PM
 */
public class SimpleCache<T> implements Cache<T>
{

   private static final String TAG = SimpleCache.class.getSimpleName();

   Map<String, T> cacheMap = newHashMap();

   public static <T> SimpleCache<T> newSimpleCache()
   {
      return new SimpleCache<T>();
   }

   public static <T> SimpleCache<T> newSimpleCache(Class<T> classes)
   {
      return new SimpleCache<T>();
   }

   @Override
   public boolean isCacheHit(String checkIfCached)
   {
      boolean hit = cacheMap.containsKey(checkIfCached);
      return hit;
   }

   @Override
   public boolean isCacheHit(Object checkIfCachedByToString)
   {
      return checkIfCachedByToString != null && isCacheHit(checkIfCachedByToString.toString());
   }

   @Override
   public T get(Object getMeByToString)
   {
      return cacheMap.get(getMeByToString.toString());
   }

   @Override
   public T get(String getMe)
   {
      return cacheMap.get(getMe);
   }

   @Override
   public void cache(String name, T cacheMe)
   {
      Log.d(TAG, "Cached instance for key: '" + name + "'");
      cacheMap.put(name, cacheMe);
   }
}
