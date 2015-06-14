package org.labkey.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowTask;

import java.util.Collections;
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

    @Override
    public boolean canDelegate(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canAssign(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canView(@NotNull WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return ((task.getAssignee() != null && task.getAssignee().getUserId() == user.getUserId())) || canClaim(task, user, container) || canDelegate(task, user, container);
    }

    @Override
    public boolean canAccessData(@Nullable WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean isAssigned(@NotNull WorkflowTask task, @NotNull User user)
    {
        return task.getAssignee() != null && task.getAssignee().getUserId() == user.getUserId();
    }

    @Override
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
