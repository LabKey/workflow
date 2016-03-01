/*
 * Copyright (c) 2015-2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
