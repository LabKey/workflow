package org.labkey.workflow.query;

import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.SimpleDisplayColumn;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewContext;
import org.labkey.workflow.WorkflowController;
import org.labkey.workflow.WorkflowModule;
import org.springframework.validation.BindException;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
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
    public static final String TABLE_IDENTITY_LINK = "act_ru_identitylink";

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
        names.add(TABLE_DEPLOYMENT);
        names.add(TABLE_TASK);
        names.add(TABLE_PROCESS_DEFINITION);
        names.add(TABLE_PROCESS_INSTANCE);
        names.add(TABLE_VARIABLE);
        names.add(TABLE_IDENTITY_LINK);
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
            return new WorkflowTenantTable(this, name);

        //just return a filtered table over the db table if it exists
        SchemaTableInfo tableInfo = getDbSchema().getTable(name);
        if (null == tableInfo)
            return null;

        FilteredTable filteredTable = new FilteredTable<>(tableInfo, this);
        filteredTable.wrapAllColumns(true);
        return filteredTable;
    }

    @Override
    public QueryView createView(ViewContext context, QuerySettings settings, BindException errors)
    {
        QueryView queryView = new QueryView(this, settings, errors);

        if (settings.getQueryName().equalsIgnoreCase(TABLE_TASK))
        {
            queryView =  new QueryView(this, settings, errors)
            {
                @Override
                protected void addDetailsAndUpdateColumns(List<DisplayColumn> ret, TableInfo table)
                {
                    SimpleDisplayColumn actionColumn = new SimpleDisplayColumn()
                    {
                        @Override
                        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
                        {
                            Container c = ContainerManager.getForId(ctx.get(FieldKey.fromParts("tenant_id_")).toString());
                            Integer group  = (Integer) ctx.get("group_");
                            if (((group != null) && getUser().isInGroup(group)) || getContainer().hasPermission(getUser(), AdminPermission.class))
                            {
                                ActionURL claimUrl = new ActionURL(WorkflowController.ClaimTaskAction.class, c)
                                        .addParameter("ownerId", getUser().getUserId())
                                        .addParameter("assigneeId", getUser().getUserId())
                                        .addParameter("taskId", (String) ctx.get("id_"));
                                out.write(PageFlowUtil.textLink("Claim", claimUrl));
                            }
                            // TODO add the details link here
                        }
                    };
                    ret.add(actionColumn);
                }
            };
        }
        queryView.setShowDeleteButton(false);
        queryView.setShowUpdateColumn(false);
        queryView.setShowInsertNewButton(false);
        queryView.setShowImportDataButton(false);
        return queryView;
    }
}
