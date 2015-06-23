package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 6/14/15.
 */
public interface WorkflowProcess
{
    String INITIATOR_ID = "initiatorId";
    String CONTAINER_ID = "container";
    String CREATED_DATE = "created";
    String PROCESS_INSTANCE_URL = "processInstanceUrl";

    String getId();

    String getProcessDefinitionKey();

    void setProcessDefinitionKey(String processKey);

    String getProcessDefinitionName();

    String getProcessDefinitionModule();

    @Nullable
    Map<String, Object> getProcessVariables();

    @Nullable
    Map<String, Object> getVariables();

    Integer getInitiatorId();

    String getProcessInstanceId();

    String getName();

    User getInitiator();

    List<WorkflowTask> getCurrentTasks();

    boolean canAccessData(User user, Container container);

    boolean canView(User user, Container container);

    boolean canDelete(User user, Container container);

    boolean canDeploy(User user, Container conatiner);

    boolean hasDiagram(Container container);

    boolean isActive();

    boolean isDeployed(Container container);

    @Nullable
    Map<String, TaskFormField> getStartFormFields(Container container);
}
