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

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiObjectNotFoundException;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.Group;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class WorkflowManager
{
    private static final WorkflowManager _instance = new WorkflowManager();
    private ProcessEngine _processEngine = null;
    private static final String ACTIVITI_CONFIG_FILE = "resources/workflow/config/activiti.cfg.xml";
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";
    private static final String WORKFLOW_MODEL_DIR = "resources/workflow/model";

    public enum TaskInvolvement {ASSIGNED, GROUP_TASK, DELEGATION_OWNER}

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    public List<Integer> getCandidateGroupIds(@NotNull String taskId)
    {

        List<Integer> groupIds = new ArrayList<>();
        Task task = getTask(taskId);
        List<IdentityLink> links = getRuntimeService().getIdentityLinksForProcessInstance(task.getProcessInstanceId());
        for (IdentityLink  link : links)
        {
            if (link.getGroupId() != null)
            {
                groupIds.add(Integer.valueOf(link.getGroupId()));
            }
        }
        return groupIds;
    }

    /**
     * Gets all the tasks that are associated with the given user with a particular level of involvement
     * @param user
     * @param container
     * @param involvements
     * @return
     */
    public List<Task> getTaskList(@NotNull UserPrincipal user, @NotNull Container container, @NotNull Set<TaskInvolvement> involvements)
    {
        List<Task> tasks  = new ArrayList<>();
        if (involvements.contains(TaskInvolvement.ASSIGNED))
            tasks.addAll(getAssignedTaskList(user, container));
        if (involvements.contains(TaskInvolvement.DELEGATION_OWNER))
            tasks.addAll(getOwnedTaskList(user, container));
        if (involvements.contains(TaskInvolvement.GROUP_TASK))
        {
            Map<UserPrincipal, List<Task>> groupTasks = getGroupTasks(user, container);
            for (List<Task> groupList : groupTasks.values())
            {
                tasks.addAll(groupList);
            }
        }
        return tasks;
    }

    /**
     * Gets the total number of tasks currently active for the given processDefinition, user and container
     * @param processDefinitionKey
     * @param user
     * @param container
     * @return count of the number of tasks
     */
    public Long getTotalTaskCount(@NotNull String processDefinitionKey, @NotNull UserPrincipal user, @NotNull Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'")
                .append(" AND T.tenant_id_ = #{containerId} ");
        return getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(user.getUserId()))
                .parameter("containerId", String.valueOf(container.getId()))
                .count();
    }

    /**
     * Retrieve the list of tasks currently assigned to a given user in the given container
     * @param user the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public List<Task> getAssignedTaskList(@NotNull UserPrincipal user, @NotNull Container container)
    {
        return getTaskService().createNativeTaskQuery()
                .sql("SELECT * FROM " + getManagementService().getTableName(Task.class) + " T WHERE T.assignee_ = #{userId} AND T.tenant_id_ = #{containerId}")
                .parameter("userId", String.valueOf(user.getUserId()))
                .parameter("containerId", String.valueOf(container.getId()))
                .list();
    }

    /**
     * Retrieve the count of the list of tasks currently assigned to a given user in the given container
     * @param user the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public Long getAssignedTaskCount(@NotNull String processDefinitionKey, @NotNull UserPrincipal user, @NotNull Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'")
                .append(" AND T.assignee_ = #{userId}")
                .append(" AND T.tenant_id_ = #{containerId} ");
        return getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(user.getUserId()))
                .parameter("containerId", String.valueOf(container.getId()))
                .count();
    }

    /**
     * Retrieve the list of tasks currently owned by a given user in the given container
     * @param principal the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public List<Task> getOwnedTaskList(@NotNull UserPrincipal principal, @NotNull Container container)
    {
        return getTaskService().createNativeTaskQuery()
                .sql("SELECT * FROM " + getManagementService().getTableName(Task.class) + " T WHERE T.owner_ = #{userId} AND T.tenant_id_ = #{containerId}")
                .parameter("userId", String.valueOf(principal.getUserId()))
                .parameter("containerId", String.valueOf(container.getId()))
                .list();
    }

    /**
     * Retrieve the count of the list of tasks currently assigned to a given user in the given container
     *
     * @param processDefinitionKey
     * @param principal the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public Long getOwnedTaskCount(String processDefinitionKey, @NotNull UserPrincipal principal, @NotNull Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'")
                .append(" AND T.owner_ = #{userId}")
                .append(" AND T.tenant_id_ = #{containerId} ");
        return getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(principal.getUserId()))
                .parameter("containerId", String.valueOf(container.getId()))
                .count();
    }

    /**
     * Find the list of tasks for which the given group is a candidate assignee
     * @param group the group in question
     * @return a list of workflow tasks currently having this group as a candidate assignee
     */
    public List<Task> getTaskList(@NotNull Group group)
    {
        return getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(group.getUserId())).list();
    }

    /**
     * Adds a group as a candidate for a particular task
     * @param task the task that is to have its list of candidate groups expanded
     * @param group the group to add as a candidate.  If the group is already a candidate ???it will be added again???
     */
    public void addGroupAssignment(@NotNull Task task, @NotNull Group group)
    {
        getTaskService().addCandidateGroup(task.getId(), String.valueOf(group.getUserId()));
    }

    /**
     * @param principal the principal whose groups are being queried
     * @param container the container context for the query
     * @return a mapping between user principals and task lists for the groups the given principal is a member of
     */
    @NotNull
    public Map<UserPrincipal, List<Task>> getGroupTasks(@NotNull UserPrincipal principal, @NotNull Container container)
    {
        Map<UserPrincipal, List<Task>> tasks = new HashMap<>();
        for (int groupId : principal.getGroups())
        {
            List<Task> groupTasks = getTaskService().createTaskQuery().taskTenantId(container.getId()).taskCandidateGroup(String.valueOf(groupId)).list();
            Group group = SecurityManager.getGroup(groupId);
            if (groupTasks.size() > 0 && group != null)
            {
                tasks.put(group, groupTasks);
            }
        }

        return tasks;
    }

    /**
     * @param processDefinitionKey
     * @param principal the principal whose groups are being queried
     * @param container the container context for the query
     * @return a mapping between user principals and counts of tasks for the groups the given principal is a member of, excluding groups with 0 tasks
     */
    @NotNull
    public Map<UserPrincipal, Long> getGroupTaskCounts(String processDefinitionKey, @NotNull UserPrincipal principal, @NotNull Container container)
    {
        Map<UserPrincipal, Long> tasks = new HashMap<>();
        for (int groupId : principal.getGroups())
        {
            Long count = getTaskService().createTaskQuery().taskTenantId(container.getId()).processDefinitionKey(processDefinitionKey).taskCandidateGroup(String.valueOf(groupId)).count();
            Group group = SecurityManager.getGroup(groupId);
            if (group != null && count > 0)
            {
                tasks.put(group, count);
            }
        }

        return tasks;
    }

    /**
     * Retrieve a task given its id
     * @param taskId
     * @return a workflow task of the given Id.  If there is no such task, an exception is thrown.
     */
    public Task getTask(@NotNull String taskId)
    {
        return getTaskService().createTaskQuery().taskId(taskId).includeTaskLocalVariables().includeProcessVariables().singleResult();
    }

    /**
     * Completes a task in a workflow given the id of the task
     * @param taskId the id of an active task
     * @throws ActivitiObjectNotFoundException when no task exists with the given id.
     * @throws ActivitiException when this task is pending delegation.
     */
    public void completeTask(@NotNull String taskId)
    {
        WorkflowManager.get().getTaskService().complete(taskId);
    }

    /**
     * Claim a task that one (or more) of the user's group is currently a candidate group for
     * @param taskId id of the task to be claimed
     * @param userId id of the user who is claiming the task
     * @throws
     */
    public void claimTask(@NotNull String taskId, @NotNull Integer userId) throws Exception
    {
        if (userId != null)
        {
            getTaskService().setOwner(taskId, String.valueOf(userId));
            getTaskService().claim(taskId, String.valueOf(userId));
        }
        else
        {
            throw new Exception("No user specified");
        }
    }

    public void assignTask(@NotNull String taskId, @NotNull Integer userId) throws Exception
    {
        if (userId != null)
        {
            getTaskService().setOwner(taskId, String.valueOf(userId));
            getTaskService().setAssignee(taskId, String.valueOf(userId));
        }
        else
        {
            throw new Exception("No user specified");
        }
    }

    public void delegateTask(@NotNull String taskId, @NotNull Integer ownerId, @NotNull Integer designateeId) throws Exception
    {
        if (ownerId != null && designateeId != null)
        {
            getTaskService().delegateTask(taskId, String.valueOf(designateeId));
            getTaskService().setOwner(taskId, String.valueOf(ownerId));
        }
        else
        {
            throw new Exception("Owner or assignee not specified");
        }
    }

    /**
     * Creates a new process instance for the given workflow and returns the id for this new instance.
     * @param workflow the workflow for which an instance is requested
     * @param container the container in which this process is being created
     * @return the id of the new process instance for this workflow
     */
    public String startWorkflow(@NotNull WorkflowProcess workflow, @NotNull Container container)
    {
        ProcessInstanceBuilder builder = getRuntimeService().createProcessInstanceBuilder().processDefinitionKey(workflow.getProcessDefinitionKey()).tenantId(container.getId());
        if (workflow.getName() != null) {
            builder.processInstanceName(workflow.getName());
        }
        for (Map.Entry<String, Object> variable : workflow.getProcessVariables().entrySet())
        {
            builder.addVariable(variable.getKey(), variable.getValue());
        }
        ProcessInstance instance = builder.start();
        return instance.getId();
    }

    public List<Task> getCurrentProcessTasks(@NotNull String processInstanceId)
    {
        return getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
    }

    public List<WorkflowTask> getCurrentProcessTasks(@NotNull String processInstanceId, @NotNull User user, @NotNull Container container)
    {
        List<Task> engineTasks = getTaskService().createTaskQuery().processInstanceId(processInstanceId).taskTenantId(container.getId()).list();
        List<WorkflowTask> tasks = new ArrayList<>();
        for (Task engineTask : engineTasks)
        {
            tasks.add(new WorkflowTask(engineTask));
        }
        return tasks;
    }

    public List<ProcessInstance> getProcessInstances(@NotNull User user, @NotNull Container container)
    {
        return getRuntimeService().createProcessInstanceQuery()
                .processInstanceTenantId(container.getId())
                .variableValueEquals("initiatorId", String.valueOf(user.getUserId()))
                .includeProcessVariables().list();
    }

    public Long getProcessInstanceCount(String processDefinitionKey, @NotNull User initiator, @NotNull Container container)
    {
        return getRuntimeService().createProcessInstanceQuery()
                .processInstanceTenantId(container.getId())
                .processDefinitionKey(processDefinitionKey)
                .variableValueEquals("initiatorId", String.valueOf(initiator.getUserId()))
                .includeProcessVariables().count();
    }

    /**
     * Given the id of a task, returns the corresponding process instance
     * @param processInstanceId id of process instance to retrieve
     * @return
     */
    public ProcessInstance getProcessInstance(@NotNull String processInstanceId)
    {
        return getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    /**
     * Given the id of a particular task, will update the process instance variables for the instance that
     * contains this task
     * @param taskId -
     *               id of the task whose process variables should be updated
     * @param variables -
     *                  variables that will be merged into the existing set of variables; if null, nothing will happen
     *
     */
    public void updateProcessVariables(@NotNull String taskId, @NotNull Map<String, Object> variables)
    {
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        Map<String, Object> currentVariables = task.getProcessVariables();
        if (currentVariables == null)
            currentVariables = variables;
        else if (variables != null)
            currentVariables.putAll(variables);
        getRuntimeService().setVariables(task.getProcessInstanceId(), currentVariables);
    }

    public void replaceProcessVariables(@NotNull String taskId, @Nullable Map<String, Object> variables)
    {
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        getRuntimeService().setVariables(task.getProcessInstanceId(), variables);

    }

    public void deleteProcessInstance(@NotNull String processInstanceId, @Nullable String reason)
    {
        getRuntimeService().deleteProcessInstance(processInstanceId, reason);
    }

    /**
     * Returns the set of variables associated with the given processInstance as well as a list of the current active tasks for that instance
     * @param processInstanceId
     * @return
     * @throws Exception
     */
    public Map<String, Object> getProcessInstanceVariables(@NotNull String processInstanceId) throws Exception
    {
        ProcessInstance processInstance  = getRuntimeService().createProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).singleResult();
        return processInstance.getProcessVariables();
    }

    public ProcessDefinition getProcessDefinition(@NotNull String processDefinitionKey)
    {
        return getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
    }

    /**
     * @param container the container for which this query is being made
     * @return the number of process definitions currently deployed in the system.
     */
    protected long getProcessDefinitionCount(@NotNull Container container)
    {
        return getRepositoryService().createProcessDefinitionQuery().processDefinitionTenantId(container.getId()).count();
    }

    public List<ProcessDefinition> getProcessDefinitionList(@NotNull Container container)
    {
        return getRepositoryService().createProcessDefinitionQuery().processDefinitionTenantId(container.getId()).latestVersion().list();
    }

    public Map<String, String> getProcessDefinitionNames(@NotNull Container container)
    {
        Map<String, String> keyToNameMap = new HashMap<>();

        List<ProcessDefinition> definitions = WorkflowManager.get().getProcessDefinitionList(container);
        for (ProcessDefinition definition : definitions)
        {
            keyToNameMap.put(definition.getKey(), definition.getName());
        }
        return keyToNameMap;
    }

    public InputStream getProcessDiagram(@NotNull String processInstanceId)
    {
        ProcessInstance instance = getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return getRepositoryService().getProcessDiagram(instance.getProcessDefinitionId());
    }

    public InputStream getProcessDiagramByKey(@NotNull String processDefinitionKey, @NotNull Container container)
    {
        ProcessDefinition definition = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).processDefinitionTenantId(container.getId()).latestVersion().singleResult();
        if (definition != null)
            return getRepositoryService().getProcessDiagram(definition.getId());
        else
            return null;
    }

    public String deployWorkflow(@NotNull String workflowName, @NotNull Container container)
    {
        Deployment deployment = getRepositoryService().createDeployment().tenantId(container.getId()).addClasspathResource(getWorkflowFileName(workflowName)).deploy();
        return deployment.getId();
    }

    public void deleteWorkflow(@NotNull String deploymentId)
    {
        getRepositoryService().deleteDeployment(deploymentId);
    }

    private String getWorkflowFileName(@NotNull String workflowName)
    {
        return WORKFLOW_MODEL_DIR + File.separator + workflowName + WORKFLOW_FILE_NAME_EXTENSION;
    }

    protected RuntimeService getRuntimeService()
    {
        return getProcessEngine().getRuntimeService();
    }

    protected ManagementService getManagementService()
    {
        return getProcessEngine().getManagementService();
    }

    protected TaskService getTaskService()
    {
        return getProcessEngine().getTaskService();
    }

    protected RepositoryService getRepositoryService()
    {
        return getProcessEngine().getRepositoryService();
    }


    private ProcessEngine getProcessEngine()
    {
        if (_processEngine == null)
        {
            ProcessEngineConfiguration processConfig;
            DataSource dataSource = WorkflowSchema.getInstance().getSchema().getScope().getDataSource();

            processConfig = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(ACTIVITI_CONFIG_FILE)
                    .setDataSource(dataSource);
            _processEngine = processConfig.buildProcessEngine();
        }
        return _processEngine;
    }

}