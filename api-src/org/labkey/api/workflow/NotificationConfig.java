/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
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

public abstract class NotificationConfig extends WorkflowDelegateActionBase
{
    public NotificationConfig(Map<String, Object> variables)
    {
        super(variables);
    }

    public abstract List<User> getUsers();

    public abstract EmailTemplate getEmailTemplate(String processInstanceId, Map<String, Object> variables);

    public abstract Container getContainer();

    @Nullable
    public abstract User getLogUser();
}
