package org.labkey.api.workflow;

/**
 * Class that is used to notify a set of users from a service task within a workflow.
 * Created by susanh on 6/9/15.
 */

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.util.emailTemplate.EmailTemplate;

import java.util.List;
import java.util.Map;

public abstract class NotificationConfig
{
    protected final Map<String, Object> _variables;

    public NotificationConfig(Map<String, Object> variables)
    {
        _variables = variables;
    }

    public abstract List<User> getUsers();

    public abstract EmailTemplate getEmailTemplate(String processInstanceId, Map<String, Object> variables);

    public abstract Container getContainer();

    @Nullable
    public abstract User getLogUser();
}
