/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
package org.labkey.workflow.query;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.query.SimpleUserSchema;

import java.util.List;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowTenantTable extends SimpleUserSchema.SimpleTable<WorkflowQuerySchema>
{

    public WorkflowTenantTable(WorkflowQuerySchema userSchema, String tableName)
    {
        super(userSchema, userSchema.getDbSchema().getTable(tableName));
        wrapAllColumns();
    }

    @Override
    protected String getContainerFilterColumn()
    {
        return "tenant_id_";
    }

    @Override
    public void wrapAllColumns()
    {
        super.wrapAllColumns();
        ColumnInfo containerColumn = getColumn("tenant_id_");
        containerColumn.setFk(new ContainerForeignKey(getUserSchema()));
        containerColumn.setUserEditable(false);
        containerColumn.setShownInInsertView(false);
        containerColumn.setShownInUpdateView(false);
        containerColumn.setLabel("Container");
    }

    protected void addWorkflowListFilter(List<SimpleFilter> filters)
    {
        if (filters.size() > 0)
        {
            SimpleFilter.OrClause or = new SimpleFilter.OrClause();
            for (SimpleFilter filter : filters)
            {
                List<SimpleFilter.FilterClause> clauses = filter.getClauses();
                for (SimpleFilter.FilterClause clause : clauses)
                {
                    or.addClause(clause);
                }
            }
            SimpleFilter filter = new SimpleFilter();
            filter.addClause(or);
            addCondition(filter);
        }
    }


}
