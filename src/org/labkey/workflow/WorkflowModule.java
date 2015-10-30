/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */

package org.labkey.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.module.DefaultModule;
import org.labkey.api.module.ModuleContext;
import org.labkey.api.view.WebPartFactory;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.workflow.query.WorkflowQuerySchema;

import java.util.Collection;
import java.util.Collections;
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
    public double getVersion()
    {
        return 15.30;
    }

    @Override
    public boolean hasScripts()
    {
        return true;
    }

    @Override
    @NotNull
    protected Collection<WebPartFactory> createWebPartFactories()
    {
        return Collections.emptyList();
    }

    @Override
    protected void init()
    {
        addController(WorkflowController.NAME, WorkflowController.class);
        WorkflowQuerySchema.register(this);
        WorkflowRegistry.registerPermissionsHandler(this, PermissionsHandlerImpl.class, true);
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
}