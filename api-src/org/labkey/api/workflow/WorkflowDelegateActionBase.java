package org.labkey.api.workflow;

import java.util.Map;

/**
 * Created by susanh on 8/2/15.
 */
public class WorkflowDelegateActionBase
{
    protected final Map<String, Object> _variables;

    public WorkflowDelegateActionBase(Map<String, Object> variables)
    {
        _variables = variables;
    }

    public Map<String, Object> getVariables()
    {
        return _variables;
    }

    public Object getVariable(String key)
    {
        return _variables.get(key);
    }

    public void setVariable(String key, Object value)
    {
        _variables.put(key, value);
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
