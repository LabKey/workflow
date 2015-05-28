package org.labkey.workflow.query;

import org.jetbrains.annotations.NotNull;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.data.CoreSchema;
import org.labkey.api.data.DataColumn;
import org.labkey.api.data.DisplayColumn;
import org.labkey.api.data.DisplayColumnFactory;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.RenderContext;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.DetailsURL;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.FilteredTable;
import org.labkey.api.query.LookupForeignKey;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.Group;
import org.labkey.api.security.SecurityUrls;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.view.ActionURL;
import org.labkey.workflow.WorkflowController;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowTaskTable extends WorkflowTenantTable
{
    private static Set<String> _columnsToIgnore = new HashSet<>();
    static
    {
        _columnsToIgnore.add("form_key_");
        _columnsToIgnore.add("category_");
        _columnsToIgnore.add("suspension_state_");
    }

    public WorkflowTaskTable(@NotNull WorkflowQuerySchema userSchema, User user, Container container)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_TASK);

        ColumnInfo idColumn = getColumn("id_");
        ActionURL base = new ActionURL(WorkflowController.TaskAction.class, getContainer());
        DetailsURL detailsURL = new DetailsURL(base, Collections.singletonMap("taskId", "id_"));
        idColumn.setURL(detailsURL);
        setDetailsURL(detailsURL);

        ColumnInfo groupCol = addCandidateGroupColumn(user, container);

        if (!container.hasPermission(user, AdminPermission.class))
            addCondition(new SQLFragment("owner_ = ? OR assignee_ = ?", user.getUserId(), user.getUserId()));
    }

    // TODO Remove this when we convert to Java 8 in favor of IntStream construction
    private List<Integer> getGroupList(int[] groupIds)
    {
        List<Integer> groupList = new ArrayList<>(groupIds.length);
        for (int i = 0; i < groupIds.length; i++)
        {
            groupList.add(groupIds[i]);
        }
        return groupList;
    }

    private ColumnInfo addCandidateGroupColumn(User user, Container container)
    {
        SQLFragment sql = new SQLFragment("(SELECT I.group_id_ FROM workflow.act_ru_identitylink I WHERE I.task_id_ = act_ru_task.id_");
        if (!container.hasPermission(user, AdminPermission.class))
        {
            sql.append(" AND (I.group_id_ IS NULL OR ");
            SimpleFilter.InClause clause = new SimpleFilter.InClause(FieldKey.fromParts("I","group_id_"), getGroupList(user.getGroups()));
            sql.append(clause.toSQLFragment(Collections.<FieldKey, ColumnInfo>emptyMap(), _schema.getSqlDialect()));
            sql.append(")");
        }
        sql.append(")");
//        SQLFragment frag = clause.toSQLFragment(Collections.<FieldKey, ColumnInfo>emptyMap(), _schema.getSqlDialect());
//        SQLFragment sql = new SQLFragment("(SELECT I.group_id_ FROM workflow.act_ru_identitylink I WHERE I.task_id_ = act_ru_task.id_ AND (I.group_id_ IS NULL OR ").append(frag).append("))");
        ExprColumn ret = new ExprColumn(this, "Group", sql, JdbcType.VARCHAR);

        ret.setFk(new GroupForeignKey(this.getUserSchema()));
        ret.setDisplayColumnFactory(new DisplayColumnFactory()
        {
            public DisplayColumn createRenderer(ColumnInfo colInfo)
            {
                return new GroupDisplayColumn(colInfo);
            }
        });
        this.addColumn(ret);

        return ret;
    }

    public static class GroupDisplayColumn extends DataColumn
    {
        private ColumnInfo _groupId;

        public GroupDisplayColumn(ColumnInfo groupId)
        {
            super(groupId);
            _groupId = groupId;
        }

        public String getName()
        {
            return "group";
        }

        public void renderGridCellContents(RenderContext ctx, Writer out) throws IOException
        {
            String id = (String)getBoundColumn().getValue(ctx);
            if (id != null)
            {
                Group g = org.labkey.api.security.SecurityManager.getGroup(Integer.valueOf(id));
                if (g != null)
                {
                    Container groupContainer = g.isAdministrators() ? ContainerManager.getRoot() : ContainerManager.getForId(g.getContainer());
                    if (g.isAdministrators() || g.isProjectGroup())
                    {
                        String groupName = g.isProjectGroup() ? groupContainer.getPath() + "/" + g.getName() : g.getName();
                        ActionURL url = PageFlowUtil.urlProvider(SecurityUrls.class).getManageGroupURL(groupContainer, groupName);

                        out.write("<a href=\"");
                        out.write(PageFlowUtil.filter(url));
                        out.write("\">");
                        out.write(PageFlowUtil.filter(g.getName()));
                        out.write("</a>");
                        return;
                    }
                }
            }
            out.write("&nbsp;");
        }

        public void addQueryColumns(Set<ColumnInfo> set)
        {
            set.add(_groupId);
        }
    }

    public static class GroupForeignKey extends LookupForeignKey
    {
        private final UserSchema _userSchema;

        public GroupForeignKey(UserSchema userSchema)
        {
            super("UserId", "Name");
            _userSchema = userSchema;
        }

        public TableInfo getLookupTableInfo()
        {
            TableInfo tinfoUsers = CoreSchema.getInstance().getTableInfoPrincipals();
            FilteredTable ret = new FilteredTable<>(tinfoUsers, _userSchema);
            ret.addWrapColumn(tinfoUsers.getColumn("UserId"));
            ret.addColumn(ret.wrapColumn("Name", tinfoUsers.getColumn("Name")));
            ret.setTitleColumn("Name");
            return ret;
        }
    }

    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !_columnsToIgnore.contains(columnInfo.getName().toLowerCase());
    }
}
