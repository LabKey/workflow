/*
 * Copyright (c) 2015-2017 LabKey Corporation
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
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.QueryView;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.User;
import org.labkey.api.services.ServiceRegistry;
import org.labkey.api.view.ViewContext;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service for accessing workflow objects including WorkflowTasks and WorkflowProcesses
 * Created by susanh on 11/5/15.
 */
public interface WorkflowService
{
    String TASK_KEY = "taskKey"; // the key/name for the column containing the current task description, also an indicator for filtering on a particular type of task.

    static WorkflowService get()
    {
        return ServiceRegistry.get().getService(WorkflowService.class);
    }

    static void setInstance(WorkflowService impl)
    {
        ServiceRegistry.get().registerService(WorkflowService.class, impl);
    }

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
     * Sends the message indicated by the message name to the provided executionId
     * setting the processVariables as provided.
     * @param messageName - name of the message to send
     * @param executionId - identifier for the execution receiving the message (the id of the execution to deliver the message to)
     * @param processVariables - variables that represent the payload of this message
     */
    public void sendMessage(String messageName, String executionId, @Nullable Map<String, Object> processVariables);

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
    @Nullable
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
     * Retrieves the count of the tasks associated with a particular processDefinitionKey that are assigned to a particular user
     * @param processDefinitionKey identifier for the process definition
     * @param filter a filter over the task variables for the assigned tasks
     * @param assignee the user the tasks are assigned to
     * @param container the container in which the tasks are defined, null for all containers   @return count of the workflow tasks
     * */
    long getTaskCount(@NotNull String processDefinitionKey, @Nullable SimpleFilter filter, @Nullable User assignee, @Nullable Container container);

    /**
     * Creates a column containing the assignee with a given key for a process instance identified by a process variable of a given name
     * @param tableInfo the table the column will be attached to
     * @param colName name to give the column being created
     * @param assigneeKey the name of the process variable that contains the assigneeId (as an integer)
     * @param identifierKey the name of the variable that has the identifier for the workflow object in this table
     * @param identifierColName
     * @param user the current user
     * @param container the container context for this table   @return column that will display the assignee
     */
    ColumnInfo getAssigneeColumn(TableInfo tableInfo, final String colName, String assigneeKey, String identifierKey, String identifierColName, User user, Container container);

    /**
     * Creates a column with the current taskType (task definition key) for a given identifier variable.  Note that this
     * will not work if there is more than one active task for a particular process instance and it currently assumes the
     * identifier is a long_ value.
     * @param tableInfo the table to which the column will be added
     * @param colLabel the name to be given to the column
     * @param identifierVarName the name of the process variable that contains the identifier
     * @param identifierColumnName the name of the column in the table that contains the identifier
     * @return a column with the id of the current task.
     */
    ColumnInfo getTaskTypeColumn(TableInfo tableInfo, final String colLabel, String identifierVarName, String identifierColumnName);


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
     * Count of the number of process instances, both active an inactive, but not including deleted ones, in the given container for a given definiiton key
     * @param processDefinitionKey identifier of the process definition
     * @param container container of context, or null for all containers
     * @return number of active and inactive process instances, excluding deleted instances
     */
    @NotNull
    public Long getProcessInstanceCount(String processDefinitionKey, @Nullable Container container);

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
     * Provides a query view of the task list for a given context
     * @param context the context of the query view
     * @param filter
     * @return a query view or null if one cannot be shown
     */
    @Nullable
    QueryView getTaskListQueryView(ViewContext context, SimpleFilter filter);

    /**
     * Remove the process instance whose id is supplied, logging the reason for the deletion if provided
     * @param processInstanceId id of the process instance to be removed
     * @param reason reason for deletion (may be null)
     */
    void deleteProcessInstance(@NotNull String processInstanceId, @Nullable String reason);

    /**
     * Deletes an active process instance given a key and value that uniquely identify it within a particular container.
     * Historic process instances with no active counterparts will not be deleted.
     * @param key the key for the variable identifier
     * @param value the value for the variable identifier
     * @param reason (optional) reason for deletion
     * @param container the container context
     * @throws Exception if there are problems retrieving the process instance using the given variable data
     */
    void deleteProcessInstance(String key, String value, @Nullable String reason, @NotNull Container container) throws RuntimeException;

    /**
     * Deletes active process instances given a key and set of values value that each uniquely identify a process within a particular container.
     * Historic process instances with no active counterparts will not be deleted.
     * @param key the key for the variable identifier
     * @param values the set of values for the given variable identifier that identify the process instance to be deleted
     * @param reason (optional) reason for deletion
     * @param container the container context
     * @throws Exception if there are problems retrieving the process instance using the given variable data
     */
    void deleteProcessInstances(String key, List<Object> values, @Nullable String reason, @NotNull Container container) throws RuntimeException;


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

    String deployWorkflow(String moduleName, Resource modelResource, @Nullable Container container) throws FileNotFoundException;

    void deleteWorkflow(@NotNull String deploymentId);
}
