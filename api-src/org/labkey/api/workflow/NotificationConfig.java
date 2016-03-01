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

/**
 * Class that is used to notify a set of users from a service task within a workflow.
 * Created by susanh on 6/9/15.
 */

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.util.emailTemplate.EmailTemplate;

import java.util.List;
import java.util.Map;

public abstract class NotificationConfig extends WorkflowDelegateActionBase
{
    public NotificationConfig(Map<String, Object> variables)
    {
        super(variables);
    }

    public abstract List<User> getUsers();

    public abstract EmailTemplate getEmailTemplate(String processInstanceId, Map<String, Object> variables);

    public abstract Container getContainer();

    @Nullable
    public abstract User getLogUser();
}
