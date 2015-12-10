/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.labkey.api.admin.notification.Notification;
import org.labkey.api.admin.notification.NotificationService;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.settings.LookAndFeelProperties;
import org.labkey.api.util.ConfigurationException;
import org.labkey.api.util.ExceptionUtil;
import org.labkey.api.util.MailHelper;
import org.labkey.api.util.emailTemplate.EmailTemplate;
import org.labkey.api.workflow.NotificationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * A Java delegate that can be used within a workflow to send email to a set of users using an email template.
 *
 * N.B. There is a single instance of this class created for the serviceTask it is defined on. All process instances share
 * the smae class instance that will be used to all the execute method.
 * See http://activiti.org/userguide/index.html#bpmnJavaServiceTaskXML
 *
 * Created by susanh on 6/8/15.
 */
public class EmailNotifier implements JavaDelegate
{
    private static final Logger _log = LoggerFactory.getLogger(EmailNotifier.class);
    private Expression _notificationClassName;

    public Expression getNotificationClassName()
    {
        return _notificationClassName;
    }

    public void setNotificationClassName(Expression notificationClassName)
    {
        _notificationClassName = notificationClassName;
    }

    private Class<NotificationConfig> getNotificationClass(DelegateExecution execution) throws ClassNotFoundException
    {
        return (Class<NotificationConfig>) Class.forName((String) getNotificationClassName().getValue(execution));
    }

    private NotificationConfig getNotificationConfig(DelegateExecution execution) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException
    {
        return getNotificationClass(execution).getDeclaredConstructor(Map.class).newInstance(execution.getVariables());
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        NotificationConfig notificationConfig = getNotificationConfig(execution);
        Container container = notificationConfig.getContainer();
        EmailTemplate template = notificationConfig.getEmailTemplate(execution.getProcessInstanceId(), execution.getVariables());
        final List<User> allAddresses = notificationConfig.getUsers();
        for (User user : allAddresses)
        {
            // give the config a chance to add a UI notification to the system for this email message
            if (notificationConfig.shouldAddUINotification())
            {
                Notification notification = new Notification();
                notification.setUserId(user.getUserId());
                notification.setObjectId(execution.getProcessInstanceId());
                notification.setType(notificationConfig.getUINotificationType());

                NotificationService.get().addNotification(container, notificationConfig.getLogUser(), notification);
            }

            String to = user.getEmail();
            try
            {
                MailHelper.ViewMessage m = MailHelper.createMessage(LookAndFeelProperties.getInstance(container).getSystemEmailAddress(), to);
                Address[] addresses = m.getAllRecipients();
                if (addresses != null && addresses.length > 0)
                {
                    m.setSubject(template.renderSubject(container));
                    String body = template.renderBody(container);
                    m.setText(body);

                    MailHelper.send(m, notificationConfig.getLogUser(), container);
                }
            }
            catch (ConfigurationException | AddressException e)
            {
                _log.error("error sending service task notification email to " + to, e);
            }
            catch (Exception e)
            {
                _log.error("error sending service task notification email to " + to, e);
                ExceptionUtil.logExceptionToMothership(null, e);
            }
        }

    }

}
