package org.labkey.workflow.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.ContainerForeignKey;
import org.labkey.api.query.SimpleUserSchema;

import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowTenantTable extends SimpleUserSchema.SimpleTable<WorkflowQuerySchema>
{
    private Set<String> _columnsToIgnore;

    public WorkflowTenantTable(WorkflowQuerySchema userSchema, String tableName, @Nullable Set<String> columnsToIgnore)
    {
        super(userSchema, userSchema.getDbSchema().getTable(tableName));
        this._columnsToIgnore = columnsToIgnore;
        wrapAllColumns();
    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return _columnsToIgnore == null || !_columnsToIgnore.contains(columnInfo.getName());
    }

    @Override
    public void wrapAllColumns()
    {
        super.wrapAllColumns();
        ColumnInfo containerColumn = getColumn("Tenant_id_");
        containerColumn.setFk(new ContainerForeignKey(getUserSchema()));
        containerColumn.setUserEditable(false);
        containerColumn.setShownInInsertView(false);
        containerColumn.setShownInUpdateView(false);
        containerColumn.setLabel("Container");
    }


}
