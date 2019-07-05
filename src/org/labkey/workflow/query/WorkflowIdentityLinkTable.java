/*
 * Copyright (c) 2015-2019 LabKey Corporation
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

import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.query.FieldKey;
import org.labkey.api.query.SimpleUserSchema;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;

/**
 * Created by susanh on 5/28/15.
 */
public class WorkflowIdentityLinkTable extends SimpleUserSchema.SimpleTable<WorkflowQuerySchema>
{
    public WorkflowIdentityLinkTable(WorkflowQuerySchema userSchema, ContainerFilter containerFilter, User user, Container container)
    {
        super(userSchema, userSchema.getDbSchema().getTable(WorkflowQuerySchema.TABLE_IDENTITY_LINK), containerFilter);
        wrapAllColumns();

        if (!container.hasPermission(user, AdminPermission.class))
        {
            String userId = String.valueOf(user.getUserId());
            SQLFragment sql = new SQLFragment("(act_ru_identityLink.proc_inst_id_ IN (SELECT V.execution_id_ FROM workflow.act_ru_variable V WHERE V.name_ = 'initiatorId' AND V.text_ = ?))");
            sql.add(userId);
            sql.append(" OR (act_ru_identityLink.task_id_ IN (SELECT T.id_ FROM workflow.act_ru_task T WHERE T.owner_ = ? OR T.assignee_ = ?))");
            sql.add(userId);
            sql.add(userId);
            addCondition(sql, new FieldKey(null, "id_"));

        }
    }
}
