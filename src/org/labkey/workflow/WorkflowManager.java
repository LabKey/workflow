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
import org.activiti.engine.FormService;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.history.HistoricTaskInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.cache.CacheLoader;
import org.labkey.api.data.Container;
import org.labkey.api.exp.Lsid;
import org.labkey.api.files.FileSystemDirectoryListener;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleResourceCache;
import org.labkey.api.module.ModuleResourceCacheHandler;
import org.labkey.api.module.ModuleResourceCaches;
import org.labkey.api.resource.FileResource;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.Path;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.UnauthorizedException;
import org.labkey.api.workflow.TaskFormField;
import org.labkey.api.workflow.WorkflowJob;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.model.TaskFormFieldImpl;
import org.labkey.workflow.model.WorkflowEngineTaskImpl;
import org.labkey.workflow.model.WorkflowHistoricTaskImpl;
import org.labkey.workflow.model.WorkflowJobImpl;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowManager
{
    private static final String ACTIVITI_CONFIG_FILE = "resources/workflow/config/activiti.cfg.xml";
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";
    private static final Path WORKFLOW_MODEL_PATH = new Path("workflow", "model");
    private static final WorkflowManager _instance = new WorkflowManager();

    private ProcessEngine _processEngine = null;

    // a cache of the deployments at the global scope. New deployments are created in the database when the workflow model files change.
    private final ModuleResourceCache<Deployment> DEPLOYMENT_CACHE = ModuleResourceCaches.create(WORKFLOW_MODEL_PATH, "Workflow model definitions", new WorkflowDeploymentCacheHandler());

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    /**
     * Gets the list of candidate group ids for a task given its id, or an empty list if there are no candidate groups
     * @param taskId id of the task to get groups for
     * @return list of candidate group ids.
     */
    @NotNull
    public List<Integer> getCandidateGroupIds(@NotNull String taskId)
    {
        List<Integer> groupIds = new ArrayList<>();
        List<IdentityLink> links = getTaskService().getIdentityLinksForTask(taskId);
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
     * Retrieve a task given its id
     * @param taskId id of the task to retrieve
     * @param container container in which the task is defined
     * @return a workflow task of the given Id.  If there is no such task, an exception is thrown.
     */
    public WorkflowTask getTask(@NotNull String taskId, @Nullable Container container)
    {
        Task engineTask = getEngineTask(taskId, container);
        if (engineTask != null)
            return new WorkflowEngineTaskImpl(engineTask);

        return new WorkflowHistoricTaskImpl(taskId, container);
    }

    public Task getEngineTask(@NotNull String taskId, @Nullable Container container)
    {
        TaskQuery query = getTaskService().createTaskQuery().taskId(taskId).includeTaskLocalVariables().includeProcessVariables();
        if (container != null)
            query.taskTenantId(container.getId());
        return query.singleResult();
    }

    public HistoricTaskInstance getHistoricTask(@NotNull String taskId, @Nullable Container container)
    {
        HistoricTaskInstanceQuery query = getHistoryService().createHistoricTaskInstanceQuery().taskId(taskId).includeTaskLocalVariables().includeProcessVariables();
        if (container != null)
            query.taskTenantId(container.getId());
        return query.singleResult();
    }


    /**
     * Completes a task in a workflow given the id of the task
     * @param taskId the id of an active task
     * @param user the user completing the task
     * @param container the container in which the task is being completed
     * @throws ActivitiObjectNotFoundException when no task exists with the given id.
     * @throws ActivitiException when this task is pending delegation.
     */
    public void completeTask(@NotNull String taskId, User user, Container container) throws Exception
    {
        WorkflowTask task = new WorkflowEngineTaskImpl(getTaskService().createTaskQuery().taskId(taskId).singleResult());

        if (!task.isActive())
            throw new Exception("No such task (id = " + taskId + ")");
        if (!task.canComplete(user, container))
            throw new UnauthorizedException("User does not have permission to complete this task");

        if (task.isDelegated())
            getTaskService().resolveTask(taskId);
        else
            getTaskService().complete(taskId);
    }

    /**
     * Claim a task that one (or more) of the user's group is currently a candidate group for
     * @param taskId id of the task to be claimed
     * @param userId id of the user who is claiming the task
     * @param container the container in which the task is being handled
     * @throws Exception if user
     */
    public void claimTask(@NotNull String taskId, @NotNull Integer userId, Container container) throws Exception
    {
        User user = UserManager.getUser(userId);
        if (user == null)
            throw new Exception("No such user: (id = " + userId + ")");

        WorkflowTask task = getTask(taskId, container);

        if (task == null || !task.isActive())
            throw new Exception("No such task (id = " + taskId + ")");
        if (!task.canClaim(user, container))
            throw new UnauthorizedException("User " + user + " cannot claim task " + taskId);

        getTaskService().setOwner(taskId, String.valueOf(userId));
        getTaskService().claim(taskId, String.valueOf(userId));
    }

    /**
     * Assign a particular task to a user given the id of the user
     * @param taskId id of the task to be assigned
     * @param assigneeId id of the user to whom the task should be assigned
     * @param user principal doing the assignment
     * @param container container context for this assignment
     * @throws Exception
     */
    public void assignTask(@NotNull String taskId, @NotNull Integer assigneeId, User user, Container container) throws Exception
    {
        if (assigneeId == null)
        {
            throw new Exception("No user specified");
        }
        else if (taskId == null)
        {
            throw new Exception("No task specified");
        }
        else
        {
            User assignee = UserManager.getUser(assigneeId);
            if (assignee == null)
                throw new Exception("No such user: (id = " + assigneeId + ")");

            WorkflowTask task = getTask(taskId, container);

            if (task == null)
                throw new Exception("No such task (id = " + taskId + ")");
            if (!task.canAssign(user, container))
                throw new Exception("User " + user + " does not have permission to assign tasks");

            getTaskService().setOwner(taskId, String.valueOf(assigneeId));
            getTaskService().setAssignee(taskId, String.valueOf(assigneeId));
        }
    }

    /**
     * Delegate a task to a particular task (retaining ownership of the task for later review of results)
     * @param taskId id of the task to be delegated
     * @param user principal doing the delegation
     * @param designateeId id of the user to delegate to
     * @param container context in which the delegation is being made
     * @throws Exception
     */
    public void delegateTask(@NotNull String taskId, @NotNull User user, @NotNull Integer designateeId, Container container) throws Exception
    {

        User designatee = UserManager.getUser(designateeId);
        if (designatee == null)
            throw new Exception("No such user: (id = " + designateeId + ")");
        WorkflowTask task = getTask(taskId, container);

        if (task == null || !task.isActive())
            throw new Exception("No such task (id = " + taskId + ")");
        if (!task.canDelegate(user, container))
            throw new Exception("User " + user + " does not have permission to delegate tasks");
        getTaskService().delegateTask(taskId, String.valueOf(designateeId));
        getTaskService().setOwner(taskId, String.valueOf(user.getUserId()));

    }

    /**
     * Creates a new process instance for the given workflow and returns the id for this new instance.
     * @param moduleName - the name of the module in which the process definition key is defined
     * @param processDefinitionKey - the unique key for this process definition
     * @param name - the human-readable name for the process
     * @param processVariables - the set of variables to associate with this process instance (should contain at least the INITIATOR_ID variable)
     * @param container the container in which this process is being created  @return the id of the new process instance for this workflow
     */
    public String startWorkflow(@NotNull String moduleName, @NotNull String processDefinitionKey, @Nullable String name, @NotNull Map<String, Object> processVariables, @Nullable Container container) throws FileNotFoundException
    {

        makeContainerDeployment(moduleName, processDefinitionKey, container);

        ProcessInstanceBuilder builder = getRuntimeService().createProcessInstanceBuilder().processDefinitionKey(processDefinitionKey);
        if (name != null) {
            builder.processInstanceName(name);
        }
        if (container != null)
            builder.tenantId(container.getId());
        for (Map.Entry<String, Object> variable : processVariables.entrySet())
        {
            builder.addVariable(variable.getKey(), variable.getValue());
        }
        builder.addVariable(WorkflowProcess.PROCESS_INSTANCE_URL, new ActionURL(WorkflowController.ProcessInstanceAction.class, container));
        builder.addVariable(WorkflowProcess.CREATED_DATE, new Date()); // TODO this could be retrieved from the corresponding entry in the History table
        ProcessInstance instance = builder.start();
        return instance.getId();
    }

    /**
     * Gets the list of jobs taht are currently active for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instnace is active
     * @return list of workflow jobs, or an empty list of there are none
     */
    public List<WorkflowJob> getCurrentProcessJobs(@NotNull String processInstanceId, @Nullable Container container)
    {
        JobQuery query = getManagementService().createJobQuery().processInstanceId(processInstanceId);
        if (container != null)
            query.jobTenantId(container.getId());
        List<WorkflowJob> list = new ArrayList<>();
        for (Job job : query.list())
        {
            list.add(new WorkflowJobImpl(job));
        }
        return list;
    }

    /**
     * Gets the list of tasks that are currently active for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instance is active
     * @return list of workflow tasks, or an empty list of there are none
     */
    @NotNull
    public List<WorkflowTask> getCurrentProcessTasks(@NotNull String processInstanceId, @Nullable Container container)
    {
        TaskQuery query =  getTaskService().createTaskQuery().processInstanceId(processInstanceId);
        if (container != null)
            query.taskTenantId(container.getId());

        List<WorkflowTask> tasks = new ArrayList<>();
        for (Task engineTask : query.list())
            tasks.add(new WorkflowEngineTaskImpl(engineTask));

        return tasks;
    }

    /**
     * Gets the list of completed tasks for the given processInstanceId in the given container,
     * or in all containers if container is null
     * @param processInstanceId instance for which tasks are to be retrieved
     * @param container container in which the process instance is active
     * @return list of workflow tasks, or an empty list of there are none
     */
    @NotNull
    public List<WorkflowTask> getCompletedProcessTasks(@NotNull String processInstanceId, @Nullable Container container)
    {
        HistoricTaskInstanceQuery query =  getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId).finished();
        if (container != null)
            query.taskTenantId(container.getId());

        List<WorkflowTask> tasks = new ArrayList<>();
        for (HistoricTaskInstance historicTask : query.list())
            tasks.add(new WorkflowHistoricTaskImpl(historicTask));

        return tasks;
    }

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
    public Long getProcessInstanceCount(String processDefinitionKey, @NotNull User user, @Nullable Container container)
    {
        ProcessInstanceQuery query = getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey);
        if (container != null && !container.hasPermission(user, AdminPermission.class))
        {
            query.variableValueEquals("initiatorId", String.valueOf(user.getUserId()));
        }
        if (container != null)
            query.processInstanceTenantId(container.getId());
        return query.count();
    }

    /**
     * Given the process definition key, returns the corresponding list of process instances in the
     * current container that were initiated by the given user
     * @param processDefinitionKey identifier for the process definition
     * @param user user making the request
     * @param container container of context, or null for all containers
     * @return the list of ProcessInstnace objects
     */
    public List<ProcessInstance> getProcessInstanceList(String processDefinitionKey, @NotNull User user, @NotNull Container container)
    {
        ProcessInstanceQuery query = getRuntimeService().createProcessInstanceQuery().processDefinitionKey(processDefinitionKey);
        query.processInstanceTenantId(container.getId());
        query.variableValueEquals("initiatorId", String.valueOf(user.getUserId()));
        return query.list();
    }

    /**
     * Given the id of a process instance, returns the corresponding process instance
     * @param processInstanceId id of process instance to retrieve
     * @return ProcessInstnace corresponding to the given id.
     */
    public ProcessInstance getProcessInstance(@NotNull String processInstanceId)
    {
        return getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    /**
     * Given the process definition key, returns the corresponding list of historic process instances in the
     * current container that were initiated by the given user
     * @param processDefinitionKey identifier for the process definition
     * @param user user making the request
     * @param container container of context, or null for all containers
     * @return the list of HistoricProcessInstance objects
     */
    public List<HistoricProcessInstance> getHistoricalProcessInstanceList(String processDefinitionKey, @NotNull User user, @NotNull Container container)
    {
        HistoricProcessInstanceQuery query = getHistoryService().createHistoricProcessInstanceQuery().processDefinitionKey(processDefinitionKey);
        query.processInstanceTenantId(container.getId());
        query.variableValueEquals("initiatorId", String.valueOf(user.getUserId()));
        return query.list();
    }

    /**
     * Given the id of a process instance, returns the corresponding historic process instance
     * @param processInstanceId id of process instance to retrieve
     * @return HistoricProcessInstance corresponding to the given id.
     */
    public HistoricProcessInstance getHistoricProcessInstance(@NotNull String processInstanceId)
    {
        return getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    /**
     * Returns the set of variables associated with the given historicProcessInstance
     * @param processInstanceId id of the historic process instance in question
     * @return the set of process instance variables for this historic process instance
     */
    public Map<String, Object> getHistoricProcessInstanceVariables(@NotNull String processInstanceId)
    {
        try
        {
            HistoricProcessInstance processInstance = getHistoryService().createHistoricProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).singleResult();
            return processInstance.getProcessVariables();
        }
        catch (PersistenceException e)
        {
            return new HashMap<>();
        }
    }

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
    public void updateProcessVariables(@NotNull String taskId, @Nullable Map<String, Object> variables)
    {
        Task task = getTaskService().createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();
        Map<String, Object> currentVariables = task.getProcessVariables();
        if (currentVariables == null)
            currentVariables = variables;
        else if (variables != null)
            currentVariables.putAll(variables);
        getRuntimeService().setVariables(task.getProcessInstanceId(), currentVariables);
    }

    /**
     * Given the id of a particular task, will replace the process instance variables (not the task variables)
     * for the instance that contains this task with the provided variables.  Task variables are left alone.
     * @param taskId -
     *               id of the task whose process variables should be updated
     * @param variables -
     *                  variables that will replace the existing set of variables
     *
     */
    public void replaceProcessVariables(@NotNull String taskId, @Nullable Map<String, Object> variables)
    {
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        getRuntimeService().setVariables(task.getProcessInstanceId(), variables);
    }

    /**
     * Remove the process instance whose id is supplied, logging the reason for the deletion if provided
     * @param processInstanceId id of the process instance to be removed
     * @param reason reason for deletion (may be null)
     */
    public void deleteProcessInstance(@NotNull String processInstanceId, @Nullable String reason)
    {
        getRuntimeService().deleteProcessInstance(processInstanceId, reason);
    }

    /**
     * Returns the set of variables associated with the given processInstance as well as a list of the current active tasks for that instance
     * @param processInstanceId id of the process instance in question
     * @return the set of process instance variables for this process instance
     */
    public Map<String, Object> getProcessInstanceVariables(@NotNull String processInstanceId)
    {
        try
        {
            ProcessInstance processInstance = getRuntimeService().createProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).singleResult();
            return processInstance.getProcessVariables();
        }
        catch (PersistenceException e)
        {
            return new HashMap<>();
        }
    }

    /**
     * Returns the latest version of the process definition corresponding to the provided key
     * @param processDefinitionKey key identifying the process definition
     * @param container container in which the process is defined
     * @return process definition for the given key
     */
    public ProcessDefinition getProcessDefinition(@NotNull String processDefinitionKey, @Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        if (container != null)
        {
            query.processDefinitionTenantId(container.getId());
        }
        else
        {
            query.processDefinitionWithoutTenantId();
        }
        return query.latestVersion().singleResult();
    }

    /**
     * @param processDefinitionId unique id of a process definition
     * @param container container in which the process is defined
     * @return name of the module that this process definition comes from
     */
    public String getProcessDefinitionModule(@NotNull String processDefinitionId, Container container)
    {
        Lsid lsid = new Lsid(WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(processDefinitionId), container).getCategory());
        return lsid.getObjectId();
    }

    /**
     * @param container the container for which this query is being made
     * @return the number of process definitions currently deployed in the container, or defined without a container if null
     */
    protected long getProcessDefinitionCount(@Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery();
        if (container != null)
            query.processDefinitionTenantId(container.getId());
        else
            query.processDefinitionWithoutTenantId();
        return query.count();
    }

    public List<ProcessDefinition> getProcessDefinitionList(@Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery();
        if (container != null)
        {
            query.processDefinitionTenantId(container.getId());
        }
        return query.latestVersion().list();
    }

    public Map<String, String> getProcessDefinitionNames(@Nullable Container container)
    {
        Map<String, String> keyToNameMap = new HashMap<>();

        List<ProcessDefinition> definitions = WorkflowManager.get().getProcessDefinitionList(container);
        for (ProcessDefinition definition : definitions)
        {
            keyToNameMap.put(definition.getKey(), definition.getName());
        }
        return keyToNameMap;
    }

    public Map<String, TaskFormField> getStartFormFields(String processDefinitionKey)
    {
        StartFormData form = getFormService().getStartFormData(processDefinitionKey);
        List<FormProperty> properties = form.getFormProperties();
        Map<String, TaskFormField> fields = new HashMap<>();
        for (FormProperty property : properties)
        {
            fields.put(property.getId(), new TaskFormFieldImpl(property));
        }
        return fields;
    }

    public Map<String, TaskFormField> getFormFields(String taskId)
    {
        TaskFormData form =  getFormService().getTaskFormData(taskId);
        List<FormProperty> properties =  form.getFormProperties();
        Map<String, TaskFormField> fields = new HashMap<>();
        for (FormProperty property : properties)
        {
            fields.put(property.getId(), new TaskFormFieldImpl(property));
        }
        return fields;
    }

    public String getProcessDefinitionKey(@NotNull String processDefinitionId)
    {
        ProcessDefinition definition =  getRepositoryService().createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
        return definition.getKey();
    }

    public InputStream getProcessDiagram(@NotNull String processInstanceId)
    {
        ProcessInstance instance = getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

        return getRepositoryService().getProcessDiagram(instance.getProcessDefinitionId());
    }

    public InputStream getProcessDiagramByKey(@NotNull String processDefinitionKey, @Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        if (container != null)
        {
            query.processDefinitionTenantId(container.getId());
        }
        ProcessDefinition definition = query.latestVersion().singleResult();
        if (definition != null)
            return getRepositoryService().getProcessDiagram(definition.getId());
        else
            return null;
    }

    public static String getWorkflowDeploymentResourceName(String moduleName, String workflowDefinitionKey)
    {
        return moduleName + "/" + workflowDefinitionKey + WORKFLOW_FILE_NAME_EXTENSION;
    }

    public void makeContainerDeployment(@NotNull String moduleName, @NotNull String processDefinitionKey, @NotNull Container container) throws FileNotFoundException
    {
        // get the deployment in the global scope, referencing the cache
        Deployment globalDeployment = DEPLOYMENT_CACHE.getResource(getWorkflowDeploymentResourceName(moduleName, processDefinitionKey));
        // find the latest version for this container and compare deployment time to the time for the global version
        ProcessDefinition containerDef = getProcessDefinition(processDefinitionKey, container);
        if (containerDef != null)
        {
            Deployment containerDeployment = getDeployment(containerDef.getDeploymentId());
            // if the container version was deployed after the global version, there's nothing more to do
            if (containerDeployment != null && containerDeployment.getDeploymentTime().after(globalDeployment.getDeploymentTime()))
                return;
        }


        ProcessDefinition globalDefinition = getProcessDefinition(processDefinitionKey, null);
        // deploy a (newer) version in this container
        deployWorkflow(new File(globalDefinition.getResourceName()), container);
    }

    private Deployment getDeployment(@NotNull String deploymentId)
    {
        return getRepositoryService().createDeploymentQuery().deploymentId(deploymentId).singleResult();
    }

    private List<Deployment> getDeployments(@NotNull Container container)
    {
        return getRepositoryService().createDeploymentQuery().deploymentTenantId(container.getId()).list();
    }

    public void deleteDeployments(@NotNull Container container)
    {
        for (Deployment deployment : getDeployments(container))
        {
            getRepositoryService().deleteDeployment(deployment.getId(), true);
        }
    }

    public String deployWorkflow(@NotNull File modelFile, @Nullable Container container) throws FileNotFoundException
    {
        FileInputStream stream = new FileInputStream(modelFile);
        DeploymentBuilder builder = getRepositoryService().createDeployment().addInputStream(modelFile.getAbsolutePath(), stream);
        if (container != null)
            builder.tenantId(container.getId());
        Deployment deployment = builder.deploy();
        return deployment.getId();
    }

    public void deleteWorkflow(@NotNull String deploymentId)
    {
        getRepositoryService().deleteDeployment(deploymentId);
    }

    protected RuntimeService getRuntimeService()
    {
        return getProcessEngine().getRuntimeService();
    }

    protected HistoryService getHistoryService()
    {
        return getProcessEngine().getHistoryService();
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

    protected FormService getFormService()
    {
        return getProcessEngine().getFormService();
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


    private static class WorkflowDeploymentCacheHandler implements ModuleResourceCacheHandler<String, Deployment>
    {
        @Override
        public boolean isResourceFile(String filename)
        {
            return StringUtils.endsWithIgnoreCase(filename, WORKFLOW_FILE_NAME_EXTENSION);
        }

        @Override
        public String getResourceName(Module module, String filename)
        {
            return filename;
        }

        @Override
        public String createCacheKey(Module module, String resourceName)
        {
            return ModuleResourceCache.createCacheKey(module, resourceName);
        }

        @Override
        public CacheLoader<String, Deployment> getResourceLoader()
        {
            return new CacheLoader<String, Deployment>()
            {
                @Override
                public Deployment load(String key, @Nullable Object argument)
                {
                    ModuleResourceCache.CacheId id = ModuleResourceCache.parseCacheKey(key);
                    Module module = id.getModule();
                    String filename = id.getName();
                    String processDefinitionKey = filename.substring(0, filename.indexOf("."));
                    Path path = WORKFLOW_MODEL_PATH.append(filename);
                    FileResource resource  = (FileResource) module.getModuleResolver().lookup(path);
                    if (resource != null)
                    {
                        try
                        {
                            // find the latest process definition without a container
                            ProcessDefinition processDef = WorkflowManager.get().getProcessDefinition(processDefinitionKey, null);
                            String deploymentId;
                            if (processDef == null) // no such definition, we'll deploy one
                            {
                                deploymentId = WorkflowManager.get().deployWorkflow(resource.getFile(), null);
                                return WorkflowManager.get().getDeployment(deploymentId);
                            }
                            else
                            {
                                deploymentId = processDef.getDeploymentId();
                                Deployment deployment = WorkflowManager.get().getDeployment(deploymentId);
                                // file is newer than deployment, so we'll deploy a new version
                                if (deployment.getDeploymentTime().before(new Date(resource.getFile().lastModified())))
                                {
                                    deploymentId = WorkflowManager.get().deployWorkflow(resource.getFile(), null);
                                    deployment = WorkflowManager.get().getDeployment(deploymentId);
                                }
                                return deployment;
                            }
                        }
                        catch (FileNotFoundException e)
                        {
                            return null;
                        }
                    }
                    else
                    {
                        return null;
                    }
                }
            };
        }

        @Nullable
        @Override
        public FileSystemDirectoryListener createChainedDirectoryListener(Module module)
        {
            return null;
        }

    }


}