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

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.BaseEntityEventListener;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.view.HttpView;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowProcessEventListener;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.workflow.model.WorkflowProcessImpl;

/**
 * Created by susanh on 7/22/15.
 *
 * N.B.  From the Activiti docs: "there will only be a single instance of [this] class created. Make sure the listener
 * implementations do not rely on member-fields or ensure safe usage from multiple threads/contexts."
 */
public class ProcessInstanceEventListener extends BaseEntityEventListener
{
    @Override
    protected void onCreate(ActivitiEvent event)
    {
        WorkflowProcessEventListener listener = getListener(event.getProcessInstanceId());
        if (listener != null)
            listener.onCreate();
    }

    @Override
    protected void onInitialized(ActivitiEvent event)
    {
        WorkflowProcessEventListener listener = getListener(event.getProcessInstanceId());
        if (listener != null)
            listener.onInitialized();
    }

    @Override
    protected void onDelete(ActivitiEvent event)
    {
        WorkflowProcessEventListener listener = getListener(event.getProcessInstanceId());
        if (listener != null)
            listener.onDelete();
    }

    @Override
    protected void onUpdate(ActivitiEvent event)
    {

        WorkflowProcessEventListener listener = getListener(event.getProcessInstanceId());
        if (listener != null)
            listener.onUpdate();
    }

    @Nullable
    private WorkflowProcessEventListener getListener(String processInstanceId)
    {
        Container container = null;
        User user = null;
        if (HttpView.hasCurrentView())
        {
            container = HttpView.currentContext().getContainer();
            user = HttpView.currentContext().getUser();
        }
        WorkflowProcess instance = new WorkflowProcessImpl(WorkflowManager.get().getProcessInstance(processInstanceId));
        if (instance != null)
            return WorkflowRegistry.get().getWorkflowProcessEventListener(instance, user, container);
        return null;
    }
}
