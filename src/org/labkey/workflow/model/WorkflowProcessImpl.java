/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    private String _processDefinitionKey;
    private String _id = null;
    private Map<String, Object> _processVariables = new HashMap<>();
    private Integer _initiatorId;
    private String _processInstanceId;
    private ViewContext _viewContext;
    private String _name; // the name for this process instance
    private List<WorkflowTask> _currentTasks;
    private Container _container;
    private String _moduleName;
    private PermissionsHandler _permissionsHandler;
    private List<WorkflowJob> _currentJobs;

    public WorkflowProcessImpl(String processDefinitionKey, String moduleName)
    {
        _processDefinitionKey = processDefinitionKey;
        _moduleName = moduleName;
    }

    public WorkflowProcessImpl(String id, Container container)
    {
        this(WorkflowManager.get().getProcessInstance(id));
        _id = id;
    }

    public WorkflowProcessImpl(ProcessInstance engineProcessInstance)
    {

        _engineProcessInstance = engineProcessInstance;
        if (_engineProcessInstance != null)
        {
            _processVariables = WorkflowManager.get().getProcessInstanceVariables(engineProcessInstance.getProcessInstanceId());
            if (_processVariables.get(CONTAINER_ID) != null)
                _container = ContainerManager.getForId((String) _processVariables.get(CONTAINER_ID));
            if (_processVariables.get(INITIATOR_ID) != null)
                setInitiatorId(Integer.valueOf((String) _processVariables.get(INITIATOR_ID)));
            setCurrentTasks(WorkflowManager.get().getCurrentProcessTasks(engineProcessInstance.getProcessInstanceId(), _container));
            setCurrentJobs(WorkflowManager.get().getCurrentProcessJobs(engineProcessInstance.getProcessInstanceId(), _container));
        }
    }

    @JsonIgnore
    public Container getContainer()
    {
        return _container;
    }

    public String getId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getId();
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getProcessDefinitionKey()
    {
        if (_engineProcessInstance != null)
        {
            if (_engineProcessInstance.getProcessDefinitionKey() == null)
                _processDefinitionKey = WorkflowManager.get().getProcessDefinitionKey(_engineProcessInstance.getProcessDefinitionId());
            else
                return _engineProcessInstance.getProcessDefinitionKey();
        }
        return _processDefinitionKey;
    }

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

    public String getProcessDefinitionModule()
    {
        if (_engineProcessInstance == null)
            return _moduleName;
        if (_moduleName == null)
        {
            Lsid lsid = new Lsid(WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), _container).getCategory());
            _moduleName = lsid.getObjectId();
        }
        return _moduleName;
    }

    public void setProcessDefinitionKey(String processKey)
    {
        _processDefinitionKey = processKey;
    }


    public Map<String, Object> getProcessVariables()
    {
        return _processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables)
    {
        _processVariables = processVariables;
    }

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

    public Integer getInitiatorId()
    {
        return _initiatorId;
    }

    public void setInitiatorId(Integer initiatorId)
    {
        _initiatorId = initiatorId;
    }

    public String getProcessInstanceId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getProcessInstanceId();
        return _processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }

    public String getName()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getName();
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    @JsonIgnore
    public User getInitiator()
    {
        if (getInitiatorId() != null)
        {
            return UserManager.getUser(getInitiatorId());
        }
        return null;
    }

    public List<WorkflowTask> getCurrentTasks()
    {
        return _currentTasks;
    }

    public void setCurrentTasks(List<WorkflowTask> currentTasks)
    {
        _currentTasks = currentTasks;
    }

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

    public boolean canAccessData(User user, Container container)
    {
        return getPermissionsHandler(user, container).canAccessData(this);
    }

    public boolean canView(User user, Container container)
    {
        return getPermissionsHandler(user, container).canView(this);
    }

    public boolean canDelete(User user, Container container)
    {
        return getPermissionsHandler(user, container).canDelete(this);
    }

    @Override
    public boolean canDeploy(User user, Container container)
    {
        return getPermissionsHandler(user, container).canDeployProcess(getProcessDefinitionKey());
    }

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
}
