package org.labkey.workflow.delegate;

/**
 * Class that is used to notify a set of users from a service task within a workflow.
 * Created by susanh on 6/9/15.
 */

import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.util.emailTemplate.EmailTemplate;
import org.labkey.workflow.model.WorkflowProcess;

import java.util.List;

public interface Notification
{
    List<User> getUsers(Container container);
    EmailTemplate getEmailTemplate(Container container);
    void setWorkflowProcess(WorkflowProcess process);
    WorkflowProcess getWorkflowProcess();
}
