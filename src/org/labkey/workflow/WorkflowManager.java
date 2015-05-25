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
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.activiti.engine.task.DelegationState;
import org.activiti.engine.task.IdentityLink;
import org.activiti.engine.task.NativeTaskQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.cache.CacheLoader;
import org.labkey.api.data.Container;
import org.labkey.api.files.FileSystemDirectoryListener;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleLoader;
import org.labkey.api.module.ModuleResourceCache;
import org.labkey.api.module.ModuleResourceCacheHandler;
import org.labkey.api.resource.Resource;
import org.labkey.api.security.Group;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.util.Path;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public class WorkflowManager
{
    private static final WorkflowManager _instance = new WorkflowManager();
    private ProcessEngine _processEngine = null;
    private static final String ACTIVITI_CONFIG_FILE = "resources/workflow/config/activiti.cfg.xml";
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";
    public static final String WORKFLOW_MODEL_DIR = "workflow/model";
    private static final Path WORKFLOW_MODEL_PATH = new Path("workflow", "model");
    private final Set<Module> _workflowModules = new CopyOnWriteArraySet<>();
    // map between module name and the list of workflow model files defined in that module
    private Map<String, List<File>> _workflowModelFiles = new HashMap<>();

//    private final ModuleResourceCache<File> CACHE = ModuleResourceCaches.create(WORKFLOW_MODEL_PATH, "Workflow model definitions", new WorkflowModelFileCacheHandler());

    public enum TaskInvolvement {ASSIGNED, GROUP_TASK, DELEGATION_OWNER}

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    // At startup, we record all modules with "resources/workflow/model" directories and register a file listener to monitor for changes.
    // Loading the list of models in each module and the descriptors themselves happens lazily.
    public void registerModule(Module module)
    {
        _workflowModules.add(module);
    }


    @NotNull
    public List<File> getWorkflowModels(@Nullable Container container)
    {
        Collection<Module> activeModules = container == null ? ModuleLoader.getInstance().getModules() : container.getActiveModules();
        ArrayList<File> modelsList = new ArrayList<>();

        for (Module module : activeModules)
        {
            if (!_workflowModules.contains(module))
                continue;

            modelsList.addAll(getWorkflowModels(module));
//            getWorkflowModelFiles(module);

        }
        // now add the models defined in the workflow module itself
        modelsList.addAll(getWorkflowModels(ModuleLoader.getInstance().getModule(WorkflowModule.NAME), "model"));

        return Collections.unmodifiableList(modelsList);
    }

    private List<File> getWorkflowModels(@NotNull Module module)
    {
        return getWorkflowModels(module, WORKFLOW_MODEL_DIR);
    }


    private List<File> getWorkflowModels(@NotNull Module module, String directory)
    {
        List<File> models = new ArrayList<>();
        if (_workflowModelFiles.get(module.getName()) == null)
        {
            File modelDir = new File(module.getExplodedPath(), directory);
            if (modelDir.isDirectory())
            {
                String[] modelFiles = modelDir.list(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        return name.endsWith(WORKFLOW_FILE_NAME_EXTENSION);
                    }
                });
                for (String modelFile : modelFiles)
                {
                    models.add(new File(modelDir, modelFile));
                }
                _workflowModelFiles.put(module.getName(), models);
            }
        }
        else
        {
            models = _workflowModelFiles.get(module.getName());
        }
        return models;
    }


//    private Collection<File> getWorkflowModelFiles(@NotNull Module module)
//    {
//        return CACHE.getResources(module);
//    }

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
    public Long getTotalTaskCount(@NotNull String processDefinitionKey, @NotNull UserPrincipal user, @Nullable Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'");
        if (container != null)
                sql.append(" AND T.tenant_id_ = #{containerId} ");
        NativeTaskQuery query = getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(user.getUserId()));
        if (container != null)
            query.parameter("containerId", String.valueOf(container.getId()));
        return query.count();
    }

    /**
     * Retrieve the list of tasks currently assigned to a given user in the given container
     * @param user the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public List<Task> getAssignedTaskList(@NotNull UserPrincipal user, @Nullable Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + getManagementService().getTableName(Task.class) + " T WHERE T.assignee_ = #{userId}");
        if (container != null)
        {
            sql.append("AND T.tenant_id_ = #{containerId}");
        }
        NativeTaskQuery query = getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(user.getUserId()));
        if (container != null)
            query.parameter("containerId", String.valueOf(container.getId()));
        return query.list();
    }

    /**
     * Retrieve the count of the list of tasks currently assigned to a given user in the given container
     * @param user the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public Long getAssignedTaskCount(@NotNull String processDefinitionKey, @NotNull UserPrincipal user, @Nullable Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'")
                .append(" AND T.assignee_ = #{userId}");
        if (container != null)
                sql.append(" AND T.tenant_id_ = #{containerId} ");
        NativeTaskQuery query = getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(user.getUserId()));
        if (container != null)
            query.parameter("containerId", String.valueOf(container.getId()));
        return query.count();
    }

    /**
     * Retrieve the list of tasks currently owned by a given user in the given container
     * @param principal the user whose tasks should be retrieved
     * @param container the container of the tasks
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public List<Task> getOwnedTaskList(@NotNull UserPrincipal principal, @Nullable Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT * FROM " + getManagementService().getTableName(Task.class) + " T WHERE T.owner_ = #{userId} ");
        if (container != null)
            sql.append("AND T.tenant_id_ = #{containerId}");
        NativeTaskQuery query = getTaskService().createNativeTaskQuery().sql(sql.toString()).parameter("userId", String.valueOf(principal.getUserId()));
        if (container != null)
            query.parameter("containerId", String.valueOf(container.getId()));
        return query.list();
    }

    /**
     * Retrieve the count of the list of tasks currently assigned to a given user in the given container
     *
     * @param processDefinitionKey
     * @param principal the user whose tasks should be retrieved
     * @param container the container of the tasks; if null returns tasks in all containers
     * @return a list of tasks, which is empty if there are no tasks meeting the criteria
     */
    @NotNull
    public Long getOwnedTaskCount(String processDefinitionKey, @NotNull UserPrincipal principal, @Nullable Container container)
    {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM " + getManagementService().getTableName(Task.class) + " T ")
                .append(" JOIN " + getManagementService().getTableName(ProcessDefinition.class) + " D on T.proc_def_id_ = D.id_")
                .append(" WHERE D.key_ = '" + processDefinitionKey + "'")
                .append(" AND T.owner_ = #{userId}");
        if (container != null)
                sql.append(" AND T.tenant_id_ = #{containerId} ");
        NativeTaskQuery query = getTaskService().createNativeTaskQuery()
                .sql(sql.toString())
                .parameter("userId", String.valueOf(principal.getUserId()));
        if (container != null)
            query.parameter("containerId", String.valueOf(container.getId()));
        return query.count();
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
    public Map<UserPrincipal, List<Task>> getGroupTasks(@NotNull UserPrincipal principal, @Nullable Container container)
    {
        Map<UserPrincipal, List<Task>> tasks = new HashMap<>();
        for (int groupId : principal.getGroups())
        {
            TaskQuery query = getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(groupId));
            if (container != null)
                query.taskTenantId(container.getId());
            List<Task> groupTasks = query.list();
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
            TaskQuery query = getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(groupId));
            if (container != null)
                query.taskTenantId(container.getId());
            Long count = query.count();
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
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        if (task.getDelegationState() == DelegationState.PENDING)
            getTaskService().resolveTask(taskId);
        else
            getTaskService().complete(taskId);
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
     * @param processDefinitionKey - the unique key for this process definition
     * @param name - the human-readable name for the process
     * @param processVariables - the set of variables to associate with this process instance (should contain at least the INITIATOR_ID variable)
     * @param container the container in which this process is being created  @return the id of the new process instance for this workflow
     */
    public String startWorkflow(@NotNull String processDefinitionKey, @Nullable String name, @NotNull Map<String, Object> processVariables, @Nullable Container container)
    {

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
        builder.addVariable(WorkflowProcess.CREATED_DATE, new Date());
        ProcessInstance instance = builder.start();
        return instance.getId();
    }

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


    public List<WorkflowTask> getCurrentProcessTasks(@NotNull String processInstanceId, @NotNull User user, @Nullable Container container)
    {
        TaskQuery query =  getTaskService().createTaskQuery().processInstanceId(processInstanceId);
        if (container != null)
            query.taskTenantId(container.getId());
        List<Task> engineTasks = query.list();
        List<WorkflowTask> tasks = new ArrayList<>();
        for (Task engineTask : engineTasks)
        {
            tasks.add(new WorkflowTask(engineTask));
        }
        return tasks;
    }

    public Long getProcessInstanceCount(String processDefinitionKey, @NotNull User initiator, @Nullable Container container)
    {
        ProcessInstanceQuery query = getRuntimeService().createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .variableValueEquals("initiatorId", String.valueOf(initiator.getUserId()))
                .includeProcessVariables();
        if (container != null)
            query.processInstanceTenantId(container.getId());
        return query.count();
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
        Task task = getTaskService().createTaskQuery().includeProcessVariables().taskId(taskId).singleResult();
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

    public ProcessDefinition getProcessDefinition(@NotNull String processDefinitionKey, @Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey);
        if (container != null)
        {
            query.processDefinitionTenantId(container.getId());
        }
        return query.latestVersion().singleResult();
    }

    /**
     * @param container the container for which this query is being made
     * @return the number of process definitions currently deployed in the system.
     */
    protected long getProcessDefinitionCount(@Nullable Container container)
    {
        ProcessDefinitionQuery query = getRepositoryService().createProcessDefinitionQuery();
        if (container != null)
        {
            query.processDefinitionTenantId(container.getId());
        }
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

    public List<FormProperty> getFormFields(String taskId)
    {
        TaskFormData form =  getFormService().getTaskFormData(taskId);
        return form.getFormProperties();
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

//    public void makeContainerDeployment(@NotNull String processDefinitionKey, @NotNull Container container) throws FileNotFoundException
//    {
//        Long count = getRepositoryService().createDeploymentQuery().processDefinitionKey(processDefinitionKey).deploymentTenantId(container.getId()).count();
//        if (count > 0)
//            return;
//
//        ProcessDefinition definition = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
//
//        deployWorkflow(new File(definition.getResourceName()), container);
//    }

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

    private String getWorkflowFileName(@NotNull String workflowName)
    {
        return "resources/workflow/model" + File.separator + workflowName + WORKFLOW_FILE_NAME_EXTENSION;
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


    private static class WorkflowModelFileCacheHandler implements ModuleResourceCacheHandler<String, File>
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
        public CacheLoader<String, File> getResourceLoader()
        {
            return new CacheLoader<String, File>()
            {
                @Override
                public File load(String key, @Nullable Object argument)
                {
                    ModuleResourceCache.CacheId id = ModuleResourceCache.parseCacheKey(key);
                    Module module = id.getModule();
                    String filename = id.getName();
                    Path path = WORKFLOW_MODEL_PATH.append(filename);
                    Resource resource  = module.getModuleResolver().lookup(path);

                    return new File(resource.getPath().toString());
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