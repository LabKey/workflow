package org.labkey.workflow.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ColumnInfo;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowTaskTable extends WorkflowTenantTable
{

    public WorkflowTaskTable(@NotNull WorkflowQuerySchema userSchema)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_TASK, null);

    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !"form_key_".equalsIgnoreCase(columnInfo.getName().toLowerCase());
    }
}
