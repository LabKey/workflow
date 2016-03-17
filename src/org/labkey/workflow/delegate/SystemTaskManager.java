/*
 * Copyright (c) 2016 LabKey Corporation
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
package org.labkey.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.labkey.api.workflow.SystemTaskRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 *
 * User: tgaluhn
 * Date: 1/22/2016
 */
public class SystemTaskManager implements JavaDelegate
{
    private Expression _taskRunnerClassName;

    public Expression getTaskRunnerClassName()
    {
        return _taskRunnerClassName;
    }

    @SuppressWarnings({"unused"})
    public void setTaskRunnerClassName(Expression taskRunnerClassName)
    {
        _taskRunnerClassName = taskRunnerClassName;
    }

    @SuppressWarnings({"unchecked"})
    private Class<SystemTaskRunner> getTaskRunnerClass(DelegateExecution execution) throws ClassNotFoundException
    {
        return (Class<SystemTaskRunner>) Class.forName((String) getTaskRunnerClassName().getValue(execution));
    }

    private SystemTaskRunner getTaskRunnerInstance(DelegateExecution execution) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
    {
        return getTaskRunnerClass(execution).getDeclaredConstructor(Map.class).newInstance(execution.getVariables());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        SystemTaskRunner runner = getTaskRunnerInstance(execution);
        runner.doAction();
        execution.setVariables(runner.getVariables());
        runner.afterVariablesUpdated();
    }
}
