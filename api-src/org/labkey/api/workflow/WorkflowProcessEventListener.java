/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
package org.labkey.api.workflow;

import org.labkey.api.data.Container;
import org.labkey.api.security.User;

/**
 * Created by susanh on 7/22/15.
 */
public abstract class WorkflowProcessEventListener
{
    protected WorkflowProcess _process;
    protected User _user;
    protected Container _container;

    public WorkflowProcessEventListener(WorkflowProcess process, User user, Container container)
    {
        _process = process;
        _user = user;
        _container = container;
    }

    /**
     * Called when an entity create event is received.
     */
    public void onCreate()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity initialized event is received.
     */
    public void onInitialized()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity delete event is received.
     */
    public void onDelete()
    {
        // NO-OP for now
    }

    /**
     * Called when an entity update event is received.
     */
    public void onUpdate()
    {
        // NO-OP for now
    }

}
