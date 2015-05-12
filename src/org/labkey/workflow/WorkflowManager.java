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

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.Group;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.workflow.view.ProcessSummaryBean;

import javax.sql.DataSource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WorkflowManager
{
    private static final WorkflowManager _instance = new WorkflowManager();
    private ProcessEngine _processEngine = null;
    private static final String ACTIVITI_CONFIG_FILE = "resources/workflow/config/activiti.cfg.xml";
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";
    private static final String WORKFLOW_MODEL_DIR = "resources/workflow/model";

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    public List<String> getProcessNames() throws Exception
    {
        List<String> names = new ArrayList<>();
        names.add("argosDataExport");
        names.add("argosDataExportSimple");
        names.add("submitForApprovalWithRetry");
        names.add("submitForApprovalWithoutRetry");
//        if (WorkflowManager.class.getClassLoader().getResource(WORKFLOW_MODEL_DIR) != null)
//        {
//            List<String> files = IOUtils.readLines(WorkflowManager.class.getClassLoader().getResourceAsStream(WORKFLOW_MODEL_DIR), Charsets.UTF_8);
//            for (String fileName : files)
//            {
//                names.add(FilenameUtils.getBaseName(fileName));
//            }
//        }
        return names;
    }

    // TODO create our own WorkflowTask class that this returns?
    public List<Task> getTaskList(User user, Container container)
    {
        List<Task> tasks = getGroupTasks(user);
        tasks.addAll(getTaskService().createTaskQuery().list()); // TODO construct query to select tasks for this user
        return tasks;
    }

    public List<Task> getTaskList(@NotNull Group group)
    {
        return getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(group.getUserId())).list();
    }

    public void addGroupAssignment(Task task, UserPrincipal principal)
    {
        getTaskService().addCandidateGroup(task.getId(), String.valueOf(principal.getUserId()));
    }

    public List<Task> getGroupTasks(UserPrincipal principal)
    {
        List<Task> tasks = new ArrayList<Task>();
        for (int groupId : principal.getGroups())
        {
            tasks.addAll(getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(groupId)).list());
        }

        return tasks;

//        List<Task> groupTasks = getTaskService().createTaskQuery().taskCandidateGroup("Project Administrator").list();
//        groupTasks.addAll(getTaskService().createTaskQuery().taskCandidateGroup("Users").list());
//        return groupTasks;
    }


    public Task getTask(@NotNull String taskId)
    {
        return getTaskService().createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
    }

    public void completeTask(@NotNull String taskId)
    {
        WorkflowManager.get().getTaskService().complete(taskId);
    }

    public void assignTask(@NotNull String taskId, @NotNull int principalId)
    {
        getTaskService().setAssignee(taskId, String.valueOf(principalId));
    }

    public void delegateTask(@NotNull String taskId, @NotNull int principalId)
    {
        getTaskService().delegateTask(taskId, String.valueOf(principalId));
    }

    public void deleteTask(@NotNull String taskId, @Nullable String reason)
    {
        getTaskService().deleteTask(taskId, reason);
    }

    /**
     * Creates a new process instance for the given workflow and returns the id for this new instance.
     * @param workflow the workflow for which an instance is requested
     * @param container the container in which this process is being created
     * @return the id of the new process instance for this workflow
     */
    public String startWorkflow(@NotNull WorkflowProcess workflow, @NotNull Container container)
    {

        ProcessInstance instance = getRuntimeService().startProcessInstanceByKeyAndTenantId(workflow.getProcessKey(), workflow.getProcessVariables(), container.getId());
        return instance.getId();
    }

    public List<Task> getCurrentProcessTasks(@NotNull String processInstanceId)
    {
        return getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
    }

    public List<ProcessInstance> getProcessInstances(@NotNull User user, @NotNull Container container)
    {
        return getRuntimeService().createProcessInstanceQuery()
                .processInstanceTenantId(container.getId())
                .variableValueEquals("userId", String.valueOf(user.getUserId()))
                .includeProcessVariables().list();
    }

    /**
     * Given the id of a task, returns the corresponding process instance
     * @param taskId
     * @return
     */
    public ProcessInstance getProcessInstance(@NotNull String taskId)
    {
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        return getRuntimeService().createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
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
    public Map<String, Object> getProcessInstanceDetails(@NotNull String processInstanceId) throws Exception
    {
        ProcessInstance processInstance  = getRuntimeService().createProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).singleResult();
        Map<String, Object> details = processInstance.getProcessVariables();
        details.put("currentTasks", getCurrentProcessTasks(processInstanceId));

        return details;
    }

    /**
     * @param container the container for which this query is being made
     * @return the number of process definitions currently deployed in the system.
     */
    protected long getProcessDefinitionCount(@NotNull Container container)
    {
        return getRepositoryService().createProcessDefinitionQuery().processDefinitionTenantId(container.getId()).count();
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

    protected ProcessSummaryBean getProcessSummary(User user, Container container)
    {
        ProcessSummaryBean summaryBean = new ProcessSummaryBean();
        summaryBean.setNumDefinitions(getProcessDefinitionCount(container));
        summaryBean.setAssignedTasks(getTaskList(user, container));
        summaryBean.setInstances(getProcessInstances(user, container));
        return summaryBean;
    }

    protected RuntimeService getRuntimeService()
    {
        return getActivitiProcessEngine().getRuntimeService();
    }

    protected TaskService getTaskService()
    {
        return getActivitiProcessEngine().getTaskService();
    }

    protected RepositoryService getRepositoryService()
    {
        return getActivitiProcessEngine().getRepositoryService();
    }


    private ProcessEngine getActivitiProcessEngine()
    {
        if (_processEngine == null)
        {
            ProcessEngineConfiguration processConfig;
            DataSource dataSource = WorkflowSchema.getInstance().getSchema().getScope().getDataSource();

            // you set the database schema so it will look there when deciding if the tables exist
            // you set the table prefix and prefixIsSchema so it will prepend the schema name in the SQL statements
            processConfig = ProcessEngineConfiguration.createProcessEngineConfigurationFromResource(ACTIVITI_CONFIG_FILE)
                    .setDataSource(dataSource).setDatabaseSchema(WorkflowSchema.NAME)
                    .setTablePrefixIsSchema(true).setDatabaseTablePrefix(WorkflowSchema.NAME + ".")
                    .setHistoryLevel(HistoryLevel.ACTIVITY)
                    .setDbIdentityUsed(false);
            _processEngine = processConfig.buildProcessEngine();
            getRuntimeService().addEventListener(new WorkflowEventListener());
        }
        return _processEngine;
    }

}