package org.labkey.workflow;

import org.activiti.engine.runtime.ProcessInstance;
import org.labkey.api.action.HasViewContext;
import org.labkey.api.view.ViewContext;

import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
public class WorkflowProcess implements HasViewContext
{
    private String _processKey;
    private String _id;
    private Map<String, Object> _processVariables;
    private int _requesterId;
    private String _processInstanceId;
    private ViewContext _context;

    public String getId()
    {
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

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


    @Override
    public void setViewContext(ViewContext context)
    {
        _context = context;
    }

    @Override
    public ViewContext getViewContext()
    {
        return _context;
    }

    public int getRequesterId()
    {
        return _requesterId;
    }

    public void setRequesterId(int requesterId)
    {
        _requesterId = requesterId;
    }

    public String getProcessInstanceId()
    {
        return _processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }
}
