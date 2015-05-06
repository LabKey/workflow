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
import org.labkey.api.data.Container;
import org.labkey.api.security.Group;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.workflow.view.ProcessSummaryBean;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class WorkflowManager
{
    private static final WorkflowManager _instance = new WorkflowManager();
    private ProcessEngine _processEngine = null;
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    private ProcessEngine getActivitiProcessEngine()
    {
        if (_processEngine == null)
        {
            ProcessEngineConfiguration processConfig;
            DataSource dataSource = WorkflowSchema.getInstance().getSchema().getScope().getDataSource();

            // TODO figure out how to make this work putting the tables in a schema instead.
            // Currently if you set the schema and the tables do not exist, they are created without the schema
            // TODO put configuration in a file so we can add the parse handlers there.
            processConfig = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                    .setDataSource(dataSource)
                    .setHistoryLevel(HistoryLevel.NONE) // if not set to NONE, it will try to recreate the tables even if they exist
                    .setDbIdentityUsed(false); // must be set to true initially so the tables are created, but then set to false since
                                               // if set to true, it will try to recreate the id tables even if they exist, but I think we want this false anyway

//
//            ProcessEngineConfigurationImpl processEngineConfiguration = ((ProcessEngineImpl)_processEngine).getProcessEngineConfiguration();
//            List<BpmnParseHandler> parseHandlers = new ArrayList<BpmnParseHandler>();
//            parseHandlers.add(new CandidateGroupParseHandler());
//            processEngineConfiguration.setPostBpmnParseHandlers(parseHandlers); // the parse handlers have to be added BEFORE the engine is built

            _processEngine = processConfig.buildProcessEngine();
        }
        return _processEngine;
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

    /**
     * Returns the number of process definitions currently deployed in the system.
     * @return
     * @param container
     */
    protected long getProcessDefinitionCount(Container container)
    {
        return getRepositoryService().createProcessDefinitionQuery().processDefinitionTenantId(container.getId()).count();
    }

    // TODO create our own WorkflowTask class that this returns?
    public List<Task> getTaskList(User user, Container container)
    {
        List<Task> tasks = getGroupTasks(user);
        tasks.addAll(getTaskService().createNativeTaskQuery().list()); // TODO construct query to select tasks for this user
        return tasks;
    }

    public List<Task> getTaskList(Group group)
    {
        return getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(group.getUserId())).list();
    }


    public List<Task> getGroupTasks(UserPrincipal principal)
    {
        // TODO use actual groups.  This requires(?) that the groups and users are translated into ids when the process XML is parsed.
        // TODO hook into the XML parsing when a deployment happens so that group names are associated with group ids in the database
//        List<Task> tasks = new ArrayList<Task>();
//        for (int groupId : principal.getGroups())
//        {
//            tasks.addAll(getTaskService().createTaskQuery().taskCandidateGroup(String.valueOf(groupId)).list());
//        }


        List<Task> groupTasks = getTaskService().createTaskQuery().taskCandidateGroup("Project Administrator").list();
        groupTasks.addAll(getTaskService().createTaskQuery().taskCandidateGroup("Users").list());
        return groupTasks;
    }


    public Task getTask(String taskId)
    {
        return getTaskService().createTaskQuery().taskId(taskId).includeProcessVariables().singleResult();
    }

    public void updateProcessVariables(String taskId, Map<String, Object> variables)
    {
        if (variables != null)
        {
            Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
            Map<String, Object> currentVariables = task.getProcessVariables();
            currentVariables.putAll(variables);
            getRuntimeService().setVariables(task.getProcessInstanceId(), currentVariables);
        }
    }

    // TODO create own ProcessInstance class?
    public List<ProcessInstance> getProcessInstances(User user, Container container)
    {
        return getRuntimeService().createProcessInstanceQuery().variableValueEquals("requesterId", user.getUserId()).includeProcessVariables().list();
    }


    /**
     * Given the id of a task, returns the corresponding process instance
     * @param taskId
     * @return
     */
    public ProcessInstance getProcessInstance(String taskId)
    {
        Task task = getTaskService().createTaskQuery().taskId(taskId).singleResult();
        return getRuntimeService().createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
    }

    public List<String> getCurrentProcessTaskNames(String processInstanceId)
    {
        return getRuntimeService().getActiveActivityIds(processInstanceId);
    }

    public String startWorkflow(WorkflowProcess workflow, User user, Container container)
    {
        workflow.getProcessVariables().put("requester", user);
        workflow.getProcessVariables().put("container", container.getId());
        ProcessInstance instance = getRuntimeService().startProcessInstanceByKey(workflow.getProcessKey(), workflow.getProcessVariables());
        return instance.getId();
    }


    public Map<String, Object> getProcessInstanceDetails(String processInstanceId) throws Exception
    {
        ProcessInstance processInstance  = getRuntimeService().createProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).singleResult();
        Map<String, Object> details = processInstance.getProcessVariables();
        details.put("currentTasks", getCurrentProcessTaskNames(processInstanceId));

        return details;
    }

    public void completeTask(String taskId)
    {
        WorkflowManager.get().getTaskService().complete(taskId);
    }

    public InputStream getProcessDiagram(String processInstanceId)
    {
        ProcessInstance instance = getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        return getRepositoryService().getProcessDiagram(instance.getProcessDefinitionId());
    }

    public InputStream getProcessDiagramByKey(String processDefinitionKey)
    {
        ProcessDefinition definition = getRepositoryService().createProcessDefinitionQuery().processDefinitionKey(processDefinitionKey).latestVersion().singleResult();
        if (definition != null)
            return getRepositoryService().getProcessDiagram(definition.getId());
        else
            return null;
    }

    public String deployWorkflow(String workflowName, Container container)
    {
        Deployment deployment = getRepositoryService().createDeployment().tenantId(container.getId()).addClasspathResource(getWorkflowFileName(workflowName)).deploy();

        return deployment.getId();
    }

    private String getWorkflowFileName(String workflowName)
    {
        return workflowName + WORKFLOW_FILE_NAME_EXTENSION;
    }

    protected ProcessSummaryBean getProcessSummary(User user, Container container)
    {
        ProcessSummaryBean summaryBean = new ProcessSummaryBean();
        summaryBean.setNumDefinitions(WorkflowManager.get().getProcessDefinitionCount(container));
        summaryBean.setAssignedTasks(WorkflowManager.get().getTaskList(user, container));
        summaryBean.setInstances(WorkflowManager.get().getProcessInstances(user, container));
        return summaryBean;
    }


}