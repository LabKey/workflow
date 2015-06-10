package org.labkey.workflow;

import org.labkey.api.module.Module;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by susanh on 5/27/15.
 */
public class WorkflowRegistry
{
    private static final String DEFAULT_HANDLER = WorkflowModule.NAME;
    private static final WorkflowRegistry _instance = new WorkflowRegistry();
    private static final Map<String, PermissionsHandler> _permissionsRegistry = new ConcurrentHashMap<>();

    static
    {
        _permissionsRegistry.put(DEFAULT_HANDLER, new PermissionsHandlerImpl());
    }

    private WorkflowRegistry()
    {
        // private so there can be only one instance created
    }

    public static WorkflowRegistry get() { return _instance; }

    public static void registerPermissionsHandler(Module module, PermissionsHandler handler)
    {
        _permissionsRegistry.put(module.getName(), handler);
    }

    public PermissionsHandler getPermissionsHandler(String moduleName)
    {
        PermissionsHandler handler = _permissionsRegistry.get(moduleName);
        return null != handler ? handler : _permissionsRegistry.get(DEFAULT_HANDLER);
    }
}

