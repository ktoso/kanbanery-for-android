package pl.project13.janbanery.resources;

import org.joda.time.DateTime;

import java.util.Date;

/**
 * @author Konrad Malawski
 */
public class HydratedTask
{

   private Task task;
   private TaskType taskType;
   private User owner;

   public HydratedTask(Task task, TaskType taskType, User owner)
   {
      assert owner != null : "Task should have owner";
      assert task.getOwnerId().equals(owner.getId()) : "Task should have matching owner";
      assert taskType != null : "Task should have task type";
      assert task.getTaskTypeId().equals(taskType.getId()) : "Task should have matching task type";

      this.task = task;
      this.taskType = taskType;
      this.owner = owner;
   }

   public String getResourceId()
   {
      return task.getResourceId();
   }

   public Long getId()
   {
      return task.getId();
   }

   public String getTitle()
   {
      return task.getTitle();
   }

   public void setTitle(String title)
   {
      task.setTitle(title);
   }

   public Long getColumnId()
   {
      return task.getColumnId();
   }

   public void setColumnId(Long columnId)
   {
      task.setColumnId(columnId);
   }

   public Long getCreatorId()
   {
      return task.getCreatorId();
   }

   public String getDescription()
   {
      return task.getDescription();
   }

   public void setDescription(String description)
   {
      task.setDescription(description);
   }

   public Long getEstimateId()
   {
      return task.getEstimateId();
   }

   public void setEstimateId(Long estimateId)
   {
      task.setEstimateId(estimateId);
   }

   public Long getOwnerId()
   {
      return task.getOwnerId();
   }

   public void setOwnerId(Long ownerId)
   {
      task.setOwnerId(ownerId);
   }

   public Integer getPosition()
   {
      return task.getPosition();
   }

   public void setPosition(Integer position)
   {
      task.setPosition(position);
   }

   public Priority getPriority()
   {
      return task.getPriority();
   }

   public void setPriority(Priority priority)
   {
      task.setPriority(priority);
   }

   public Boolean getReadyToPull()
   {
      return task.getReadyToPull();
   }

   public void setReadyToPull(Boolean readyToPull)
   {
      task.setReadyToPull(readyToPull);
   }

   public Boolean getBlocked()
   {
      return task.getBlocked();
   }

   public DateTime getMovedAt()
   {
      return task.getMovedAt();
   }

   public void setDeadline(Date deadline)
   {
      task.setDeadline(deadline);
   }

   public Date getDeadline()
   {
      return task.getDeadline();
   }

   public boolean isArchived()
   {
      return task.isArchived();
   }

   public boolean isIceBoxed()
   {
      return task.isIceBoxed();
   }

   public DateTime getUpdatedAt()
   {
      return task.getUpdatedAt();
   }

   public DateTime getCreatedAt()
   {
      return task.getCreatedAt();
   }

   public TaskType getTaskType()
   {
      return taskType;
   }

   public User getOwner()
   {
      return owner;
   }

   public void setOwner(User owner)
   {
      this.owner = owner;
   }

   @Override
   public int hashCode()
   {
      return task.hashCode();
   }

   @Override
   public boolean equals(Object o)
   {
      return task.equals(o);
   }

   @Override
   public String toString()
   {
      return task.toString();
   }
}
