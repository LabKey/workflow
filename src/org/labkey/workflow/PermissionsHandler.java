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

import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowTask;

import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public interface PermissionsHandler
{
    // TODO perhaps better to have a method getPermissions(task, user, container) if we define own Permission classes for workflow

    public boolean canStartProcess(String processDefinitionKey);

    public boolean canView(WorkflowProcess process, User user, Container container);

    public boolean canAccessData(WorkflowProcess process, User user, Container container);

    public boolean canDelete(WorkflowProcess process, User user, Container container);

    public boolean canClaim(WorkflowTask task, User user, Container container);

    public boolean canDelegate(WorkflowTask task, User user, Container container);

    public boolean canAssign(WorkflowTask task, User user, Container container);

    public boolean canView(WorkflowTask task, User user, Container container);

    public boolean canAccessData(WorkflowTask task, User user, Container container);

    public boolean isAssigned(WorkflowTask task, User user);

    public boolean canComplete(WorkflowTask task, User user, Container container);

    public Set<Class<? extends Permission>> getCandidateUserPermissions(WorkflowTask task, User user, Container container);

}
