/*
 * Copyright (c) 2015-2019 LabKey Corporation
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
import org.labkey.api.data.ContainerManager;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.view.SimpleWebPartFactory;
import org.labkey.api.view.WebPartFactory;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.api.workflow.WorkflowService;
import org.labkey.workflow.query.WorkflowQuerySchema;
import org.labkey.workflow.view.WorkflowWebPart;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class WorkflowModule extends DefaultModule
{
    public static final String NAME = "Workflow";

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public @Nullable Double getSchemaVersion()
    {
        return 20.000;
    }

    @Override
    public boolean hasScripts()
    {
        return true;
    }

    @Override
    protected void init()
    {
        addController(WorkflowController.NAME, WorkflowController.class);
        WorkflowQuerySchema.register(this);
        WorkflowRegistry.registerPermissionsHandler(this, PermissionsHandlerImpl.class, true);
        WorkflowService.setInstance(WorkflowManager.get());
    }

    @Override
    public void doStartup(ModuleContext moduleContext)
    {
        // add a container listener so we'll know when our container is deleted:
        ContainerManager.addContainerListener(new WorkflowContainerListener());
    }

    @Override
    @NotNull
    public Set<String> getSchemaNames()
    {
        return Collections.singleton(WorkflowSchema.NAME);
    }

    @NotNull
    @Override
    public Set<Class> getUnitTests()
    {
        return Set.of(
            WorkflowManager.TestCase.class
        );
    }

    @NotNull
    @Override
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return List.of(
            new SimpleWebPartFactory("Workflow List", WebPartFactory.LOCATION_BODY, WorkflowWebPart.class, null)
        );
    }
}