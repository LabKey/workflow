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
package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 11/5/15.
 */
public interface WorkflowService
{
    /**
     * Gets the list of candidate group ids for a task given its id, or an empty list if there are no candidate groups
     * @param taskId id of the task to get groups for
     * @return list of candidate group ids.
     */
    @NotNull
    List<Integer> getCandidateGroupIds(@NotNull String taskId);

    /**
     * Retrieve a task given its id
     * @param taskId id of the task to retrieve
     * @param container container in which the task is defined
     * @return a workflow task of the given Id.  If there is no such task, an exception is thrown.
     */
    WorkflowTask getTask(@NotNull String taskId, @Nullable Container container);

    /**
     * Completes a task in a workflow given the id of the task
     * @param taskId the id of an active task
     * @param user the user completing the task
     * @param container the container in which the task is being completed
     * @throws Exception when no task exists with the given id or the tasks is pending delegation
     */
    void completeTask(@NotNull String taskId, User user, Container container) throws Exception;


    /**
     * Claim a task that one (or more) of the user's group is currently a candidate group for
     * @param taskId id of the task to be claimed
     * @param userId id of the user who is claiming the task
     * @param container the container in which the task is being handled
     * @throws Exception if user
     */
    void claimTask(@NotNull String taskId, @NotNull Integer userId, Container container) throws Exception;

    /**
     * Assign a particular task to a user given the id of the user
     * @param taskId id of the task to be assigned
     * @param assigneeId id of the user to whom the task should be assigned
     * @param user principal doing the assignment
     * @param container container context for this assignment
     * @throws Exception
     */
    void assignTask(@NotNull String taskId, @NotNull Integer assigneeId, User user, Container container) throws Exception;

    /**
     * Delegate a task to a particular task (retaining ownership of the task for later review of results)
     * @param taskId id of the task to be delegated
     * @param user principal doing the delegation
     * @param designateeId id of the user to delegate to
     * @param container context in which the delegation is being made
     * @throws Exception
     */
    void delegateTask(@NotNull String taskId, @NotNull User user, @NotNull Integer designateeId, Container container) throws Exception;

    /**
     * Creates a new process instance for the given workflow and returns the id for this new instance.
     * @param moduleName - the name of the module in which the process definition key is defined
     * @param processDefinitionKey - the unique key for this process definition
     * @param name - the human-readable name for the process
     * @param processVariables - the set of variables to associate with this process instance (should contain at least the INITIATOR_ID variable)
     * @param container the container in which this process is being created
     * @return the id of the new process instance for this workflow
     */
    String startWorkflow(@NotNull String moduleName, @NotNull String processDefinitionKey, @Nullable String name, @NotNull Map<String, Object> processVariables, @Nullable Container container) throws FileNotFoundException;

    /**
     * Creates a new process instance for a given workflow starting at the message start event provided
     * @param moduleName - name of the module in which the workflow is defined
     * @param processDefinitionKey - the unique key for this process definition (also the prefix of the bpmn.xml file)
     * @param processVariables - the set of variables to associate with this process instance (should contain at least the INITIATOR_ID variable)
     * @param container - the container in which this process is being created
     * @param startMessage - the id of the message element defined for the start event
     * @return id of the process instance created
     * @throws FileNotFoundException if the bpmn.xml file that defines the process does not exist and it is necessary to deploy a new instance of this model in this container
     */
    String startWorkflow(@NotNull String moduleName, @NotNull String processDefinitionKey, @NotNull Map<String, Object> processVariables, @NotNull Container container, @NotNull String startMessage) throws FileNotFoundException;

    /**
     * Find the workflow process corresponding to the given process instance id.  It will find either an acitve
     * or inactive process instance.
     * @param processInstanceId identifier for the process instance to retrieve.
     * @return null if no such process instance exists
     */
    WorkflowProcess getWorkflowProcess(@NotNull String processInstanceId);

    /**
     * Given a key and value that correspond to a process variable for a set of processes within a container, finds the
     * workflow process with that process variable value that was started last.  This can be used for variables of
     * type string as well as integer.
     * @param key - the process variable name
     * @param value - the string value for the unique identifier for the process instance
     * @param container - the container context
     * @return the workflow identified by the given key and value
     * @throws Exception if the key-value pair does not uniquely identify a single latest workflow process
     */
    WorkflowProcess getWorkflowProcessForVariable(String key, String value, Container container) throws Exception;

    /**
     * Gets the list of jobs taht are currently active for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instance is active
     * @return list of workflow jobs, or an empty list of there are none
     */
    List<WorkflowJob> getCurrentProcessJobs(@NotNull String processInstanceId, @Nullable Container container);


    /**
     * Gets the list of tasks that are currently active for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instance is active
     * @return list of workflow tasks, or an empty list of there are none
     */
    @NotNull
    List<WorkflowTask> getCurrentProcessTasks(@NotNull String processInstanceId, @Nullable Container container);

    /**
     * Gets the list of completed tasks for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instance is active
     * @return list of workflow tasks, or an empty list of there are none
     */
    @NotNull
    List<WorkflowTask> getCompletedProcessTasks(@NotNull String processInstanceId, @Nullable Container container);

    /**
     * Count of the number of process instances in the current container that were initiated by the given user,
     * or by any user if this user has administrative permissions.  If the container is null, all process instances
     * for the given definition key will be returned.
     * @param processDefinitionKey identifier for the process definition
     * @param user user making the request
     * @param container container of context, or null for all containers
     * @return number of process instances
     */
    @NotNull
    Long getProcessInstanceCount(String processDefinitionKey, @NotNull User user, @Nullable Container container);

    /**
     * Returns the set of variables associated with the given historicProcessInstance
     * @param processInstanceId id of the historic process instance in question
     * @return the set of process instance variables for this historic process instance
     */
    Map<String, Object> getHistoricProcessInstanceVariables(@NotNull String processInstanceId);

    /**
     * Given the id of a particular task, will update the process instance variables (not the task variables)
     * for the instance that contains this task with the given variables.  New variables will be added; existing
     * variables will be replaced.  Task variables are left alone.
     * @param taskId -
     *               id of the task whose process variables should be updated
     * @param variables -
     *                  variables that will be merged into the existing set of variables
     *
     */
    void updateProcessVariables(@NotNull String taskId, @Nullable Map<String, Object> variables);

    /**
     * Given the id of a particular task, will update the variables for this task (not the process instance)
     * New variables will be added; existing variables will be replaced.  Process variables are left alone.
     * @param taskId -
     *               id of the task whose variables should be updated
     * @param variables -
     *                  variables that will be merged into the existing set of variables
     *
     */
     void updateTaskVariables(@NotNull String taskId, @Nullable Map<String, Object> variables);

    /**
     * Remove the process instance whose id is supplied, logging the reason for the deletion if provided
     * @param processInstanceId id of the process instance to be removed
     * @param reason reason for deletion (may be null)
     */
    void deleteProcessInstance(@NotNull String processInstanceId, @Nullable String reason);

    /**
     * Returns the set of variables associated with the given processInstance as well as a list of the current active tasks for that instance
     * @param processInstanceId id of the process instance in question
     * @return the set of process instance variables for this process instance
     */
    Map<String, Object> getProcessInstanceVariables(@NotNull String processInstanceId);

    /**
     * Returns the set of variables associated with a given task.
     * Does not include any variables associated with the corresponding process instance.
     * @param taskId id of the task in question
     * @return  the set of variables associated with this task, or an empty map if there are none
     */
    Map<String, Object> getTaskVariables(@NotNull String taskId);

    /**
     * @param processDefinitionId unique id of a process definition
     * @param container container in which the process is defined
     * @return name of the module that this process definition comes from
     */
    String getProcessDefinitionModule(@NotNull String processDefinitionId, Container container);


    Map<String, String> getProcessDefinitionNames(@Nullable Container container);

    Map<String, TaskFormField> getStartFormFields(String processDefinitionKey);

    Map<String, TaskFormField> getFormFields(String taskId);

    String getProcessDefinitionKey(@NotNull String processDefinitionId);

    InputStream getProcessDiagram(@NotNull String processInstanceId);

    InputStream getProcessDiagramByKey(@NotNull String processDefinitionKey, @Nullable Container container);

    void deleteDeployments(@NotNull Container container);

    String deployWorkflow(@NotNull File modelFile, @Nullable Container container) throws FileNotFoundException;

    void deleteWorkflow(@NotNull String deploymentId);
}
