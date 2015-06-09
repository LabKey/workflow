package org.labkey.workflow.delegate;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.delegate.JavaDelegate;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.settings.LookAndFeelProperties;
import org.labkey.api.util.ConfigurationException;
import org.labkey.api.util.ExceptionUtil;
import org.labkey.api.util.MailHelper;
import org.labkey.api.util.emailTemplate.EmailTemplate;
import org.labkey.workflow.model.WorkflowProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import java.util.List;

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
    private static final Logger LOG = LoggerFactory.getLogger(EmailNotifier.class);
    private Expression _notificationClass;

    public Expression getNotificationClass()
    {
        return _notificationClass;
    }

    public void setNotificationClass(Expression notificationClass)
    {
        _notificationClass = notificationClass;
    }

    private Class<Notification> getNotificationClass(DelegateExecution execution) throws ClassNotFoundException
    {
        return (Class<Notification>) Class.forName((String) getNotificationClass().getValue(execution));
    }

    private Notification getNotifier(DelegateExecution execution) throws ClassNotFoundException, IllegalAccessException, InstantiationException
    {
        // initialize the process that is associated with this execution
        WorkflowProcess process = new WorkflowProcess(execution.getProcessInstanceId(), null);

        Notification notification = getNotificationClass(execution).newInstance();
        notification.setWorkflowProcess(process);
        return notification;
    }

    @Override
    public void execute(DelegateExecution execution) throws Exception
    {
        Notification notification = getNotifier(execution);


        Container container = notification.getWorkflowProcess().getContainer();
        EmailTemplate template = notification.getEmailTemplate(container);
        final List<User> allAddresses = notification.getUsers(container);
        for (User user : allAddresses)
        {
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

                    // TODO should this always be the initiator?  Or should it be null?
                    MailHelper.send(m, notification.getWorkflowProcess().getInitiator(), container);
                }
            }
            catch (ConfigurationException | AddressException e)
            {
                LOG.error("error sending service task notification email to " + to, e);
            }
            catch (Exception e)
            {
                LOG.error("error sending service task notification email to " + to, e);
                ExceptionUtil.logExceptionToMothership(null, e);
            }
        }

    }

}
