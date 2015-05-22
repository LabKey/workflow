<%
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
%>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.model.WorkflowTask" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.util.StringUtilsLabKey" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="org.labkey.workflow.model.WorkflowProcess" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="org.json.JSONObject" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("workflow/view/reassignTask.js"));
        resources.add(ClientDependency.fromPath("workflow/view/completeTask.js"));

        return resources;
    }
%>
<%
    HttpView me = HttpView.currentView();
    WorkflowTask bean = (WorkflowTask) me.getModelBean();
    if (!bean.isActive())
    {
%>
There is no active task with id <%= bean.getId() %>
<%
    }
    else
    {
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>

<br>
<br>
<table class="labkey-proj">

    <tr>
        <td>
            <strong><%= bean.getName() %></strong>
        </td>
        <td>
            <%= PageFlowUtil.textLink("Process Instance", new ActionURL(WorkflowController.ProcessInstanceAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId()))%>
        </td>
    </tr>
    <%
        if (bean.getDescription() != null)
        {
    %>
    <tr>
        <td colspan="2"><%= bean.getDescription() %></td>
    </tr>
    <%
        }
    %>
    <tr>
    <td>Created: <%= bean.getCreateTime() %></td>
    <%
        if (bean.getDueDate() != null)
        {
    %>
    <td><strong>   Due: </strong> <%= bean.getDueDate() %></td>
    <%
        }
    %>
    </tr>
</table>

<table class="labkey-proj">
    <%
        if (bean.getOwner() != null)
        {
    %>
    <tr>
        <td>Owned by</td>
        <td><%= bean.getOwner() %></td>
    </tr>
    <%
        }
        if (bean.isDelegated())
        {
    %>
    <tr>
        <td>Delegated to</td>
        <td><%= bean.getAssignee() %></td>
    </tr>
    <%
            if (bean.canDelegate(getUser(), getContainer()))
            {
                %>
    <tr>
        <td colspan="2">
            <%= button("Reassign").onClick("createReassignTaskWindow(" + q(bean.getId()) + "); return false;") %>
        </td>
    </tr>
    <%
            }
        }
        else if (bean.getAssignee() != null)
        {
    %>
    <tr>
        <td>Assigned to</td>
        <td>
            <%= bean.getAssignee() %>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <%= button("Reassign").onClick("createReassignTaskWindow(" + q(bean.getId()) + "); return false;") %>
        </td>
    </tr>
    <%
        }
        else
        {
    %>
    <tr>
        <td colspan="2">
        <%= button("Assign").onClick("createReassignTaskWindow(" + q(bean.getId()) + "); return false;") %>
        </td>
    </tr>
    </table>
    <%
        }
    %>
    <%
        if (bean.getVariables() != null && !bean.getVariables().isEmpty())
        {

            Map<String, Object> displayVariables = WorkflowProcess.getDisplayVariables(getContainer(), bean.getVariables());
    %>

    <strong>Task Details</strong>
    <br><br>
    <table class="labkey-proj">
     <%

             for (Map.Entry<String, Object> variable : displayVariables.entrySet())
             {
                 if (variable.getKey().equalsIgnoreCase("Get Data"))
                     continue;
    %>
        <tr>
            <td><%= h(variable.getKey()) %></td>
            <td><%= h(variable.getValue()) %></td>
        </tr>
    <%
            }
    %>
    </table>

    <%
            if (displayVariables.containsKey("Get Data"))
            {
    %>
    <strong>Data Parameters</strong><br><br>
    <table class="labkey-proj">
    <%
                HashMap<String, Object> dataAccess = (HashMap<String, Object>) displayVariables.get("Get Data");
                HashMap<String, Object> parameters = (HashMap<String, Object>) dataAccess.get("parameters");

                for (Map.Entry<String, Object> parameter : parameters.entrySet())
                {
    %>
        <tr>
            <td><%= h(parameter.getKey()) %></td>
            <td><%= h(parameter.getValue()) %></td>
        </tr>
    <%
                }
    %>
        <tr colspan="2">
            <td><br><%= PageFlowUtil.button("Download Data").onClick(" getData(" + q((String) dataAccess.get("url")) + ", " + new JSONObject(parameters).toString() + "); return false;") %></td>
        </tr>

    </table>
    <%

            }
        }
    %>

<br>
<%
    if ("handleExportRequest".equals(bean.getTaskDefinitionKey()))
    {
%>
<%= PageFlowUtil.button("Approve").onClick(" createCompleteTaskWindow(" + q(bean.getId()) + ", " + q(bean.getName()) + ", " + "{\"approved\": \"true\"}); return false;") %>
<%= PageFlowUtil.button("Deny").onClick(" createCompleteTaskWindow(" + q(bean.getId()) + ", " + q(bean.getName()) + ", " + "{\"approved\": \"false\"}); return false;") %>

<%
    }
    else
    {
%>
<%= PageFlowUtil.textLink(h(bean.getName()), new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()))%>
<%
    }
%>
<br><br>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
<%
    }
%>
<br>
<br>
<%= PageFlowUtil.textLink("Return to process list", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>