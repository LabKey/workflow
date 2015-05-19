package org.labkey.workflow.model;

import org.activiti.engine.repository.ProcessDefinition;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.workflow.WorkflowManager;

import java.io.File;
import java.util.Map;

/**
 * Created by susanh on 5/14/15.
 */
public class WorkflowSummary
{
    private ProcessDefinition _engineProcessDefinition;
    private String _processDefinitionKey;
    private Long _numAssignedTasks;
    private Long _numOwnedTasks;
    private Map<UserPrincipal, Long> _numGroupTasks;
    private Long _numInstances;
    private Long _numTotalTasks;


    public WorkflowSummary()
    {}

    public WorkflowSummary(String processDefinitionKey, User user, Container container)
    {
        _processDefinitionKey = processDefinitionKey;
        _engineProcessDefinition = WorkflowManager.get().getProcessDefinition(processDefinitionKey, container);
        setSummaryCounts(user, container);
    }

    public String getName()
    {
        if (_engineProcessDefinition == null)
            return null;
        else if (_engineProcessDefinition.getName() == null)
            return "Process Definition " + _engineProcessDefinition.getId();
        else
            return _engineProcessDefinition.getName();
    }

    public String getDescription()
    {
        if (_engineProcessDefinition == null)
            return null;
        else
            return _engineProcessDefinition.getDescription();
    }

    public boolean hasDiagram()
    {
        return _engineProcessDefinition != null && _engineProcessDefinition.getResourceName() != null;
    }

    public File getModelFile()
    {
        File file = null;
        if (_engineProcessDefinition != null)
        {
            file = new File(_engineProcessDefinition.getResourceName());
        }
        return file;
    }


    public String getProcessDefinitionKey()
    {
        return _processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey)
    {
        _processDefinitionKey = processDefinitionKey;
    }

    public Long getNumInstances()
    {
        return _numInstances;
    }

    public void setNumInstances(Long numInstances)
    {
        _numInstances = numInstances;
    }

    public Long getNumAssignedTasks()
    {
        return _numAssignedTasks;
    }

    public void setNumAssignedTasks(Long numAssignedTasks)
    {
        _numAssignedTasks = numAssignedTasks;
    }

    public Map<UserPrincipal, Long> getNumGroupTasks()
    {
        return _numGroupTasks;
    }

    public void setNumGroupTasks(Map<UserPrincipal, Long> numGroupTasks)
    {
        _numGroupTasks = numGroupTasks;
    }

    public Long getNumOwnedTasks()
    {
        return _numOwnedTasks;
    }

    public void setNumOwnedTasks(Long numOwnedTasks)
    {
        _numOwnedTasks = numOwnedTasks;
    }

    public Long getNumTotalTasks()
    {
        return _numTotalTasks;
    }

    public void setNumTotalTasks(Long numTotalTasks)
    {
        _numTotalTasks = numTotalTasks;
    }

    public void setSummaryCounts(User user, Container container)
    {
        setNumAssignedTasks(WorkflowManager.get().getAssignedTaskCount(_processDefinitionKey, user, container));
        setNumOwnedTasks(WorkflowManager.get().getOwnedTaskCount(_processDefinitionKey, user, container));
        setNumGroupTasks(WorkflowManager.get().getGroupTaskCounts(_processDefinitionKey, user, container));
        setNumInstances(WorkflowManager.get().getProcessInstanceCount(_processDefinitionKey, user, container));
        setNumTotalTasks(WorkflowManager.get().getTotalTaskCount(_processDefinitionKey, user, container));
    }
}
