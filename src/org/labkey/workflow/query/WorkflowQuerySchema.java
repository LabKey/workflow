/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow.query;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.CompareType;
import org.labkey.api.data.Container;
import org.labkey.api.data.DbSchema;
import org.labkey.api.data.DbSchemaType;
import org.labkey.api.data.DetailsColumn;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.JavaScriptDisplayColumn;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.SchemaTableInfo;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.module.Module;
import org.labkey.api.query.AliasedColumn;
import org.labkey.api.query.DefaultSchema;
import org.labkey.api.query.DetailsURL;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.QuerySchema;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.ViewContext;
import org.labkey.workflow.WorkflowController;
import org.labkey.workflow.WorkflowModule;
import org.springframework.validation.BindException;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
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
    public static final String TABLE_RUNTIME_JOB = "act_ru_job";
    public static final String TABLE_HISTORY_ACTIVITY_INSTANCE = "act_hi_actinst";
    public static final String TABLE_HISTORY_COMMENT = "act_hi_comment";
    public static final String TABLE_HISTORY_DETAIL = "act_hi_detail";
    public static final String TABLE_HISTORY_IDENTITY_LINK = "act_hi_identitylink";
    public static final String TABLE_HISTORY_PROCESS_INSTANCE = "act_hi_procinst";
    public static final String TABLE_HISTORY_TASK_INSTANCE = "act_hi_taskinst";
    public static final String TABLE_HISTORY_VARIABLE_INSTANCE = "act_hi_varinst";

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
        names.add(TABLE_RUNTIME_JOB);
        names.add(TABLE_HISTORY_ACTIVITY_INSTANCE);
        names.add(TABLE_HISTORY_COMMENT);
        names.add(TABLE_HISTORY_IDENTITY_LINK);
        names.add(TABLE_HISTORY_DETAIL);
        names.add(TABLE_HISTORY_PROCESS_INSTANCE);
        names.add(TABLE_HISTORY_TASK_INSTANCE);
        names.add(TABLE_HISTORY_VARIABLE_INSTANCE);
        return names;
    }

    @Nullable
    @Override
    protected TableInfo createTable(String name)
    {
        switch (name)
        {
            case TABLE_TASK:
                return new WorkflowTaskTable(this, getUser(), getContainer());
            case TABLE_PROCESS_DEFINITION:
                return new WorkflowProcessDefinitionTable(this);
            case TABLE_PROCESS_INSTANCE:
                return new WorkflowProcessInstanceTable(this, getUser(), getContainer());
            case TABLE_IDENTITY_LINK:
                return new WorkflowIdentityLinkTable(this, getUser(), getContainer());
            case TABLE_VARIABLE:
                return new WorkflowVariableTable(this, getUser(), getContainer());
            case TABLE_DEPLOYMENT:
                return new WorkflowTenantTable(this, name);
        }

        //just return a filtered table over the db table if it exists
        SchemaTableInfo tableInfo = getDbSchema().getTable(name);
        if (null == tableInfo)
            return null;

        FilteredTable filteredTable = new FilteredTable<>(tableInfo, this);
        filteredTable.wrapAllColumns(true);
        return filteredTable;
    }

    @Override
    public QueryView createView(ViewContext context, @NotNull QuerySettings settings, BindException errors)
    {
        QueryView queryView = null;

        String processDefinitionKey = context.getRequest().getParameter("processDefinitionKey");
        if (StringUtils.isNotBlank(processDefinitionKey))
        {
            SimpleFilter filter = settings.getBaseFilter();
            FieldKey definitionKeyField = new FieldKey(null, "proc_def_id_");
            // These keys have the form <key string>:revision:id, so we add the ":" to distinguish keys with the same prefix.
            SimpleFilter processFilter = new SimpleFilter(definitionKeyField, processDefinitionKey + ":", CompareType.CONTAINS);
            filter.addAllClauses(processFilter);
        }


        if (settings.getQueryName().equalsIgnoreCase(TABLE_TASK))
        {
            String assigneeId = context.getRequest().getParameter("assignee");
            SimpleFilter baseFilter = settings.getBaseFilter();
            if (StringUtils.isNotBlank(assigneeId))
            {
                FieldKey field = new FieldKey(null, "assignee_");
                SimpleFilter assigneeFilter;
                if (assigneeId.equals("_blank"))
                {
                    assigneeFilter = new SimpleFilter(field, assigneeId, CompareType.ISBLANK);
                }
                else
                {
                    assigneeFilter = new SimpleFilter(field, assigneeId, CompareType.EQUAL);
                }
                baseFilter.addAllClauses(assigneeFilter);
            }

            String ownerId = context.getRequest().getParameter("owner");
            if (StringUtils.isNotBlank(ownerId))
            {
                FieldKey field = new FieldKey(null, "owner_");
                SimpleFilter ownerFilter;
                if (ownerId.equals("_blank"))
                {
                    ownerFilter = new SimpleFilter(field, ownerId, CompareType.ISBLANK);
                }
                else
                {
                    ownerFilter = new SimpleFilter(field, ownerId, CompareType.EQUAL);
                }
                baseFilter.addAllClauses(ownerFilter);
            }

            queryView =  new QueryView(this, settings, errors)
            {
                @Override
                protected void addDetailsAndUpdateColumns(List<DisplayColumn> ret, TableInfo table)
                {
                    ActionURL base = new ActionURL(WorkflowController.TaskAction.class, getContainer());
                    DetailsURL detailsURL = new DetailsURL(base, Collections.singletonMap("taskId", "id_"));
                    setDetailsURL(detailsURL.toString());
                    DetailsColumn column = new DetailsColumn(detailsURL, table);
                    column.setDisplayHtml("Task Details");
                    ret.add(column);
                    ret.add(getReassignmentColumn(table, "Reassign", null));
                }
            };
        }
        else
        {
            queryView = new QueryView(this, settings, errors);
        }
//        queryView.setShowDeleteButton(settings.getQueryName().equalsIgnoreCase(TABLE_PROCESS_INSTANCE));
        queryView.setShowDeleteButton(false);
        queryView.setShowUpdateColumn(false);
        queryView.setShowInsertNewButton(false);
        queryView.setShowImportDataButton(false);
        return queryView;
    }

    private DisplayColumn getReassignmentColumn(@NotNull TableInfo table, @NotNull String linkText, @Nullable String title)
    {
        AliasedColumn reassignmentColumn = new AliasedColumn(linkText, table.getColumn("id_"));
        reassignmentColumn.setDisplayColumnFactory(new ReassignmentDisplayColumnFactory(linkText));
        return reassignmentColumn.getRenderer();
    }

    private class ReassignmentDisplayColumnFactory implements DisplayColumnFactory
    {
        private final String _assignmentType;

        public ReassignmentDisplayColumnFactory(String assignmentType)
        {
            _assignmentType = assignmentType;
        }

        @Override
        public DisplayColumn createRenderer(ColumnInfo colInfo)
        {
            Collection<String> dependencies = Collections.singletonList("workflow/view/reassignTask.js");
            String javaScriptEvent = "onclick=\"createReassignTaskWindow(${id_:jsString}, ${assignee_:jsString});\"";
            return new JavaScriptDisplayColumn(colInfo, dependencies, javaScriptEvent, "labkey-text-link")
            {
                @NotNull
                @Override
                public String getFormattedValue(RenderContext ctx)
                {
                    return _assignmentType;
                }

                @Override
                public void renderTitle(RenderContext ctx, Writer out) throws IOException
                {
                    // no title for these columns
                }

                @Override
                public boolean isSortable()
                {
                    return false;
                }

                @Override
                public boolean isFilterable()
                {
                    return false;
                }

                @Override
                public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
                {
                    // TODO add in the proper permissions check here, passed in as part of the constructor
                    String group = (String) ctx.get("_Group");
                    if (group == null)
                        return;
                    Integer groupId = Integer.valueOf(group);
                    if (getUser().isInGroup(groupId) || getContainer().hasPermission(getUser(), AdminPermission.class))
                    {
                        super.renderGridCellContents(ctx, out);
                    }
                }
            };
        }
    }
}
