package org.labkey.workflow;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.module.Module;
import org.labkey.api.module.ModuleResourceLoadException;
import org.labkey.api.module.ModuleResourceLoader;
import org.labkey.api.pipeline.PipelineJobService;
import org.labkey.api.pipeline.PipelineService;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Created by susanh on 5/16/15.
 */
public class WorkflowModuleResourceLoader implements ModuleResourceLoader
{
    @NotNull
    @Override
    public Set<String> getModuleDependencies(Module module, File explodedModuleDir)
    {
        File dir = new File(explodedModuleDir, WorkflowManager.WORKFLOW_MODEL_DIR);
        if (dir.exists())
            return Collections.singleton(WorkflowModule.NAME);
        return Collections.emptySet();
    }

    @Override
    public void registerResources(Module module) throws IOException, ModuleResourceLoadException
    {
        File dir = new File(module.getExplodedPath(), WorkflowManager.WORKFLOW_MODEL_DIR);
        if (dir.exists())
            WorkflowManager.get().registerModule(module);
    }
}
