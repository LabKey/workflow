package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;

import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public interface PermissionsHandler
{
    // TODO perhaps better to have a method getPermissions(task, user, container) if we define own Permission classes for workflow

    boolean canStartProcess(String processDefinitionKey);

    boolean canView(@NotNull WorkflowProcess process, User user, Container container);

    boolean canAccessData(@NotNull WorkflowProcess process, User user, Container container);

    boolean canDelete(@NotNull WorkflowProcess process, User user, Container container);

    boolean canClaim(@NotNull WorkflowTask task, User user, Container container);

    boolean canDelegate(@NotNull WorkflowTask task, User user, Container container);

    boolean canAssign(@NotNull WorkflowTask task, User user, Container container);

    boolean canView(@NotNull WorkflowTask task, User user, Container container);

    boolean canAccessData(@NotNull WorkflowTask task, User user, Container container);

    boolean isAssigned(@NotNull WorkflowTask task, User user);

    boolean canComplete(@NotNull WorkflowTask task, User user, Container container);

    Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task, User user, Container container);

}
