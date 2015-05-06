package org.labkey.workflow;

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;

import java.util.Date;
import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowTask implements Task
{
    private String _id;
    private String _taskDefinitionId;
    private String _processInstanceId;
    private String _description;
    private Map<String, Object> _taskParameters;

    public WorkflowTask() {}

    public WorkflowTask(String id, String taskDefinitionId, String processInstanceId, String documentation)
    {
        this(id, taskDefinitionId, processInstanceId, documentation, null);
    }

    public WorkflowTask(String id, String taskDefinitionId, String processInstanceId, String documentation, Map<String, Object> parameters)
    {
        _id = id;
        _taskDefinitionId = taskDefinitionId;
        _processInstanceId = processInstanceId;
        _description = documentation;
        _taskParameters = parameters;
    }


    public String getTaskDefinitionId()
    {
        return _taskDefinitionId;
    }

    public void setTaskDefinitionId(String taskDefinitionId)
    {
        _taskDefinitionId = taskDefinitionId;
    }

    @Override
    public String getId()
    {
        return _id;
    }

    @Override
    public String getName()
    {
        return null;
    }

    @Override
    public String getDescription()
    {
        return _description;
    }

    @Override
    public int getPriority()
    {
        return 0;
    }

    @Override
    public String getOwner()
    {
        return null;
    }

    @Override
    public String getAssignee()
    {
        return null;
    }

    public String getProcessInstanceId()
    {
        return _processInstanceId;
    }

    @Override
    public String getExecutionId()
    {
        return null;
    }

    @Override
    public String getProcessDefinitionId()
    {
        return null;
    }

    @Override
    public Date getCreateTime()
    {
        return null;
    }

    @Override
    public String getTaskDefinitionKey()
    {
        return null;
    }

    @Override
    public Date getDueDate()
    {
        return null;
    }

    @Override
    public String getCategory()
    {
        return null;
    }

    @Override
    public String getParentTaskId()
    {
        return null;
    }

    @Override
    public String getTenantId()
    {
        return null;
    }

    @Override
    public String getFormKey()
    {
        return null;
    }

    @Override
    public Map<String, Object> getTaskLocalVariables()
    {
        return null;
    }

    @Override
    public Map<String, Object> getProcessVariables()
    {
        return null;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }

    public Map<String, Object> getTaskParameters()
    {
        return _taskParameters;
    }

    public void setTaskParameters(Map<String, Object> taskParameters)
    {
        this._taskParameters = taskParameters;
    }

    @Override
    public void setName(String name)
    {

    }

    @Override
    public void setDescription(String description)
    {
        _description = description;
    }

    @Override
    public void setPriority(int priority)
    {

    }

    @Override
    public void setOwner(String owner)
    {

    }

    @Override
    public void setAssignee(String assignee)
    {

    }

    @Override
    public DelegationState getDelegationState()
    {
        return null;
    }

    @Override
    public void setDelegationState(DelegationState delegationState)
    {

    }

    @Override
    public void setDueDate(Date dueDate)
    {

    }

    @Override
    public void setCategory(String category)
    {

    }

    @Override
    public void delegate(String userId)
    {
    }

    @Override
    public void setParentTaskId(String parentTaskId)
    {

    }

    @Override
    public void setTenantId(String tenantId)
    {

    }

    @Override
    public void setFormKey(String formKey)
    {

    }

    @Override
    public boolean isSuspended()
    {
        return false;
    }
}
