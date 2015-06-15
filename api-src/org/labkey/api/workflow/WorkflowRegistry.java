package org.labkey.api.workflow;

import org.labkey.api.module.Module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for registering handlers, listeners, etc. for workflow processes.
 *
 * Created by susanh on 5/27/15.
 */
public class WorkflowRegistry
{
    private static String defaultHandler = null;
    private static final WorkflowRegistry _instance = new WorkflowRegistry();
    private static final Map<String, PermissionsHandler> _permissionsRegistry = new ConcurrentHashMap<>();

    private WorkflowRegistry()
    {
        // private so there can be only one instance created
    }

    public static WorkflowRegistry get() { return _instance; }

    public static void registerPermissionsHandler(Module module, PermissionsHandler handler)
    {
       registerPermissionsHandler(module, handler, false);
    }

    public static void registerPermissionsHandler(Module module, PermissionsHandler handler, Boolean isDefault)
    {
        _permissionsRegistry.put(module.getName(), handler);
        if (isDefault)
            defaultHandler = module.getName();

    }

    public PermissionsHandler getPermissionsHandler(String moduleName)
    {
        PermissionsHandler handler = _permissionsRegistry.get(moduleName);
        return null != handler ? handler : _permissionsRegistry.get(defaultHandler);
    }
}

