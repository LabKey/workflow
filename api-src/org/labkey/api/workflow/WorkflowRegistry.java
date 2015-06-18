package org.labkey.api.workflow;

import org.labkey.api.data.Container;
import org.labkey.api.module.Module;
import org.labkey.api.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class for registering handlers, listeners, etc. for workflow processes.
 *
 * Created by susanh on 5/27/15.
 */
public class WorkflowRegistry
{
    private static final Logger _log = LoggerFactory.getLogger(WorkflowRegistry.class);
    private static String _defaultHandler = null;
    private static final WorkflowRegistry _instance = new WorkflowRegistry();
    private static final Map<String, Class<? extends PermissionsHandler>> _permissionsRegistry = new ConcurrentHashMap<>();

    private WorkflowRegistry()
    {
        // private so there can be only one instance created
    }

    public static WorkflowRegistry get() { return _instance; }

    public static void registerPermissionsHandler(Module module, Class<? extends PermissionsHandler> handler)
    {
       registerPermissionsHandler(module, handler, false);
    }

    public static void registerPermissionsHandler(Module module, Class<? extends PermissionsHandler> handler, Boolean isDefault)
    {
        _permissionsRegistry.put(module.getName(), handler);
        if (isDefault)
            _defaultHandler = module.getName();

    }

    public PermissionsHandler getPermissionsHandler(String moduleName, User user, Container container)
    {
        Class<? extends PermissionsHandler> handler;
        if (null == moduleName)
            handler = _permissionsRegistry.get(_defaultHandler);
        else
        {
            handler = _permissionsRegistry.get(moduleName);
            if (null == handler)
                handler = _permissionsRegistry.get(_defaultHandler);
        }
        try
        {
            return handler.getDeclaredConstructor(User.class, Container.class).newInstance(user, container);
        }
        catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e)
        {
            _log.error("Unable to instantiate permissions handler for module " + moduleName, e);
            return null;
        }
    }
}

