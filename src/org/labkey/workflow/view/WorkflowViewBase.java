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
package org.labkey.workflow.view;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.labkey.api.data.Container;
import org.labkey.api.jsp.JspBase;
import org.labkey.api.util.DateUtil;
import org.labkey.api.util.HtmlString;
import org.labkey.api.util.HtmlStringBuilder;
import org.labkey.api.util.PageFlowUtil;
import org.labkey.api.util.StringUtilsLabKey;
import org.labkey.api.view.ActionURL;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.workflow.WorkflowController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by susanh on 6/14/15.
 *
 */
public abstract class WorkflowViewBase extends JspBase
{
    protected String rowClass = "labkey-row";

    public String nextRowClass()
    {
        rowClass = rowClass.equals("labkey-row")? "labkey-alternate-row" : "labkey-row";
        return rowClass;
    }

    public String variablesTableRows(Map<String, Object> variables)
    {
        StringBuilder builder = new StringBuilder();
        if (variables != null && !variables.isEmpty())
        {
            Map<String, Object> displayVariables = getDisplayVariables(getContainer(), variables);

            for (Map.Entry<String, Object> variable : displayVariables.entrySet())
            {
                if (variable.getKey().equalsIgnoreCase("Data Access"))
                    continue;
                builder.append("<tr class=\"").append(nextRowClass()).append("\">\n");
                builder.append("<td>").append(h(variable.getKey())).append("</td>\n");
                builder.append("<td>").append(h(variable.getValue())).append("</td>\n");
                builder.append("</tr>\n");
            }
        }
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public String dataAccessTable(Map<String, Object> variables, boolean canAccessData)
    {
        StringBuilder builder = new StringBuilder();
        if (variables != null && variables.containsKey(WorkflowProcess.DATA_ACCESS_KEY))
        {
            builder.append("<strong>Data Parameters</strong><br><br>\n");
            builder.append("<table class=\"labkey-proj\">\n");
            Map<String, Object> dataAccess = (Map<String, Object>) variables.get(WorkflowProcess.DATA_ACCESS_KEY);
            HashMap<String, Object> parameters = (HashMap<String, Object>) dataAccess.get(WorkflowProcess.DATA_ACCESS_PARAMETERS_KEY);

            for (Map.Entry<String, Object> parameter : parameters.entrySet())
            {
                builder.append("<tr>\n");
                builder.append("<td>").append(h(parameter.getKey())).append("</td>\n");
                builder.append("<td>").append(h(getParameterValue(parameter.getKey(), parameter.getValue()))).append("</td>\n");
                builder.append("</tr>\n");
            }
            if (canAccessData)
            {
                builder.append("<tr colspan=\"2\">");
                builder.append("<td><br>").append(PageFlowUtil.button("Download Data").id("downloadDataBtn").onClick(" downloadWorkflowTaskData(" + q((String) dataAccess.get("url")) + ", " + new JSONObject(parameters).toString() + "); return false;")).append("<br><br></td>\n");
                builder.append("</tr>");
            }
            builder.append("</table>\n");
        }
        return builder.toString();
    }

    private Object getParameterValue(String key, Object value)
    {
        return value;
    }


    @Nullable
    public Map<String, Object> getDisplayVariables(Container container, Map<String, Object> variables)
    {
        String displayKey;
        Object displayValue;
        Map<String, Object> _displayVariables = new HashMap<>();
        for (String key : variables.keySet())
        {
            if (WorkflowProcess.CONTAINER_ID.equalsIgnoreCase(key) || WorkflowProcess.INITIATOR_ID.equalsIgnoreCase(key))
                continue;
            else if (key.endsWith("GroupId"))
            {
                displayKey = key.substring(0, key.length() - 2);
                try
                {
                    displayValue = org.labkey.api.security.SecurityManager.getGroup(Integer.valueOf((String) variables.get(key)));
                }
                catch (NumberFormatException e)
                {
                    displayValue = variables.get(key);
                }
            }
            else if (variables.get(key) instanceof Date)
            {
                displayKey = key;
                displayValue = DateUtil.formatDateTime(container, (Date) variables.get(key));
            }
            else
            {
                displayKey = key;
                displayValue = variables.get(key);
            }
            displayKey = StringUtilsLabKey.splitCamelCase(StringUtils.capitalize(displayKey));

            _displayVariables.put(displayKey, displayValue);
        }

        return _displayVariables;

    }

    public HtmlString navigationLinks(@Nullable String processDefinitionName, @NotNull String processDefinitionKey, @Nullable String processInstanceId)
    {
        HtmlStringBuilder builder = HtmlStringBuilder.of();

        builder.append(PageFlowUtil.link("All workflows").href(new ActionURL(WorkflowController.BeginAction.class, getContainer())));
        builder.append(HtmlString.NBSP).append(HtmlString.NBSP);
        if (processDefinitionName != null)
        {
            builder.append(PageFlowUtil.link(processDefinitionName).href(new ActionURL(WorkflowController.SummaryAction.class, getContainer()).addParameter("processDefinitionKey", processDefinitionKey)));
            builder.append(HtmlString.NBSP).append(HtmlString.NBSP);

        }
        builder.append(PageFlowUtil.link("Process instance list").href(new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("processDefinitionKey", processDefinitionKey)));
        builder.append(HtmlString.NBSP).append(HtmlString.NBSP);

        if (processInstanceId != null)
        {
            builder.append(PageFlowUtil.link("This Process Instance").href(new ActionURL(WorkflowController.ProcessInstanceAction.class, getContainer()).addParameter("processInstanceId", processInstanceId)));
            builder.append(HtmlString.NBSP).append(HtmlString.NBSP);
        }
        builder.append(PageFlowUtil.link("My tasks").href(new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", processDefinitionKey).addParameter("assignee", getUser().getUserId())));

        return builder.getHtmlString();
    }
}
