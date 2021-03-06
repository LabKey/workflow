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
package org.labkey.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.labkey.api.action.HasViewContext;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.exp.Lsid;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.view.ViewContext;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.TaskFormField;
import org.labkey.api.workflow.WorkflowJob;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.WorkflowManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowProcessImpl implements WorkflowProcess, HasViewContext
{
    private ProcessInstance _engineProcessInstance;
    private HistoricProcessInstance _historicEngineProcessInstance;

    private String _processDefinitionKey;
    private String _id = null;
    private Map<String, Object> _processVariables = new HashMap<>();
    private Integer _initiatorId;
    private String _processInstanceId;
    private ViewContext _viewContext;
    private String _name; // the name for this process instance
    private List<WorkflowTask> _currentTasks = Collections.EMPTY_LIST;
    private List<WorkflowTask> _completedTasks = Collections.EMPTY_LIST;
    private Container _container;
    private String _moduleName;
    private PermissionsHandler _permissionsHandler;
    private List<WorkflowJob> _currentJobs = Collections.EMPTY_LIST;

    public WorkflowProcessImpl(String processDefinitionKey, String moduleName)
    {
        _processDefinitionKey = processDefinitionKey;
        _moduleName = moduleName;
    }

    public WorkflowProcessImpl(String id)
    {
        ProcessInstance instance = WorkflowManager.get().getProcessInstance(id);
        if (instance != null)
            setActiveEngineInstance(instance, true);
        else
            setHistoricEngineProcessInstance(WorkflowManager.get().getHistoricProcessInstance(id));
        _id = id;
    }

    public WorkflowProcessImpl(ProcessInstance engineProcessInstance)
    {
        setActiveEngineInstance(engineProcessInstance, true);
    }

    public WorkflowProcessImpl(HistoricProcessInstance historicProcessInstance)
    {
        this(historicProcessInstance, false);
    }

    public WorkflowProcessImpl(HistoricProcessInstance historicProcessInstance, boolean includeCompletedTasks)
    {
        ProcessInstance instance = WorkflowManager.get().getProcessInstance(historicProcessInstance.getId());
        if (instance != null)
            setActiveEngineInstance(instance, includeCompletedTasks);
        else
            setHistoricEngineProcessInstance(historicProcessInstance);
    }

    private void setActiveEngineInstance(ProcessInstance instance, boolean includeCompletedTasks)
    {
        _engineProcessInstance = instance;
        if (_engineProcessInstance != null)
        {
            setProcessVariables(WorkflowManager.get().getProcessInstanceVariables(_engineProcessInstance.getProcessInstanceId()));
            setCurrentTasks(WorkflowManager.get().getCurrentProcessTasks(_engineProcessInstance.getProcessInstanceId(), _container));
            setCurrentJobs(WorkflowManager.get().getCurrentProcessJobs(_engineProcessInstance.getProcessInstanceId(), _container));
            if (includeCompletedTasks)
                setCompletedTasks(WorkflowManager.get().getCompletedProcessTasks(_engineProcessInstance.getId(), _container));
        }
    }

    private void setHistoricEngineProcessInstance(HistoricProcessInstance instance)
    {
        _historicEngineProcessInstance = instance;

        if (_historicEngineProcessInstance != null)
        {
            setCompletedTasks(WorkflowManager.get().getCompletedProcessTasks(_historicEngineProcessInstance.getId(), _container));

            Map<String, Object> variables = WorkflowManager.get().getHistoricProcessInstanceVariables(_historicEngineProcessInstance.getId());
            variables.put("endDate", _historicEngineProcessInstance.getEndTime());
            setProcessVariables(variables);
        }
    }

    @JsonIgnore
    public Container getContainer()
    {
        return _container;
    }

    @Override
    public String getId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getId();
        if (_historicEngineProcessInstance != null)
            return _historicEngineProcessInstance.getId();
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    @Override
    public String getProcessDefinitionKey()
    {
        if (_processDefinitionKey == null)
        {
            if (_engineProcessInstance != null)
            {
                if (_engineProcessInstance.getProcessDefinitionKey() == null)
                    _processDefinitionKey = WorkflowManager.get().getProcessDefinitionKey(_engineProcessInstance.getProcessDefinitionId());
                else
                    return _engineProcessInstance.getProcessDefinitionKey();
            }
            else if (_historicEngineProcessInstance != null)
            {
                _processDefinitionKey = WorkflowManager.get().getProcessDefinitionKey(_historicEngineProcessInstance.getProcessDefinitionId());
            }
        }
        return _processDefinitionKey;
    }

    @Override
    public String getProcessDefinitionName()
    {
        if (getProcessDefinitionKey() == null)
            return null;
        ProcessDefinition definition = WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), null);
        if (definition != null)
            return definition.getName();
        else
            return null;
    }

    @Override
    public String getProcessDefinitionModule()
    {
        if (_engineProcessInstance == null && _historicEngineProcessInstance == null)
            return _moduleName;
        if (_moduleName == null)
        {
            Lsid lsid = new Lsid(WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), _container).getCategory());
            _moduleName = lsid.getObjectId();
        }
        return _moduleName;
    }

    @Override
    public void setProcessDefinitionKey(String processKey)
    {
        _processDefinitionKey = processKey;
    }


    @Override
    public Map<String, Object> getProcessVariables()
    {
        return _processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables)
    {
        _processVariables = processVariables;
        if (_processVariables.get(CONTAINER_ID) != null)
            _container = ContainerManager.getForId((String) _processVariables.get(CONTAINER_ID));
        if (_processVariables.get(INITIATOR_ID) != null)
            if (_processVariables.get(INITIATOR_ID) instanceof Integer)
                setInitiatorId((Integer) _processVariables.get(INITIATOR_ID));
            else
                setInitiatorId(Integer.valueOf((String) _processVariables.get(INITIATOR_ID)));
    }

    @JsonIgnore
    @Override
    public Map<String, Object> getVariables()
    {
        return getProcessVariables();
    }

    @Override
    public void setViewContext(ViewContext context)
    {
        _viewContext = context;
    }

    @JsonIgnore
    @Override
    public ViewContext getViewContext()
    {
        return _viewContext;
    }

    @Override
    public Integer getInitiatorId()
    {
        return _initiatorId;
    }

    public String getInitiatorName()
    {
        User initiator = getInitiator();
        if (initiator != null)
            return initiator.getDisplayName(null);
        return null;
    }

    public void setInitiatorId(Integer initiatorId)
    {
        _initiatorId = initiatorId;
    }

    @Override
    public String getProcessInstanceId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getProcessInstanceId();
        else if (_historicEngineProcessInstance != null)
            return _historicEngineProcessInstance.getId();
        return _processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }

    @Override
    public String getName()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getName();
        else if (_historicEngineProcessInstance != null)
            return _historicEngineProcessInstance.getName();
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    @Override
    @JsonIgnore
    public User getInitiator()
    {
        if (getInitiatorId() != null)
        {
            return UserManager.getUser(getInitiatorId());
        }
        return null;
    }

    @Override
    public List<WorkflowTask> getCurrentTasks()
    {
        return _currentTasks;
    }

    public void setCurrentTasks(List<WorkflowTask> currentTasks)
    {
        _currentTasks = currentTasks;
    }

    @Override
    public List<WorkflowTask> getCompletedTasks()
    {
        return _completedTasks;
    }

    public void setCompletedTasks(List<WorkflowTask> completedTasks)
    {
        _completedTasks = completedTasks;
    }

    @Override
    public List<WorkflowJob> getCurrentJobs()
    {
        return _currentJobs;
    }

    public void setCurrentJobs(List<WorkflowJob> currentJobs)
    {
        _currentJobs = currentJobs;
    }

    private PermissionsHandler getPermissionsHandler(User user, Container container)
    {
        if (_permissionsHandler == null)
        {
            _permissionsHandler =  WorkflowRegistry.get().getPermissionsHandler(getProcessDefinitionModule(), user, container);
        }
        return _permissionsHandler;
    }

    @Override
    public boolean canAccessData(User user, Container container)
    {
        return getPermissionsHandler(user, container).canAccessData(this);
    }

    @Override
    public boolean canView(User user, Container container)
    {
        return getPermissionsHandler(user, container).canView(this);
    }

    @Override
    public boolean canDelete(User user, Container container)
    {
        return getPermissionsHandler(user, container).canDelete(this);
    }

    @Override
    public boolean canDeploy(User user, Container container)
    {
        return getPermissionsHandler(user, container).canDeployProcess(getProcessDefinitionKey());
    }

    @Override
    public boolean hasDiagram(Container container)
    {
        if (_engineProcessInstance == null)
            return false;
        ProcessDefinition definition = WorkflowManager.get().getProcessDefinition(_engineProcessInstance.getProcessDefinitionKey(), container);
        return definition != null && definition.getDiagramResourceName() != null;
    }

    @Override
    public boolean isActive()
    {
        return _engineProcessInstance != null;
    }

    @Override
    public boolean isDeployed(Container container)
    {
        return WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), container) != null;
    }

    @Override
    public Map<String, TaskFormField> getStartFormFields(Container container)
    {
        ProcessDefinition definition = WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), container);
        if (definition != null)
            return WorkflowManager.get().getStartFormFields(definition.getId());
        else
            return Collections.emptyMap();
    }

    @Override
    public String getProcessExecutionId()
    {
        List<WorkflowTask> tasks = getCurrentTasks();
        if (tasks.isEmpty())
            return null;
        return tasks.get(0).getExecutionId();
    }
}
