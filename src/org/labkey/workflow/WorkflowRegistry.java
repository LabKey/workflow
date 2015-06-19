/*
 * Copyright (c) 2015 LabKey Corporation
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

import org.labkey.api.module.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by susanh on 5/27/15.
 */
public class WorkflowRegistry
{
    private static final String DEFAULT_HANDLER = WorkflowModule.NAME;
    private static WorkflowRegistry _instance = new WorkflowRegistry();
    private static Map<String, PermissionsHandler> _permissionsRegistry = new HashMap<String, PermissionsHandler>();

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
        if (_permissionsRegistry.containsKey(moduleName))
            return _permissionsRegistry.get(moduleName);
        else
            return _permissionsRegistry.get(DEFAULT_HANDLER);
    }
}

