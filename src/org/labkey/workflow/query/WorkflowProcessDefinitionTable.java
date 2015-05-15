package org.labkey.workflow.query;

import org.labkey.api.data.ColumnInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowProcessDefinitionTable extends WorkflowTenantTable
{
    private static final Set<String> _columnsToIgnore = new HashSet<>();
    static
    {
        _columnsToIgnore.add("has_start_form_key_");
        _columnsToIgnore.add("category_");
    }

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
