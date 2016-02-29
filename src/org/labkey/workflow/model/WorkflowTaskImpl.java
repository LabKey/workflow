/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
package org.labkey.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.activiti.engine.task.TaskInfo;
import org.apache.commons.collections15.CollectionUtils;
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
    protected List<Integer> _groupIds = null;
    private Map<String, TaskFormField> _formFields = null;
    private WorkflowProcess _processInstance = null;
    private PermissionsHandler _permissionsHandler = null;

    protected WorkflowTaskImpl()
    {}

    public WorkflowTaskImpl(TaskInfo taskInfo)
    {
        _taskInfo = taskInfo;
    }

    public String getId()
    {
        return _taskInfo == null ? _id : _taskInfo.getId();
    }

    public String getName()
    {
        return _taskInfo == null ? null : _taskInfo.getName();
    }

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

    public String getProcessDefinitionKey(Container container)
    {
        return _taskInfo == null || !isActive() ? null : getProcessInstance().getProcessDefinitionKey();
    }

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

    public boolean isActive()
    {
        return getProcessInstance().isActive();
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

    public void assign(@NotNull Integer assigneeId, User user, Container container) throws Exception
    {
        WorkflowManager.get().assignTask(getId(), assigneeId, user, container);
        this.setAssignee(UserManager.getUser(assigneeId));
    }

    public boolean isAssigned(@NotNull User user)
    {
        return getAssigneeId() != null && getAssigneeId() == user.getUserId();
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

    public String getProcessInstanceId()
    {
        return _taskInfo.getProcessInstanceId();
    }

    public String getProcessDefinitionId()
    {
        return _taskInfo.getProcessDefinitionId();
    }

    public Date getCreateTime()
    {
        return _taskInfo.getCreateTime();
    }

    public String getTaskDefinitionKey()
    {
        return _taskInfo.getTaskDefinitionKey();
    }

    public Date getDueDate()
    {
        return _taskInfo.getDueDate();
    }

    public String getParentTaskId()
    {
        return _taskInfo.getParentTaskId();
    }

    public Map<String, Object> getTaskLocalVariables()
    {
        return _taskInfo.getTaskLocalVariables();
    }

    public Map<String, Object> getProcessVariables()
    {
        return _taskInfo.getProcessVariables();
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
        return _taskInfo.getTenantId();
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
        return getPermissionsHandler(user, container).canView(this);
    }

    public boolean canAccessData(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canAccessData(this);
    }

    public boolean canComplete(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canComplete(this);
    }

    public boolean canUpdate(User user, Container container)
    {
        return isActive() && getPermissionsHandler(user, container).canUpdate(this);
    }

    public Set<Class<? extends Permission>> getReassignPermissions(User user, Container container)
    {
        return getPermissionsHandler(user, container).getCandidateUserPermissions(this);
    }

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
