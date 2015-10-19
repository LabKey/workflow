package org.labkey.workflow.model;

import org.activiti.engine.history.HistoricTaskInstance;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.workflow.WorkflowManager;

import java.util.Date;
import java.util.List;

/**
 * Created by cnathe on 9/25/2015.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowHistoricTaskImpl extends WorkflowTaskImpl
{
    private HistoricTaskInstance _historicTask;

    public WorkflowHistoricTaskImpl(String taskId, Container container)
    {
        this(WorkflowManager.get().getHistoricTask(taskId, container));
        _id = taskId;
    }

    public WorkflowHistoricTaskImpl(HistoricTaskInstance historicTask)
    {
        _historicTask = historicTask;
        _taskInfo = historicTask;
    }


    // Don't do anything for set methods for a HistoricTaskInstance
    public void setName(String name)
    {}
    public void setDescription(String description)
    {}
    public void setOwner(User owner)
    {}
    public void setAssignee(User assignee)
    {}
    public void setReadyForReview()
    {}
    public void setDueDate(Date dueDate)
    {}
    public void setParentTaskId(String parentTaskId)
    {}
    public void setContainer(String containerId)
    {}
    public void delegate(User user)
    {}

    public boolean isDelegated()
    {
        return false;
    }

    public boolean isReadyForReview()
    {
        return false;
    }

    public boolean isSuspended()
    {
        return false;
    }

    public boolean isActive() { return false; }

    @Override
    public List<Integer> getGroupIds()
    {
        return null;
    }

    @Override
    public Date getEndDate()
    {
        return _historicTask.getEndTime();
    }
}