package org.labkey.workflow.view;

import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;

import java.util.List;

/**
 * Created by susanh on 4/29/15.
 */
public class ProcessSummaryBean
{
    private long _numDefinitions;
    private List<Task> _assignedTasks;
    private List<Task> _groupTasks;
    private List<ProcessInstance> _instances;
    private String _feedback;
    private String _currentProcessKey;

    public String getCurrentProcessKey()
    {
        return _currentProcessKey;
    }

    public void setCurrentProcessKey(String currentProcessKey)
    {
        _currentProcessKey = currentProcessKey;
    }

    public String getFeedback()
    {
        return _feedback;
    }

    public void setFeedback(String feedback)
    {
        _feedback = feedback;
    }

    public long getNumDefinitions()
    {
        return _numDefinitions;
    }

    public void setNumDefinitions(long numDefinitions)
    {
        _numDefinitions = numDefinitions;
    }

    public List<ProcessInstance> getInstances()
    {
        return _instances;
    }

    public void setInstances(List<ProcessInstance> instances)
    {
        _instances = instances;
    }

    public List<Task> getAssignedTasks()
    {
        return _assignedTasks;
    }

    public void setAssignedTasks(List<Task> tasks)
    {
        _assignedTasks = tasks;
    }

    public List<Task> getGroupTasks()
    {
        return _groupTasks;
    }

    public void setGroupTasks(List<Task> groupTasks)
    {
        _groupTasks = groupTasks;
    }
}
