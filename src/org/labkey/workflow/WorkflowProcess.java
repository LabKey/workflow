package org.labkey.workflow;

import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
public class WorkflowProcess
{
    private String _processKey;
    private Map<String, Object> _processVariables;

    public String getProcessKey()
    {
        return _processKey;
    }

    public void setProcessKey(String processKey)
    {
        _processKey = processKey;
    }

    public Map<String, Object> getProcessVariables()
    {
        return _processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables)
    {
        _processVariables = processVariables;
    }
}
