package org.labkey.workflow.query;

import org.labkey.api.data.ColumnInfo;
import org.labkey.api.query.FilteredTable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowProcessInstanceTable extends WorkflowTenantTable
{

    private static Set<String> _columnsToIgnore = new HashSet<>();
    static
    {
        _columnsToIgnore.add("business_key_");
        _columnsToIgnore.add("super_exec_");
        _columnsToIgnore.add("act_id_");

    }

    public WorkflowProcessInstanceTable(WorkflowQuerySchema userSchema)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_PROCESS_INSTANCE, null);
    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !_columnsToIgnore.contains(columnInfo.getName().toLowerCase());
    }
}
