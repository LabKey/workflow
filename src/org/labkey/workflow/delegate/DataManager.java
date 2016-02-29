/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
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

    @SuppressWarnings({"unused"})
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
