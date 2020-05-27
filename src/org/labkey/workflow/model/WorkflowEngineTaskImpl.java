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

import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.Task;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.workflow.WorkflowManager;

import java.util.Date;
import java.util.List;

/**
 * Created by cnathe on 9/25/2015.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowEngineTaskImpl extends WorkflowTaskImpl
{
    private Task _engineTask;

    public WorkflowEngineTaskImpl(String taskId, Container container)
    {
        this(WorkflowManager.get().getEngineTask(taskId, container));
        _id = taskId;
    }

    public WorkflowEngineTaskImpl(Task engineTask)
    {
        _engineTask = engineTask;
        _taskInfo = _engineTask;
    }


    @Override
    public void setName(String name)
    {
        _engineTask.setName(name);
    }

    @Override
    public void setDescription(String description)
    {
        _engineTask.setDescription(description);
    }

    /**
     * Set the user responsible for full completion of the task, which may include review of results
     * if the task has been delegated
     * @param owner
     */
    @Override
    public void setOwner(User owner)
    {
        _engineTask.setOwner(String.valueOf(owner.getUserId()));
    }

    /**
     * Set the user who is currently assigned to work on this task
     * @param assignee
     */
    @Override
    public void setAssignee(User assignee)
    {
        _engineTask.setOwner(String.valueOf(assignee.getUserId()));
    }

    @Override
    public boolean isDelegated()
    {
        return _engineTask.getDelegationState() == DelegationState.PENDING;
    }

    @Override
    public boolean isReadyForReview()
    {
        return _engineTask.getDelegationState() == DelegationState.RESOLVED;
    }

    public void setReadyForReview()
    {
        _engineTask.setDelegationState(DelegationState.RESOLVED);
    }

    @Override
    public void setDueDate(Date dueDate)
    {
        _engineTask.setDueDate(dueDate);
    }

    @Override
    public void delegate(User user)
    {
        _engineTask.delegate(String.valueOf(user.getUserId()));
    }

    public void setParentTaskId(String parentTaskId)
    {
        _engineTask.setParentTaskId(parentTaskId);
    }

    public void setContainer(String containerId)
    {
        _engineTask.setTenantId(containerId);
    }

    @Override
    public boolean isSuspended()
    {
        return _engineTask.isSuspended();
    }

    @Override
    public boolean isActive() { return _engineTask != null; }

    @Override
    public List<Integer> getGroupIds()
    {
        if (_groupIds == null)
            _groupIds = WorkflowManager.get().getCandidateGroupIds(getId());
        return _groupIds;
    }

    @Override
    public Date getEndDate()
    {
        return null;
    }
}
