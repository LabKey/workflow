/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.api.workflow;

import java.util.Date;

/**
 * Created by susanh on 7/23/15.
 */
public interface WorkflowJob
{
    Date getDueDate();

    String getId();

    String getProcessInstanceId();

    String getExecutionId();

    String getProcessDefinitionId();

    int getRetries();

    String getExceptionMessage();

    String getContainerId();
}
