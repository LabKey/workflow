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
package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;

import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 *
 */
public abstract class PermissionsHandler
{
    protected final User _user;
    protected final Container _container;
    protected final boolean _hasAdmin;

    public PermissionsHandler(User user, Container container)
    {
        _user = user;
        _container = container;
        _hasAdmin = _container.hasPermission(_user, AdminPermission.class);
    }

    public boolean canStartProcess(@NotNull String processDefinitionKey)
    {
        return _container.hasPermission(_user, ReadPermission.class);
    }

    public boolean canDeployProcess(@NotNull String processDefinitionKey) { return _hasAdmin; }


    public boolean canView(@NotNull WorkflowProcess process)
    {
        return (process.getInitiatorId() == _user.getUserId()) || _hasAdmin;
    }

    public boolean canAccessData(@NotNull WorkflowProcess process)
    {
        return _hasAdmin;
    }


    public boolean canDelete(@NotNull WorkflowProcess process)
    {
        return _hasAdmin || process.getInitiatorId() == _user.getUserId();
    }

    public boolean canClaim(@NotNull WorkflowTask task)
    {
        return _hasAdmin || task.isInCandidateGroups(_user);
    }

    public boolean canDelegate(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    public boolean canAssign(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    public boolean canView(@NotNull WorkflowTask task)
    {
        return ((task.getAssignee() != null && task.getAssignee().getUserId() == _user.getUserId())) || canClaim(task) || canDelegate(task);
    }

    public boolean canAccessData(@Nullable WorkflowTask task)
    {
        return _hasAdmin;
    }

    public boolean canComplete(@NotNull WorkflowTask task)
    {
        return task.isAssigned(_user);
    }

    public boolean canUpdate(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    public SimpleFilter getProcessListFilter()
    {
        return new SimpleFilter();
    }

    protected SimpleFilter.FilterClause getInitiatorCondition()
    {
        SQLFragment sql = new SQLFragment("act_ru_execution.id_ IN (SELECT V.execution_id_ FROM workflow.act_ru_variable V WHERE V.name_ = 'initiatorId' AND V.text_ = ?)");
        sql.add(String.valueOf(_user.getUserId()));
        return new SimpleFilter.SQLClause("(" + sql.getSQL() + ")", sql.getParams().toArray(),  new FieldKey(null, "id_"));
    }

    public SimpleFilter getTaskListFilter()
    {
        return new SimpleFilter();
    }

    protected SimpleFilter.FilterClause getAssigneeOwnerClause()
    {
        SQLFragment sql = new SQLFragment("owner_ = ? OR assignee_ = ?");
        sql.add(String.valueOf(_user.getUserId()));
        sql.add(String.valueOf(_user.getUserId()));
        return new SimpleFilter.SQLClause("(" + sql.getSQL() + ")", sql.getParams().toArray(), new FieldKey(null, "id_"));
    }

    /**
     * For a particular task, return the set of permissions a user must have in order to perform this task.
     * This is used when displaying the list of candidate users to assign a task to.
     * @param task the task that is to be assigned
     * @return a set of permissions classes
     */
    public abstract Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task);

    @SuppressWarnings("unchecked")
    public Object getDataAccessParameter(Map<String, Object> variables, String key)
    {
        Object dataAccess = variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                return ((Map<String, Object>)parameters).get(key);
        }
        return null;
    }
}
