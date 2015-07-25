package org.labkey.workflow.model;

import org.activiti.engine.runtime.Job;
import org.labkey.api.workflow.WorkflowJob;

import java.util.Date;

/**
 * This class represents a system job (e.g., a timer) within a workflow.
 * Created by susanh on 7/23/15.
 */
public class WorkflowJobImpl implements WorkflowJob
{
    private Job _engineJob;

    public WorkflowJobImpl(Job engineJob)
    {
        _engineJob = engineJob;
    }

    @Override
    public Date getDueDate()
    {
        return _engineJob.getDuedate();
    }

    @Override
    public String getId()
    {
        return _engineJob.getId();
    }

    @Override
    public String getProcessInstanceId()
    {
        return _engineJob.getProcessInstanceId();
    }

    @Override
    public String getExecutionId()
    {
        return _engineJob.getExecutionId();
    }

    @Override
    public String getProcessDefinitionId()
    {
        return _engineJob.getProcessDefinitionId();
    }

    @Override
    public int getRetries()
    {
        return _engineJob.getRetries();
    }

    @Override
    public String getExceptionMessage()
    {
        return _engineJob.getExceptionMessage();
    }

    @Override
    public String getContainerId()
    {
        return _engineJob.getTenantId();
    }
}
