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

import org.activiti.engine.history.HistoricTaskInstance;
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
public class WorkflowHistoricTaskImpl extends WorkflowTaskImpl
{
    private HistoricTaskInstance _historicTask;

    public WorkflowHistoricTaskImpl(String taskId, Container container)
    {
        this(WorkflowManager.get().getHistoricTask(taskId, container));
        _id = taskId;
    }

    public WorkflowHistoricTaskImpl(HistoricTaskInstance historicTask)
    {
        _historicTask = historicTask;
        _taskInfo = historicTask;
    }


    // Don't do anything for set methods for a HistoricTaskInstance
    @Override
    public void setName(String name)
    {}
    @Override
    public void setDescription(String description)
    {}
    @Override
    public void setOwner(User owner)
    {}
    @Override
    public void setAssignee(User assignee)
    {}
    public void setReadyForReview()
    {}
    @Override
    public void setDueDate(Date dueDate)
    {}
    public void setParentTaskId(String parentTaskId)
    {}
    public void setContainer(String containerId)
    {}
    @Override
    public void delegate(User user)
    {}

    @Override
    public boolean isDelegated()
    {
        return false;
    }

    @Override
    public boolean isReadyForReview()
    {
        return false;
    }

    @Override
    public boolean isSuspended()
    {
        return false;
    }

    @Override
    public boolean isActive() { return false; }

    @Override
    public List<Integer> getGroupIds()
    {
        return null;
    }

    @Override
    public Date getEndDate()
    {
        return _historicTask.getEndTime();
    }
}
