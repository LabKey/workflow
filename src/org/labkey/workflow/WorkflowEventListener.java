/*
 * Copyright (c) 2015 LabKey Corporation
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
package org.labkey.workflow;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;
import org.apache.log4j.Logger;

/**
 * Created by susanh on 5/9/15.
 */
public class WorkflowEventListener implements ActivitiEventListener
{
    protected static final Logger logger = Logger.getLogger(WorkflowEventListener.class);

    @Override
    public void onEvent(ActivitiEvent event)
    {
        switch (event.getType())
        {
            case ENGINE_CREATED:
                logger.debug("Engine created execution id is " + event.getExecutionId() + " processInstanceId is " + event.getProcessInstanceId() + " processDefinition id is " + event.getProcessDefinitionId());
                break;
            case TASK_CREATED:

            default:
                logger.debug("Event " + event);
        }
    }

    @Override
    public boolean isFailOnException()
    {
        return false;
    }
}
