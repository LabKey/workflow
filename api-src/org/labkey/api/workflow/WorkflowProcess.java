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
package org.labkey.api.workflow;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 6/14/15.
 *
 */
public interface WorkflowProcess
{
    String INITIATOR_ID = "initiatorId";
    String CONTAINER_ID = "container";
    String CREATED_DATE = "created";
    String PROCESS_INSTANCE_URL = "processInstanceUrl";

    String DATA_ACCESS_KEY = "dataAccess";
    String DATA_ACCESS_PARAMETERS_KEY = "parameters";

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

    List<WorkflowJob> getCurrentJobs();

    List<WorkflowTask> getCompletedTasks();

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
