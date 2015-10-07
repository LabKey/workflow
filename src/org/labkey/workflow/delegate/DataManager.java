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
package org.labkey.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.labkey.api.admin.notification.Notification;
import org.labkey.api.admin.notification.NotificationService;
import org.labkey.api.workflow.DataManagerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by davebradlee on 7/20/15.
 *
 */
public class DataManager implements JavaDelegate
{
    private static final Logger _log = LoggerFactory.getLogger(DataManager.class);
    private Expression _dataManagerClassName;

    public Expression getDataManagerClassName()
    {
        return _dataManagerClassName;
    }

    public void setDataManagerClassName(Expression dataManagerClassName)
    {
        _dataManagerClassName = dataManagerClassName;
    }

    @SuppressWarnings({"unchecked"})
    private Class<DataManagerConfig> getDataManagerClass(DelegateExecution execution) throws ClassNotFoundException
    {
        return (Class <DataManagerConfig>) Class.forName((String) getDataManagerClassName().getValue(execution));
    }

    private DataManagerConfig getDataManagerConfig(DelegateExecution execution) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
    {
        return getDataManagerClass(execution).getDeclaredConstructor(Map.class).newInstance(execution.getVariables());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        DataManagerConfig dataManagerConfig = getDataManagerConfig(execution);
        dataManagerConfig.doAction();
        execution.setVariables(dataManagerConfig.getVariables());

        // give the config a chance to add a UI notification to the system for this data manager action
        if (dataManagerConfig.shouldAddUINotification())
        {
            Notification notification = new Notification();
            notification.setUserId(dataManagerConfig.getInitiator().getUserId());
            notification.setObjectId(execution.getProcessInstanceId());
            notification.setType(dataManagerConfig.getUINotificationType());

            NotificationService.get().addNotification(dataManagerConfig.getContainer(), dataManagerConfig.getInitiator(), notification);
        }
    }
}
