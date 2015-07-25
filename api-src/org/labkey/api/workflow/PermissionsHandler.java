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
package org.labkey.api.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;

import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 *
 */
public abstract class PermissionsHandler
{
    protected final User _user;
    protected final Container _container;

    public PermissionsHandler(User user, Container container)
    {
        _user = user;
        _container = container;
    }

    public abstract boolean canStartProcess(@NotNull String processDefinitionKey);

    public abstract boolean canDeployProcess(@NotNull String processDefinitionKey);

    public abstract boolean canView(@NotNull WorkflowProcess process);

    public abstract boolean canAccessData(@NotNull WorkflowProcess process);

    public abstract boolean canDelete(@NotNull WorkflowProcess process);

    public abstract boolean canClaim(@NotNull WorkflowTask task);

    public abstract boolean canDelegate(@NotNull WorkflowTask task);

    public abstract boolean canAssign(@NotNull WorkflowTask task);

    public abstract boolean canView(@NotNull WorkflowTask task);

    public abstract boolean canAccessData(@NotNull WorkflowTask task);

    public abstract boolean canComplete(@NotNull WorkflowTask task);

    public abstract Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task);

    @SuppressWarnings("unchecked")
    public Object getDataAccessParameter(Map<String, Object> variables, String key)
    {
        Object dataAccess = variables.get(WorkflowProcess.DATA_ACCESS_KEY);
        if (null != dataAccess && dataAccess instanceof Map)
        {
            Object parameters = ((Map<String, Object>) dataAccess).get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);
            if (null != parameters && parameters instanceof Map)
                return ((Map<String, Object>)parameters).get(key);
        }
        return null;
    }
}
