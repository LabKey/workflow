package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: tgaluhn
 * Date: 1/25/2016
 */
public abstract class SystemTaskRunner extends WorkflowDelegateActionBase
{
    private final Container _container;
    private final User _initiator;

    public SystemTaskRunner(Map<String, Object> variables)
    {
        super(variables);
        _container = ContainerManager.getForId((String) _variables.get(WorkflowProcess.CONTAINER_ID));
        _initiator = UserManager.getUser(Integer.valueOf((String) _variables.get(WorkflowProcess.INITIATOR_ID)));
    }

    public abstract void doAction() throws Exception;

    @NotNull
    protected List<User> getUsers(Set<Class<? extends Permission>> permissions)
    {
        return _container != null ? SecurityManager.getUsersWithPermissions(_container, permissions) : Collections.emptyList();
    }

    public Container getContainer()
    {
        return _container;
    }

    public User getInitiator()
    {
        return _initiator;
    }
}
