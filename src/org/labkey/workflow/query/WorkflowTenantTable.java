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
        ColumnInfo containerColumn = getColumn("Tenant_id_");
        containerColumn.setFk(new ContainerForeignKey(getUserSchema()));
        containerColumn.setUserEditable(false);
        containerColumn.setShownInInsertView(false);
        containerColumn.setShownInUpdateView(false);
        containerColumn.setLabel("Container");
    }


}
