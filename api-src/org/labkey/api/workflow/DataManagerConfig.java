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
public abstract class DataManagerConfig
{
    protected final Map<String, Object> _variables;
    private final Container _container;
    private final User _initiator;

    public DataManagerConfig(Map<String, Object> variables)
    {
        _variables = variables;
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

    public Object getVariable(String key)
    {
        return _variables.get(key);
    }

    public void setVariable(String key, Object value)
    {
        _variables.put(key, value);
    }

    public Map<String, Object> getVariables()
    {
        return _variables;
    }

    @SuppressWarnings("unchecked")
    public Object getDataAccessParameter(String key)
    {
        Object dataAccess = _variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                return ((Map<String, Object>)parameters).get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void setDataAccessParameter(String key, Object value)
    {
        Object dataAccess = _variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                ((Map<String, Object>)parameters).put(key, value);
        }
    }
}
