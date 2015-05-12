package org.labkey.workflow.query;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.FilteredTable;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowProcessDefinitionTable extends WorkflowTenantTable
{
    public WorkflowProcessDefinitionTable(WorkflowQuerySchema userSchema)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_PROCESS_DEFINITION, null);
    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !"has_start_form_key_".equalsIgnoreCase(columnInfo.getName());
    }

}
