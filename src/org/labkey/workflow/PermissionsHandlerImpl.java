package org.labkey.workflow;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;

import java.util.Arrays;
import java.util.List;

/**
 * Created by susanh on 5/27/15.
 */
public class PermissionsHandlerImpl implements PermissionsHandler
{

    @Override
    public boolean canView(WorkflowProcess process, User user, Container container)
    {
        return (process.getInitiatorId() == user.getUserId()) || container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canAccessData(WorkflowProcess process, User user, Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canDelete(WorkflowProcess process, User user, Container container)
    {
        return process.getInitiatorId() == user.getUserId() || container.hasPermission(user, AdminPermission.class);
    }

    @Override
    public boolean canClaim(@NotNull WorkflowTask task, @NotNull User user, @Nullable Container container)
    {
        List<Integer> candidateGroupIds = WorkflowManager.get().getCandidateGroupIds(task.getId());
        return container.hasPermission(user, AdminPermission.class) || candidateGroupIds.isEmpty() || CollectionUtils.containsAny(candidateGroupIds, Arrays.asList(ArrayUtils.toObject(user.getGroups())));
    }

    public boolean canDelegate(@Nullable WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
    }

    public boolean canAssign(@Nullable WorkflowTask task, @NotNull User user, @NotNull Container container)
    {
        return container.hasPermission(user, AdminPermission.class);
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
}
