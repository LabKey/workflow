/*
 * Copyright (c) 2016 LabKey Corporation
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
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * User: tgaluhn
 * Date: 1/25/2016
 */
public abstract class SystemTaskRunner extends WorkflowDelegateActionBase
{
    public SystemTaskRunner(Map<String, Object> variables)
    {
        super(variables);
    }

    public SystemTaskRunner(WorkflowProcess process)
    {
       super(process);
    }

    public abstract void doAction() throws Exception;


    @NotNull
    protected List<User> getUsers(Set<Class<? extends Permission>> permissions)
    {
        return _container != null ? SecurityManager.getUsersWithPermissions(_container, permissions) : Collections.emptyList();
    }

}
