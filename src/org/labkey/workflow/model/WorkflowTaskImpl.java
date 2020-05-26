/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.TaskFormField;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.WorkflowManager;
import org.labkey.workflow.WorkflowModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public abstract class WorkflowTaskImpl implements WorkflowTask
{
    protected TaskInfo _taskInfo;
    protected String _id;
    protected String _executionId;
    protected List<Integer> _groupIds = null;
    private Map<String, TaskFormField> _formFields = Collections.emptyMap();
    private WorkflowProcess _processInstance = null;
    private PermissionsHandler _permissionsHandler = null;

    protected WorkflowTaskImpl()
    {}

    public WorkflowTaskImpl(TaskInfo taskInfo)
    {
        _taskInfo = taskInfo;
    }

    @Override
    public String getId()
    {
        return _taskInfo == null ? _id : _taskInfo.getId();
    }

    @Override
    public String getExecutionId()
    {
        return _taskInfo == null ? _executionId : _taskInfo.getExecutionId();
    }

    @Override
    public String getName()
    {
        return _taskInfo == null ? null : _taskInfo.getName();
    }

    @Override
    public String getDescription()
    {
        return _taskInfo == null ? null : _taskInfo.getDescription();
    }

    @JsonIgnore
    private WorkflowProcess getProcessInstance()
    {
        if (_processInstance == null && _taskInfo != null)
        {
            _processInstance = new WorkflowProcessImpl(_taskInfo.getProcessInstanceId());
        }
        return _processInstance;
    }

    @Override
    public String getProcessDefinitionKey(Container container)
    {
        return _taskInfo == null || !isActive() ? null : getProcessInstance().getProcessDefinitionKey();
    }

    @Override
    public String getProcessDefinitionName(Container container)
    {
        String key = getProcessDefinitionKey(container);
        return _taskInfo == null || key == null ? null : WorkflowManager.get().getProcessDefinition(key, container).getName();
    }

    public String getProcessDefinitionModule(Container container)
    {
        if (_taskInfo == null)
            return WorkflowModule.NAME;
        else
        {
            return WorkflowManager.get().getProcessDefinitionModule(getProcessDefinitionId(), container);
        }
    }

    @Override
    public boolean isActive()
    {
        return getProcessInstance().isActive();
    }

    @Override
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

    @Override
    @Nullable
    public Integer getOwnerId()
    {
        if (_taskInfo == null || _taskInfo.getOwner() == null)
            return null;
        else
            return Integer.valueOf(_taskInfo.getOwner());
    }

    @Nullable
    public String getOwnerName()
    {
        User owner = getOwner();
        if (owner != null)
        {
            return owner.getDisplayName(null);
        }
        return null;
    }

    @Override
    public void assign(@NotNull Integer assigneeId, User user, Container container) throws Exception
    {
        WorkflowManager.get().assignTask(getId(), assigneeId, user, container);
        this.setAssignee(UserManager.getUser(assigneeId));
    }

    @Override
    public boolean isAssigned(@NotNull User user)
    {
        return getAssigneeId() != null && getAssigneeId() == user.getUserId();
    }

    @Override
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

    @Override
    @Nullable
    public Integer getAssigneeId()
    {
        if (_taskInfo == null || _taskInfo.getAssignee() == null)
            return null;
        else
            return Integer.valueOf(_taskInfo.getAssignee());
    }

    @Nullable
    public String getAssigneeName()
    {
        User user = getAssignee();
        if (user != null)
            return user.getDisplayName(null);
        return null;
    }

    @Override
    public String getProcessInstanceId()
    {
        return _taskInfo == null ? null : _taskInfo.getProcessInstanceId();
    }

    @Override
    public String getProcessDefinitionId()
    {
        return _taskInfo == null ? null : _taskInfo.getProcessDefinitionId();
    }

    @Override
    public Date getCreateTime()
    {
        return _taskInfo == null ? null : _taskInfo.getCreateTime();
    }

    @Override
    public String getTaskDefinitionKey()
    {
        return _taskInfo == null ? null : _taskInfo.getTaskDefinitionKey();
    }

    @Override
    public Date getDueDate()
    {
        return _taskInfo == null ? null : _taskInfo.getDueDate();
    }

    @Override
    public String getParentTaskId()
    {
        return _taskInfo == null ? null : _taskInfo.getParentTaskId();
    }

    @Override
    public Map<String, Object> getTaskLocalVariables()
    {
        return _taskInfo == null ? Collections.emptyMap() : _taskInfo.getTaskLocalVariables();
    }

    @Override
    public Map<String, Object> getProcessVariables()
    {
        return _taskInfo == null ? Collections.emptyMap() : _taskInfo.getProcessVariables();
    }

    @Override
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

    @Override
    @JsonIgnore // for some reason this comes back as the empty string, but it is also available as one of the process variables
    public String getContainer()
    {
        return _taskInfo == null ? null : _taskInfo.getTenantId();
    }

    @Override
    public boolean isInCandidateGroups(User user)
    {
        return hasCandidateGroups() && CollectionUtils.containsAny(getGroupIds(), Arrays.asList(ArrayUtils.toObject(user.getGroups())));
    }

    @Override
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

    @Override
    public boolean canClaim(User user, Container container)
    {
        return getAssigneeId() == null && isActive() && getPermissionsHandler(user, container).canClaim(this);
    }

    @Override
    public boolean canDelegate(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canDelegate(this);
    }

    @Override
    public boolean canAssign(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canAssign(this);
    }

    @Override
    public boolean canView(User user, Container container)
    {
        return getPermissionsHandler(user, container).canView(this);
    }

    @Override
    public boolean canAccessData(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canAccessData(this);
    }

    @Override
    public boolean canComplete(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canComplete(this);
    }

    @Override
    public boolean canUpdate(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canUpdate(this);
    }

    @Override
    public Set<Class<? extends Permission>> getReassignPermissions(User user, Container container)
    {
        return getPermissionsHandler(user, container).getCandidateUserPermissions(this);
    }

    @Override
    @JsonIgnore
    @NotNull
    public Map<String, TaskFormField> getFormFields()
    {
        return WorkflowManager.get().getFormFields(getId());
    }

}
