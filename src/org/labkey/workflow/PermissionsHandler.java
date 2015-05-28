package org.labkey.workflow;

import org.labkey.api.data.Container;
import org.labkey.api.exp.Handler;
import org.labkey.api.security.SecurableResource;
import org.labkey.api.security.User;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;

/**
 * Created by susanh on 5/27/15.
 */
public interface PermissionsHandler
{
    // TODO perhaps better to have a method getPermissions(task, user, container) if we define own Permission classes for workflow

    public boolean canView(WorkflowProcess process, User user, Container container);

    public boolean canAccessData(WorkflowProcess process, User user, Container container);

    public boolean canDelete(WorkflowProcess process, User user, Container container);

    public boolean canClaim(WorkflowTask task, User user, Container container);

    public boolean canDelegate(WorkflowTask task, User user, Container container);

    public boolean canAssign(WorkflowTask task, User user, Container container);

    public boolean canView(WorkflowTask task, User user, Container container);

    public boolean canAccessData(WorkflowTask task, User user, Container container);

    public boolean isAssigned(WorkflowTask task, User user);

    public boolean canComplete(WorkflowTask task, User user, Container container);
}
