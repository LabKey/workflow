package org.labkey.api.workflow;

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;

import java.util.Map;

/**
 * Created by davebradlee on 7/20/15.
 *
 */
public abstract class DataManagerConfig extends WorkflowDelegateActionBase
{
    private final Container _container;
    private final User _initiator;

    public DataManagerConfig(Map<String, Object> variables)
    {
        super(variables);
        _container = ContainerManager.getForId((String) _variables.get(WorkflowProcess.CONTAINER_ID));
        _initiator = UserManager.getUser(Integer.valueOf((String) _variables.get(WorkflowProcess.INITIATOR_ID)));
    }

    public abstract void doAction() throws Exception;

    public Container getContainer()
    {
        return _container;
    }

    public User getInitiator()
    {
        return _initiator;
    }

}
