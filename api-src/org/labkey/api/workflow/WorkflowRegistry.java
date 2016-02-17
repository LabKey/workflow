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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.module.Module;
import org.labkey.api.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
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
    private static final Map<String, Class<? extends WorkflowProcessEventListener>> _processListenerRegistry = new ConcurrentHashMap<>();

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
        _permissionsRegistry.put(module.getName().toLowerCase(), handler);
        if (isDefault)
            _defaultHandler = module.getName().toLowerCase();

    }

    @Nullable
    public PermissionsHandler getPermissionsHandler(@Nullable String moduleName, User user, Container container)
    {
        Class<? extends PermissionsHandler> handlerClass;
        if (null == moduleName)
            handlerClass = _permissionsRegistry.get(_defaultHandler);
        else
        {
            handlerClass = _permissionsRegistry.get(moduleName.toLowerCase());
            if (null == handlerClass)
                handlerClass = _permissionsRegistry.get(_defaultHandler);
        }
        return createPermissionsHandler(handlerClass, user, container);
    }

    private static PermissionsHandler createPermissionsHandler(Class<? extends PermissionsHandler> handlerClass, User user, Container container)
    {
        try
        {
            return handlerClass.getDeclaredConstructor(User.class, Container.class).newInstance(user, container);
        }
        catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e)
        {
            _log.error("Unable to instantiate permissions handler for class " + handlerClass, e);
            return null;
        }
    }

    public static List<SimpleFilter> getProcessListFilters(User user, Container container)
    {
        List<SimpleFilter> filters = new ArrayList<>();
        for (Class<? extends PermissionsHandler> handlerClass : _permissionsRegistry.values())
        {
            PermissionsHandler handler = createPermissionsHandler(handlerClass, user, container);
            SimpleFilter filter = handler.getProcessListFilter();
            if (!filter.isEmpty())
                filters.add(filter);
        }
        return filters;
    }

    public static List<SimpleFilter> getTaskListFilters(User user, Container container)
    {
        List<SimpleFilter> filters = new ArrayList<>();
        for (Class<? extends PermissionsHandler> handlerClass : _permissionsRegistry.values())
        {
            PermissionsHandler handler = createPermissionsHandler(handlerClass, user, container);
            SimpleFilter filter = handler.getTaskListFilter();
            if (!filter.isEmpty())
                filters.add(filter);
        }
        return filters;
    }

    private static String getProcessListenerKey(String moduleName, String processDefinitionKey)
    {
        return moduleName.toLowerCase() + ":" + processDefinitionKey;
    }

    public static void registerWorkflowProcessEventListener(Module module, String processDefinitionKey, Class<? extends WorkflowProcessEventListener> listener)
    {
        _processListenerRegistry.put(getProcessListenerKey(module.getName(), processDefinitionKey), listener);
    }

    @Nullable
    public WorkflowProcessEventListener getWorkflowProcessEventListener(@NotNull WorkflowProcess instance, User user, Container container)
    {

        Class<? extends WorkflowProcessEventListener> listener = _processListenerRegistry.get(getProcessListenerKey(instance.getProcessDefinitionModule(), instance.getProcessDefinitionKey()));
        if (listener != null)
        {
            try
            {
                return listener.getDeclaredConstructor(WorkflowProcess.class, User.class, Container.class).newInstance(instance, user, container);
            }
            catch (InstantiationException|IllegalAccessException|InvocationTargetException|NoSuchMethodException e)
            {
                _log.error("Unable to instantiate process instance event listener for process " + instance.getName() + " with id " + instance.getId(), e);
                return null;
            }
        }
        return null;
    }
}

