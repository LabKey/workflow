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

import org.labkey.api.data.BaseColumnInfo;
import org.labkey.api.data.ColumnInfo;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerFilter;
import org.labkey.api.data.DatabaseTableType;
import org.labkey.api.data.JdbcType;
import org.labkey.api.data.SQLFragment;
import org.labkey.api.data.TableInfo;
import org.labkey.api.query.DefaultQueryUpdateService;
import org.labkey.api.query.DetailsURL;
import org.labkey.api.query.ExprColumn;
import org.labkey.api.query.InvalidKeyException;
import org.labkey.api.query.QueryUpdateService;
import org.labkey.api.query.QueryUpdateServiceException;
import org.labkey.api.query.UserIdForeignKey;
import org.labkey.api.security.User;
import org.labkey.api.view.ActionURL;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.workflow.WorkflowController;
import org.labkey.workflow.WorkflowManager;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by susanh on 5/11/15.
 */
public class WorkflowProcessInstanceTable extends WorkflowTenantTable
{

    private static Set<String> _columnsToIgnore = new HashSet<>();
    static
    {
        _columnsToIgnore.add("business_key_");
        _columnsToIgnore.add("super_exec_");
        _columnsToIgnore.add("act_id_");
        _columnsToIgnore.add("is_concurrent_");
        _columnsToIgnore.add("is_scope_");
        _columnsToIgnore.add("is_event_scope_");
        _columnsToIgnore.add("suspension_state_");
        _columnsToIgnore.add("cached_ent_state_");
        _columnsToIgnore.add("lock_time_");
    }

    public WorkflowProcessInstanceTable(WorkflowQuerySchema userSchema, ContainerFilter containerFilter, User user, Container container)
    {
        super(userSchema, WorkflowQuerySchema.TABLE_PROCESS_INSTANCE, containerFilter);

        BaseColumnInfo idColumn = getMutableColumn("id_");
        ActionURL base = new ActionURL(WorkflowController.ProcessInstanceAction.class, getContainer());
        DetailsURL detailsURL = new DetailsURL(base, Collections.singletonMap("processInstanceId", "proc_inst_id_"));

        idColumn.setURL(detailsURL);
        setDetailsURL(detailsURL);

        addInitiatorColumn();

        addWorkflowListFilter(WorkflowRegistry.getProcessListFilters(user, container));
    }

   private ColumnInfo addInitiatorColumn()
    {
        SQLFragment sql = new SQLFragment("(SELECT V.text_ FROM workflow.act_ru_variable V WHERE V.name_ = 'initiatorId' AND V.execution_id_ = act_ru_execution.id_)");

        ExprColumn ret = new ExprColumn(this, "Initiator", sql, JdbcType.VARCHAR);
        ret.setFk(new UserIdForeignKey(this.getUserSchema()));

        this.addColumn(ret);

        return ret;
    }


    @Override
    public boolean acceptColumn(ColumnInfo columnInfo)
    {
        return !_columnsToIgnore.contains(columnInfo.getName().toLowerCase());
    }

    @Override
    public QueryUpdateService getUpdateService()
    {
        TableInfo table = getRealTable();
        if (table != null && table.getTableType() == DatabaseTableType.TABLE)
        {
            return new DefaultQueryUpdateService(this, table)
            {
                @Override
                protected Map<String, Object> deleteRow(User user, Container container, Map<String, Object> oldRowMap) throws QueryUpdateServiceException, SQLException, InvalidKeyException
                {
                    // FIXME this doesn't quite work
                    WorkflowManager.get().deleteProcessInstance((String) oldRowMap.get("PROC_INST_ID_"), null);
                    return oldRowMap;

                }
            };
        }
        return null;
    }


}
