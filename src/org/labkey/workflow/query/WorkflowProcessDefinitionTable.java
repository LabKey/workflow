/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow.query;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.util.PageFlowUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowProcessDefinitionTable extends WorkflowTenantTable
{
    private static final Set<String> _columnsToIgnore = PageFlowUtil.set("has_start_form_key_", "category_");

    public WorkflowProcessDefinitionTable(WorkflowQuerySchema userSchema)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_PROCESS_DEFINITION);
    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !_columnsToIgnore.contains(columnInfo.getName().toLowerCase());
    }

}
