package pl.project13.janbanery.util;

import pl.project13.janbanery.resources.Column;
import pl.project13.janbanery.resources.Task;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;

/**
 * A map that enables easier access to Tasks via their column and id
 *
 * @author Konrad Malawski
 */
public class TaskMap
{
   private Map<String, Task> map = newHashMap();

   public void put(Column column, Task task)
   {
      map.put(formatKey(column, task), task);
   }

   public Task get(Column column, Long taskId)
   {
      return map.get(formatKey(column, taskId));
   }

   private String formatKey(Column column, Task task)
   {
      return formatKey(column, task.getId());
   }

   private String formatKey(Column column, Long taskId)
   {
      return column.getId() + "_" + taskId;
   }

   public Collection<Task> values()
   {
      return map.values();
   }

   public int size()
   {
      return map.size();
   }

   public Set<String> keySet()
   {
      return map.keySet();
   }

   public boolean isEmpty()
   {
      return map.isEmpty();
   }

   @Override
   public int hashCode()
   {
      return map.hashCode();
   }

   @Override
   public boolean equals(Object o)
   {
      return map.equals(o);
   }

   public Set<Map.Entry<String, Task>> entrySet()
   {
      return map.entrySet();
   }

   public boolean containsValue(Object o)
   {
      return map.containsValue(o);
   }

   public boolean containsKey(Object o)
   {
      return map.containsKey(o);
   }

   public void clear()
   {
      map.clear();
   }
}
