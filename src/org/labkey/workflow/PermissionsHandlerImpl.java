/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowTask;

import java.util.Collections;
import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public class PermissionsHandlerImpl extends PermissionsHandler
{
    private final boolean _hasAdmin;

    public PermissionsHandlerImpl(@NotNull User user, @NotNull Container container)
    {
        super (user, container);
        _hasAdmin = _container.hasPermission(_user, AdminPermission.class);
    }

    @Override
    public boolean canStartProcess(@NotNull String processDefinitionKey)
    {
        return _container.hasPermission(_user, ReadPermission.class);
    }

    @Override
    public boolean canDeployProcess(@NotNull String processDefinitionKey) { return _hasAdmin; }

    @Override
    public boolean canView(@NotNull WorkflowProcess process)
    {
        return (process.getInitiatorId() == _user.getUserId()) || _hasAdmin;
    }

    @Override
    public boolean canAccessData(@NotNull WorkflowProcess process)
    {
        return _hasAdmin;
    }

    @Override
    public boolean canDelete(@NotNull WorkflowProcess process)
    {
        return process.getInitiatorId() == _user.getUserId() || _hasAdmin;
    }

    @Override
    public boolean canClaim(@NotNull WorkflowTask task)
    {
        return _hasAdmin || task.isInCandidateGroups(_user);
    }

    @Override
    public boolean canDelegate(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    @Override
    public boolean canAssign(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    @Override
    public boolean canView(@NotNull WorkflowTask task)
    {
        return ((task.getAssignee() != null && task.getAssignee().getUserId() == _user.getUserId())) || canClaim(task) || canDelegate(task);
    }

    @Override
    public boolean canAccessData(@Nullable WorkflowTask task)
    {
        return _hasAdmin;
    }

    @Override
    public boolean canComplete(@NotNull WorkflowTask task)
    {
        return task.isAssigned(_user);
    }

    @Override
    public boolean canUpdate(@NotNull WorkflowTask task)
    {
        return _hasAdmin;
    }

    @Override
    public SimpleFilter getProcessListFilter()
    {
        SimpleFilter filter = new SimpleFilter();
        if (!_hasAdmin) // admins can see everything by default
        {
            filter.addClause(getInitiatorCondition());
        }
        return filter;
    }

    @Override
    public SimpleFilter getTaskListFilter()
    {
        SimpleFilter filter = new SimpleFilter();
        if (!_hasAdmin) // admins can see everything by default
        {
            filter.addClause(getAssigneeOwnerClause()); // otherwise you see tasks you are assigned or that you own
        }
        return filter;
    }

    @Override
    public Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task)
    {
        return Collections.<Class<? extends Permission>>singleton(AdminPermission.class);
    }
}
