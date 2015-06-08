package org.labkey.workflow;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public class PermissionsHandlerImpl implements PermissionsHandler
{
    // TODO perhaps make this take a User and Container in its constructor so the methods below don't have to all have those as parameters.

    @Override
    public boolean canStartProcess(@NotNull String processDefinitionKey)
    {
        return true;
    }

    @Override
    public boolean canView(@NotNull WorkflowProcess process, User user, Container container)
    {
        return (process.getInitiatorId() == user.getUserId()) || container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canAccessData(@NotNull WorkflowProcess process, User user, Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canDelete(@NotNull WorkflowProcess process, User user, Container container)
    {
        return process.getInitiatorId() == user.getUserId() || container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canClaim(@NotNull WorkflowTask task, @NotNull User user, @Nullable Container container)
    {
        return  (container != null && container.hasPermission(user, AdminPermission.class)) || task.isInCandidateGroups(user);
    }

    public boolean canDelegate(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return task.hasCandidateGroups() && container.hasPermission(user, AdminPermission.class);
    }

    public boolean canAssign(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return task.hasCandidateGroups() && container.hasPermission(user, AdminPermission.class);
    }

    public boolean canView(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return ((task.getAssignee() != null && task.getAssignee().getUserId() == user.getUserId())) || canClaim(task, user, container) || canDelegate(task, user, container);
    }

    public boolean canAccessData(@Nullable WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    public boolean isAssigned(@NotNull WorkflowTask task, @NotNull User user)
    {
        return task.getAssignee() != null && task.getAssignee().getUserId() == user.getUserId();
    }

    public boolean canComplete(@NotNull WorkflowTask task, @NotNull User user, @Nullable Container container)
    {
        return isAssigned(task, user);
    }

    @Override
    public Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task, User user, Container container)
    {
        return Collections.<Class<? extends Permission>>singleton(AdminPermission.class);
    }
}
