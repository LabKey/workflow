package org.labkey.workflow.view;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.collections15.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.workflow.WorkflowManager;
import org.labkey.workflow.view.WorkflowProcessBean;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowTaskBean
{
    private Task _engineTask;
    private List<Integer> _groupIds = null;
    private String _processDefinitionKey;

    public WorkflowTaskBean(String taskId)
    {
        _engineTask = WorkflowManager.get().getTask(taskId);
    }

    public WorkflowTaskBean(Task engineTask)
    {
        _engineTask = engineTask;
    }

    public String getId()
    {
        return _engineTask.getId();
    }

    public String getName()
    {
        return _engineTask.getName();
    }

    public String getDescription()
    {
        return _engineTask.getDescription();
    }

    public String getProcessDefinitionKey()
    {
        if (_processDefinitionKey == null)
        {
            _processDefinitionKey = WorkflowManager.get().getProcessInstance(_engineTask.getProcessInstanceId()).getProcessDefinitionKey();
        }
        return _processDefinitionKey;
    }

    @Nullable
    public User getProcessInitiator()
    {
        if ((getVariables() == null) || (getVariables().get(WorkflowProcessBean.INITIATOR_ID) != null))
            return UserManager.getUser(Integer.valueOf((String) getVariables().get(WorkflowProcessBean.INITIATOR_ID)));
        else
            return null;
    }

    @Nullable
    public User getOwner()
    {
        if (_engineTask.getOwner() == null)
            return null;
        else
            return UserManager.getUser(Integer.valueOf(_engineTask.getOwner()));
    }

    @Nullable
    public User getAssignee()
    {
        if (_engineTask.getAssignee() == null)
            return null;
        else
            return UserManager.getUser(Integer.valueOf(_engineTask.getAssignee()));
    }

    public String getProcessInstanceId()
    {
        return _engineTask.getProcessInstanceId();
    }

    public String getProcessDefinitionId()
    {
        return _engineTask.getProcessDefinitionId();
    }

    public Date getCreateTime()
    {
        return _engineTask.getCreateTime();
    }

    public String getTaskDefinitionKey()
    {
        return _engineTask.getTaskDefinitionKey();
    }

    public Date getDueDate()
    {
        return _engineTask.getDueDate();
    }

    public String getParentTaskId()
    {
        return _engineTask.getParentTaskId();
    }

    public Map<String, Object> getTaskLocalVariables()
    {
        return _engineTask.getTaskLocalVariables();
    }

    public Map<String, Object> getProcessVariables()
    {
        return _engineTask.getProcessVariables();
    }

    @Nullable
    public Map<String, Object> getVariables()
    {
        Map<String, Object> variables = getProcessVariables();
        if (variables == null)
        {
            variables = getTaskLocalVariables();
        }
        else if (getTaskLocalVariables() != null)
        {
            variables.putAll(getTaskLocalVariables());
        }
        return variables;
    }

    @NotNull
    public String getContainer()
    {
        return _engineTask.getTenantId();
    }

    public List<Integer> getGroupIds()
    {
        if (_groupIds == null)
            _groupIds = WorkflowManager.get().getCandidateGroupIds(getId());
        return _groupIds;
    }

    public boolean canClaim(User user, Container container)
    {
        return CollectionUtils.containsAny(getGroupIds(), Arrays.asList(user.getGroups()));
    }

    public boolean canDelegate(User user, Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    public boolean canAssign(User user, Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
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

}
