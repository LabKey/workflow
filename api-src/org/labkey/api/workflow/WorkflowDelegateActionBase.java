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
package org.labkey.api.workflow;

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;

import java.util.Map;

/**
 * Created by susanh on 8/2/15.
 */
public class WorkflowDelegateActionBase
{
    protected final Map<String, Object> _variables;
    protected WorkflowProcess _process;
    protected final Container _container;
    protected final User _initiator;

    public WorkflowDelegateActionBase(Map<String, Object> variables)
    {
        _variables = variables;
        _container = ContainerManager.getForId((String) _variables.get(WorkflowProcess.CONTAINER_ID));
        _initiator = UserManager.getUser(Integer.valueOf((String) _variables.get(WorkflowProcess.INITIATOR_ID)));
    }

    public WorkflowDelegateActionBase(WorkflowProcess process)
    {
        this(process.getVariables());
        _process = process;
    }

    public WorkflowProcess getProcess()
    {
        return _process;
    }

    public Map<String, Object> getVariables()
    {
        return _variables;
    }

    public Container getContainer()
    {
        return _container;
    }

    public User getInitiator()
    {
        return _initiator;
    }

    public Object getVariable(String key)
    {
        return _variables.get(key);
    }

    public void setVariable(String key, Object value)
    {
        _variables.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public Object getDataAccessParameter(String key)
    {
        Object dataAccess = _variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                return ((Map<String, Object>)parameters).get(key);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void setDataAccessParameter(String key, Object value)
    {
        Object dataAccess = _variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                ((Map<String, Object>)parameters).put(key, value);
        }
    }

    public boolean shouldAddUINotification()
    {
        return false;
    }

    public String getUINotificationType()
    {
        return null;
    }
}
