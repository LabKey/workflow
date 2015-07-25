/*
 * Copyright (c) 2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.exp.Lsid;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.TaskFormField;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.WorkflowManager;
import org.labkey.workflow.WorkflowModule;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowTaskImpl implements WorkflowTask
{
    private Task _engineTask;
    private String _id;
    private List<Integer> _groupIds = null;
    private Map<String, TaskFormField> _formFields = null;
    private ProcessInstance _processInstance = null;
    private PermissionsHandler _permissionsHandler = null;

    public WorkflowTaskImpl(String taskId, Container container)
    {
        _engineTask = WorkflowManager.get().getEngineTask(taskId, container);
        _id = taskId;
    }

    public WorkflowTaskImpl(Task engineTask)
    {
        _engineTask = engineTask;
    }

    public String getId()
    {
        return _engineTask == null ? _id : _engineTask.getId();
    }

    public String getName()
    {
        return _engineTask == null ? null : _engineTask.getName();
    }

    public String getDescription()
    {
        return _engineTask == null ? null : _engineTask.getDescription();
    }

    @JsonIgnore
    private ProcessInstance getProcessInstance()
    {
        if (_processInstance == null && _engineTask != null)
        {
            _processInstance = WorkflowManager.get().getProcessInstance(_engineTask.getProcessInstanceId());
        }
        return _processInstance;
    }

    public String getProcessDefinitionKey(Container container)
    {
        return _engineTask == null ? null : getProcessInstance().getProcessDefinitionKey();
    }

    public String getProcessDefinitionName(Container container)
    {
        return _engineTask == null ? null : WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(container), container).getName();
    }

    public String getProcessDefinitionModule(Container container)
    {
        if (_engineTask == null)
            return WorkflowModule.NAME;
        else
        {
            return WorkflowManager.get().getProcessDefinitionModule(getProcessDefinitionId(), container);
        }
    }

    @Nullable
    @JsonIgnore
    public User getOwner()
    {
        Integer id = getOwnerId();
        if (id == null)
            return null;
        else
            return UserManager.getUser(id);
    }

    @Nullable
    public Integer getOwnerId()
    {
        if (_engineTask == null || _engineTask.getOwner() == null)
            return null;
        else
            return Integer.valueOf(_engineTask.getOwner());
    }

    @Nullable
    @JsonIgnore
    public User getAssignee()
    {
        Integer id = getAssigneeId();
        if (id == null)
            return null;
        else
            return UserManager.getUser(id);
    }

    @Nullable
    public Integer getAssigneeId()
    {
        if (_engineTask == null || _engineTask.getAssignee() == null)
            return null;
        else
            return Integer.valueOf(_engineTask.getAssignee());
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

    @JsonIgnore
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

    @JsonIgnore // for some reason this comes back as the empty string, but it is also available as one of the process variables
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

    public boolean isInCandidateGroups(User user)
    {
        return hasCandidateGroups() && CollectionUtils.containsAny(getGroupIds(), Arrays.asList(ArrayUtils.toObject(user.getGroups())));
    }

    public boolean hasCandidateGroups()
    {
        return getGroupIds() != null && !_groupIds.isEmpty();
    }

    private PermissionsHandler getPermissionsHandler(User user, Container container)
    {
        if (_permissionsHandler == null)
        {
            _permissionsHandler = WorkflowRegistry.get().getPermissionsHandler(getProcessDefinitionModule(container), user, container);
        }
        return _permissionsHandler;
    }

    public boolean canClaim(User user, Container container)
    {
        return getAssigneeId() == null && isActive() && getPermissionsHandler(user, container).canClaim(this);
    }

    public boolean canDelegate(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canDelegate(this);
    }

    public boolean canAssign(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canAssign(this);
    }

    public boolean canView(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canView(this);
    }

    public boolean canAccessData(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canAccessData(this);
    }

    public boolean canComplete(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canComplete(this);
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

    public boolean isAssigned(User user)
    {
        return getAssigneeId() != null && getAssigneeId() == user.getUserId();
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

    public Set<Class<? extends Permission>> getReassignPermissions(User user, Container container)
    {
        return getPermissionsHandler(user, container).getCandidateUserPermissions(this);
    }

    public boolean isActive() { return _engineTask != null; }

    @JsonIgnore
    @NotNull
    public Map<String, TaskFormField> getFormFields()
    {
        if (_formFields == null)
        {
            _formFields = WorkflowManager.get().getFormFields(getId());
        }
        return _formFields;
    }

}
