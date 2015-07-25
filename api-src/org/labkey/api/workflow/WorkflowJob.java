package org.labkey.api.workflow;

import java.util.Date;

/**
 * Created by susanh on 7/23/15.
 */
public interface WorkflowJob
{
    Date getDueDate();

    String getId();

    String getProcessInstanceId();

    String getExecutionId();

    String getProcessDefinitionId();

    int getRetries();

    String getExceptionMessage();

    String getContainerId();
}
