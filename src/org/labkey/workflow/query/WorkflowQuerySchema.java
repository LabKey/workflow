package org.labkey.workflow.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.workflow.WorkflowModule;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowQuerySchema extends UserSchema
{
    public static final String NAME = "workflow";
    public static final String DESCRIPTION = "Provides information on workflow models, processes and tasks";

    public static final String TABLE_TASK = "act_ru_task";
    public static final String TABLE_PROCESS_DEFINITION = "act_re_procdef";
    public static final String TABLE_PROCESS_INSTANCE = "act_ru_execution";
    public static final String TABLE_DEPLOYMENT = "act_re_deployment";
    public static final String TABLE_VARIABLE = "act_ru_variable";

    public WorkflowQuerySchema(User user, Container container)
    {
        super(NAME, DESCRIPTION, user, container, DbSchema.get(NAME, DbSchemaType.Module));
    }

    public static void register(final WorkflowModule module)
    {
        DefaultSchema.registerProvider(NAME, new DefaultSchema.SchemaProvider(module)
        {
            @Override
            public QuerySchema createSchema(DefaultSchema schema, Module module)
            {
                return new WorkflowQuerySchema(schema.getUser(), schema.getContainer());
            }
        });
    }

    @Override
    public Set<String> getTableNames()
    {
        Set<String> names = new HashSet<>();

        names.add(TABLE_TASK);
        names.add(TABLE_DEPLOYMENT);
        names.add(TABLE_PROCESS_DEFINITION);
        names.add(TABLE_PROCESS_INSTANCE);
        names.add(TABLE_VARIABLE);
        return names;
    }

    @Nullable
    @Override
    protected TableInfo createTable(String name)
    {
        if (name.equals(TABLE_TASK))
            return new WorkflowTaskTable(this);
        else if (name.equals(TABLE_PROCESS_DEFINITION))
            return new WorkflowProcessDefinitionTable(this);
        else if (name.equals(TABLE_PROCESS_INSTANCE))
            return new WorkflowProcessInstanceTable(this);
        else if (name.equals(TABLE_DEPLOYMENT))
            return new WorkflowTenantTable(this, name, null);

        //just return a filtered table over the db table if it exists
        SchemaTableInfo tableInfo = getDbSchema().getTable(name);
        if (null == tableInfo)
            return null;

        FilteredTable filteredTable = new FilteredTable<>(tableInfo, this);
        filteredTable.wrapAllColumns(true);
        return filteredTable;
    }
}
