/*
 * Copyright (c) 2015 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        ColumnInfo containerColumn = getColumn("tenant_id_");
        containerColumn.setFk(new ContainerForeignKey(getUserSchema()));
        containerColumn.setUserEditable(false);
        containerColumn.setShownInInsertView(false);
        containerColumn.setShownInUpdateView(false);
        containerColumn.setLabel("Container");
    }


}
