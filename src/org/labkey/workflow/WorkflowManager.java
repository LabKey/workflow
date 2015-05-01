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
import org.labkey.api.security.User;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class WorkflowManager
{
    private static final WorkflowManager _instance = new WorkflowManager();
    private static ProcessEngine _processEngine = null;
    private static final String WORKFLOW_FILE_NAME_EXTENSION = ".bpmn20.xml";

    private WorkflowManager()
    {
        // prevent external construction with a private default constructor
    }

    public static WorkflowManager get()
    {
        return _instance;
    }

    protected ProcessEngine getActivitiProcessEngine()
    {
        if (_processEngine == null)
        {
            ProcessEngineConfiguration processConfig;
            DataSource dataSource = WorkflowSchema.getInstance().getSchema().getScope().getDataSource();

            // TODO figure out how to make this work putting the tables in a schema instead.
            // Currently if you set the schema and the tables do not exist, they are created without the schema
            processConfig = ProcessEngineConfiguration.createStandaloneInMemProcessEngineConfiguration()
                    .setDataSource(dataSource)
                    .setHistoryLevel(HistoryLevel.NONE) // if not set to NONE, it will try to recreate the tables even if they exist
                    .setDbIdentityUsed(false); // must be set to true initially so the tables are created, but then set to false since
                                               // if set to true, it will try to recreate the id tables even if they exist, but I think we want this false anyway
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

    protected long getProcessDefinitionCount()
    {
        return getRepositoryService().createProcessDefinitionQuery().count();
    }

    // TODO create our own UserTask class that this returns
    public List<Task> getTaskList(User user)
    {
        List<Task> tasks = getGroupTasks(user);
        tasks.addAll(getTaskService().createNativeTaskQuery().list()); // TODO construct query to select tasks for this user
        return tasks;
    }

    public List<Task> getGroupTasks(User user)
    {
        // TODO use actual groups
        return getTaskService().createTaskQuery().taskCandidateGroup("Project Administrator").list();
    }

    // TODO create own ProcessInstance class?
    public List<ProcessInstance> getProcessInstances(User user)
    {
        return getRuntimeService().createProcessInstanceQuery().variableValueEquals("userId", user.getUserId()).includeProcessVariables().list();
    }

    public List<String> getCurrentProcessTasks(String processInstanceId)
    {
        return getRuntimeService().getActiveActivityIds(processInstanceId);
    }

    public String startWorkflow(String processKey, Map<String, Object> variables, User user)
    {
        ProcessInstance instance = WorkflowManager.get().getRuntimeService().startProcessInstanceByKey(processKey, variables);

        List<Task> processTasks = getTaskService().createTaskQuery().processInstanceId(instance.getId()).list();
        Task requestTask = processTasks.get(0);
        getTaskService().setOwner(requestTask.getId(), String.valueOf(user.getUserId()));
        getTaskService().setAssignee(requestTask.getId(), String.valueOf(user.getUserId())); // assign this user the first task
        return instance.getId();
    }

    public Map<String, Object> getProcessInstanceDetails(String processInstanceId) throws Exception
    {
        List<ProcessInstance> instanceList = getRuntimeService().createProcessInstanceQuery().includeProcessVariables().processInstanceId(processInstanceId).list();
        if (instanceList.size() != 1)
        {
            throw new Exception("Number of requests with id " + processInstanceId + " not 1 as expected"); // TODO what's the proper exception class
        }
        ProcessInstance processInstance  = instanceList.get(0);
        Map<String, Object> details = processInstance.getProcessVariables();
        details.put("currentTasks", getCurrentProcessTasks(processInstanceId));

        return details;
    }


    public void completeWorkflowTask(String processInstanceId, int taskIndex)
    {
        List<Task> processTasks = getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        Task requestTask = processTasks.get(taskIndex);
        WorkflowManager.get().getTaskService().complete(requestTask.getId());
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

    public String deployWorkflow(String workflowName)
    {
        Deployment deployment = getRepositoryService().createDeployment().addClasspathResource(getWorkflowFileName(workflowName)).deploy();
        return deployment.getId();
    }

    private String getWorkflowFileName(String workflowName)
    {
        return workflowName + WORKFLOW_FILE_NAME_EXTENSION;
    }
}