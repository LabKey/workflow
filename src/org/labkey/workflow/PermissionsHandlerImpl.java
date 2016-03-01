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
package org.labkey.workflow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowTask;

import java.util.Collections;
import java.util.Set;

/**
 * Created by susanh on 5/27/15.
 */
public class PermissionsHandlerImpl extends PermissionsHandler
{

    public PermissionsHandlerImpl(@NotNull User user, @NotNull Container container)
    {
        super (user, container);
    }


    @Override
    public SimpleFilter getProcessListFilter()
    {
        SimpleFilter filter = new SimpleFilter();
        if (!_hasAdmin) // admins can see everything by default
        {
            filter.addClause(getInitiatorCondition());
        }
        return filter;
    }

    @Override
    public SimpleFilter getTaskListFilter()
    {
        SimpleFilter filter = new SimpleFilter();
        if (!_hasAdmin) // admins can see everything by default
        {
            filter.addClause(getAssigneeOwnerClause()); // otherwise you see tasks you are assigned or that you own
        }
        return filter;
    }

    @Override
    public Set<Class<? extends Permission>> getCandidateUserPermissions(@NotNull WorkflowTask task)
    {
        return Collections.<Class<? extends Permission>>singleton(AdminPermission.class);
    }
}
