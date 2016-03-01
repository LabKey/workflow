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
import org.labkey.api.security.User;

/**
 * Created by susanh on 7/22/15.
 */
public abstract class WorkflowProcessEventListener
{
    protected WorkflowProcess _process;
    protected User _user;
    protected Container _container;

    public WorkflowProcessEventListener(WorkflowProcess process, User user, Container container)
    {
        _process = process;
        _user = user;
        _container = container;
    }

    /**
     * Called when an entity create event is received.
     */
    public void onCreate()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity initialized event is received.
     */
    public void onInitialized()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity delete event is received.
     */
    public void onDelete()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity update event is received.
     */
    public void onUpdate()
    {
        // NO-OP for now
    }

}
