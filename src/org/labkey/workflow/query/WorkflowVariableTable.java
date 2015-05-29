package org.labkey.workflow.query;

import org.labkey.api.data.Container;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;

/**
 * Created by susanh on 5/28/15.
 */
public class WorkflowVariableTable extends SimpleUserSchema.SimpleTable<WorkflowQuerySchema>
{
    public WorkflowVariableTable(WorkflowQuerySchema userSchema, User user, Container container)
    {
        super(userSchema, userSchema.getDbSchema().getTable(WorkflowQuerySchema.TABLE_VARIABLE));
        wrapAllColumns();

        if (!container.hasPermission(user, AdminPermission.class))
        {
            String userId = String.valueOf(user.getUserId());
            SQLFragment sql = new SQLFragment("(act_ru_variable.proc_inst_id_ IN (SELECT V.execution_id_ FROM workflow.act_ru_variable V WHERE V.name_ = 'initiatorId' AND V.text_ = ?))");
            sql.add(userId);
            sql.append(" OR (act_ru_variable.task_id_ IN (SELECT T.id_ FROM workflow.act_ru_task T WHERE T.owner_ = ? OR T.assignee_ = ?))");
            sql.add(userId);
            sql.add(userId);
            addCondition(sql, new FieldKey(null, "id_"));

        }

    }
}

