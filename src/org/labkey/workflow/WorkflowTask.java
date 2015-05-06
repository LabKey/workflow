package org.labkey.workflow;

import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.security.User;

import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowTask
{
    private String _taskId;
    private String _taskDefinitionId;
    private String _processInstanceId;
    private String _documentation;
    private Map<String, Object> taskParameters;

    public String getTaskId()
    {
        return _taskId;
    }

    public void setTaskId(String taskId)
    {
        _taskId = taskId;
    }

    public String getTaskDefinitionId()
    {
        return _taskDefinitionId;
    }

    public void setTaskDefinitionId(String taskDefinitionId)
    {
        _taskDefinitionId = taskDefinitionId;
    }

    public String getProcessInstanceId()
    {
        return _processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }

    public String getDocumentation()
    {
        return _documentation;
    }

    public void setDocumentation(String documentation)
    {
        _documentation = documentation;
    }


    public Map<String, Object> getTaskParameters()
    {
        return taskParameters;
    }

    public void setTaskParameters(Map<String, Object> taskParameters)
    {
        this.taskParameters = taskParameters;
    }
}
