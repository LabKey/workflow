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
package org.labkey.workflow.model;

import org.activiti.engine.repository.ProcessDefinition;
import org.labkey.api.data.Container;
import org.labkey.api.exp.Lsid;
import org.labkey.api.security.User;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.workflow.WorkflowManager;

import java.io.File;
import java.util.Map;

/**
 * Created by susanh on 5/14/15.
 */
public class WorkflowSummary
{
    private ProcessDefinition _engineProcessDefinition;
    private String _processDefinitionKey;
    private Long _numAssignedTasks;
    private Long _numOwnedTasks;
    private Map<UserPrincipal, Long> _numGroupTasks;
    private Long _numInstances;
    private Long _numTotalTasks;


    public WorkflowSummary()
    {}

    public WorkflowSummary(String processDefinitionKey, User user, Container container)
    {
        _processDefinitionKey = processDefinitionKey;
        _engineProcessDefinition = WorkflowManager.get().getProcessDefinition(processDefinitionKey, container);
    }

    public String getWorkflowModelModule()
    {
        if (_engineProcessDefinition == null)
            return null;
        else
        {
            Lsid lsid = new Lsid(_engineProcessDefinition.getCategory());
            return lsid.getObjectId();
        }
    }

    public boolean canStartProcess(User user, Container container)
    {
        PermissionsHandler handler = WorkflowRegistry.get().getPermissionsHandler(getWorkflowModelModule(), user, container);
        if (handler != null)
            return handler.canStartProcess(getProcessDefinitionKey());
        else
            return false;
    }

    public String getName()
    {
        if (_engineProcessDefinition == null)
            return null;
        else if (_engineProcessDefinition.getName() == null)
            return "Process Definition " + _engineProcessDefinition.getId();
        else
            return _engineProcessDefinition.getName();
    }

    public String getDescription()
    {
        if (_engineProcessDefinition == null)
            return null;
        else
            return _engineProcessDefinition.getDescription();
    }

    public boolean hasDiagram()
    {
        return _engineProcessDefinition != null && _engineProcessDefinition.getDiagramResourceName() != null;
    }

    public File getModelFile()
    {
        File file = null;
        if (_engineProcessDefinition != null)
        {
            file = new File(_engineProcessDefinition.getResourceName());
        }
        return file;
    }


    public String getProcessDefinitionKey()
    {
        return _processDefinitionKey;
    }

    public void setProcessDefinitionKey(String processDefinitionKey)
    {
        _processDefinitionKey = processDefinitionKey;
    }

    public Long getNumInstances()
    {
        return _numInstances;
    }

    public void setNumInstances(Long numInstances)
    {
        _numInstances = numInstances;
    }

    public Long getNumAssignedTasks()
    {
        return _numAssignedTasks;
    }

    public void setNumAssignedTasks(Long numAssignedTasks)
    {
        _numAssignedTasks = numAssignedTasks;
    }

    public Map<UserPrincipal, Long> getNumGroupTasks()
    {
        return _numGroupTasks;
    }

    public void setNumGroupTasks(Map<UserPrincipal, Long> numGroupTasks)
    {
        _numGroupTasks = numGroupTasks;
    }

    public Long getNumOwnedTasks()
    {
        return _numOwnedTasks;
    }

    public void setNumOwnedTasks(Long numOwnedTasks)
    {
        _numOwnedTasks = numOwnedTasks;
    }

    public Long getNumTotalTasks()
    {
        return _numTotalTasks;
    }

    public void setNumTotalTasks(Long numTotalTasks)
    {
        _numTotalTasks = numTotalTasks;
    }

}
