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
                Task task = WorkflowManager.get().getTask(event.getExecutionId());
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
