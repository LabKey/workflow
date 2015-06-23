package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;

import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public abstract class PermissionsHandler
{
    protected final User _user;
    protected final Container _container;

    public PermissionsHandler(User user, Container container)
    {
        _user = user;
        _container = container;
    }

    public abstract boolean canStartProcess(@NotNull String processDefinitionKey);

    public abstract boolean canDeployProcess(@NotNull String processDefinitionKey);

    public abstract boolean canView(@NotNull WorkflowProcess process);

    public abstract boolean canAccessData(@NotNull WorkflowProcess process);

    public abstract boolean canDelete(@NotNull WorkflowProcess process);

    public abstract boolean canClaim(@NotNull WorkflowTask task);

    public abstract boolean canDelegate(@NotNull WorkflowTask task);

    public abstract boolean canAssign(@NotNull WorkflowTask task);

    public abstract boolean canView(@NotNull WorkflowTask task);

    public abstract boolean canAccessData(@NotNull WorkflowTask task);

    public abstract boolean canComplete(@NotNull WorkflowTask task);

    public abstract Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task);

}
