package org.labkey.workflow.model;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
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
public class WorkflowEngineTaskImpl extends WorkflowTaskImpl
{
    private Task _engineTask;

    public WorkflowEngineTaskImpl(String taskId, Container container)
    {
        this(WorkflowManager.get().getEngineTask(taskId, container));
        _id = taskId;
    }

    public WorkflowEngineTaskImpl(Task engineTask)
    {
        _engineTask = engineTask;
        _taskInfo = _engineTask;
    }


    public void setName(String name)
    {
        _engineTask.setName(name);
    }

    public void setDescription(String description)
    {
        _engineTask.setDescription(description);
    }

    /**
     * Set the user responsible for full completion of the task, which may include review of results
     * if the task has been delegated
     * @param owner
     */
    public void setOwner(User owner)
    {
        _engineTask.setOwner(String.valueOf(owner.getUserId()));
    }

    /**
     * Set the user who is currently assigned to work on this task
     * @param assignee
     */
    public void setAssignee(User assignee)
    {
        _engineTask.setOwner(String.valueOf(assignee.getUserId()));
    }

    public boolean isDelegated()
    {
        return _engineTask.getDelegationState() == DelegationState.PENDING;
    }

    public boolean isReadyForReview()
    {
        return _engineTask.getDelegationState() == DelegationState.RESOLVED;
    }

    public void setReadyForReview()
    {
        _engineTask.setDelegationState(DelegationState.RESOLVED);
    }

    public void setDueDate(Date dueDate)
    {
        _engineTask.setDueDate(dueDate);
    }

    public void delegate(User user)
    {
        _engineTask.delegate(String.valueOf(user.getUserId()));
    }

    public void setParentTaskId(String parentTaskId)
    {
        _engineTask.setParentTaskId(parentTaskId);
    }

    public void setContainer(String containerId)
    {
        _engineTask.setTenantId(containerId);
    }

    public boolean isSuspended()
    {
        return _engineTask.isSuspended();
    }

    public boolean isActive() { return _engineTask != null; }

    public List<Integer> getGroupIds()
    {
        if (_groupIds == null)
            _groupIds = WorkflowManager.get().getCandidateGroupIds(getId());
        return _groupIds;
    }

    @Override
    public Date getEndDate()
    {
        return null;
    }
}
