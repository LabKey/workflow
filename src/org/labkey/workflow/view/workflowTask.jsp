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
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.model.TaskFormField" %>
<%@ page import="org.labkey.workflow.model.WorkflowProcess" %>
<%@ page import="org.labkey.workflow.model.WorkflowTask" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.labkey.api.security.permissions.AdminPermission" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
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
<labkey:errors></labkey:errors>
<%
    HttpView me = HttpView.currentView();
    WorkflowTask bean = (WorkflowTask) me.getModelBean();
    if (!bean.isActive())
    {
%>
There is no active task with id <%= bean.getId() %>
<%
    }
    else if (!bean.canView(getUser(), getContainer()))
    {
%>
<%= getUser() %> does not have permission to view this task.
<%
    }
    else
    {
        String assigneeLabel = bean.isDelegated() ? "Delegated to" : "Assigned to";
        String changeAssigneeLabel = bean.getAssigneeId() == null ? "Assign" : "Reassign";
        Boolean canChangeAssignee = bean.canClaim(getUser(), getContainer()) || (bean.isDelegated() && bean.canDelegate(getUser(), getContainer())) || bean.canAssign(getUser(), getContainer());
%>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink(bean.getProcessDefinitionName(getContainer()), new ActionURL(WorkflowController.SummaryAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey(getContainer())))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("Process instance list", new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey(getContainer())))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("This Process Instance", new ActionURL(WorkflowController.ProcessInstanceAction.class, getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId()))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("My tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey(getContainer())).addParameter("query.assignee_~eq", getUser().getUserId()))%>
<br>
<br>
<table class="labkey-proj">

    <tr>
        <td>
            <strong><%= bean.getName() %></strong>
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


<strong>Task Details</strong>
<br><br>
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
    %>
    <%
        if (bean.getAssignee() != null)
        {
    %>
    <tr>
        <td><%= assigneeLabel %></td>
        <td><%= bean.getAssignee() %></td>
    </tr>
    <tr></tr>
    <%
        }
        if (canChangeAssignee)
        {
    %>
    <tr>
        <td colspan="2" height="40px">
            <%= button(changeAssigneeLabel).onClick("createReassignTaskWindow(" + q(bean.getId()) + ", " + bean.getAssigneeId() + "); return false;") %>
        </td>
    </tr>
    <%
        }
    %>

    <%
        if (bean.getVariables() != null && !bean.getVariables().isEmpty())
        {

            Map<String, Object> displayVariables = WorkflowProcess.getDisplayVariables(getContainer(), bean.getVariables());
    %>

     <%

             for (Map.Entry<String, Object> variable : displayVariables.entrySet())
             {
                 if (variable.getKey().equalsIgnoreCase("Data Access"))
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
            if (displayVariables.containsKey("Data Access"))
            {
    %>
    <strong>Data Parameters</strong><br><br>
    <table class="labkey-proj">
    <%
                HashMap<String, Object> dataAccess = (HashMap<String, Object>) displayVariables.get("Data Access");
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
                if (bean.canAccessData(getUser(), getContainer()))
                {
    %>
        <tr colspan="2">
            <td><br><%= PageFlowUtil.button("Download Data").onClick(" downloadDataGrid(" + q((String) dataAccess.get("url")) + ", " + new JSONObject(parameters).toString() + "); return false;") %><br><br></td>
        </tr>
    <%
                }
    %>

    </table>
    <%
            }
        }
    %>

<br>
<%
        if (bean.canComplete(getUser(), getContainer()))
        {
            Map<String, TaskFormField> fields = bean.getFormFields();
            if (fields.isEmpty())
            {
                if (bean.getTaskDefinitionKey().equals("downloadDataSet"))
                {
%>
<%= PageFlowUtil.button("Complete Task").onClick("completeWorkflowTask(" + q(bean.getId()) + "," + q(bean.getTaskDefinitionKey()) + ", [], " + q(bean.getProcessInstanceId()) + "," + qh(bean.getProcessDefinitionKey(getContainer())) + ")")%>
<%
                }
                else
                {
%>
<%= PageFlowUtil.button(h(bean.getName())).onClick("completeWorkflowTask(" + q(bean.getId()) + "," + q(bean.getTaskDefinitionKey()) + ", [], " + q(bean.getProcessInstanceId()) + "," + qh(bean.getProcessDefinitionKey(getContainer())) + ")")%>
<%
                }
            }
            else
            {
%>
<strong><%= h(bean.getName()) %></strong>
<br>
<br>
<form name="<%= bean.getTaskDefinitionKey() %>" action="javascript:completeWorkflowTask('<%= bean.getId() %>', '<%= bean.getTaskDefinitionKey() %>', ['<%=StringUtils.join(fields.keySet(), "', '") %>'], q(bean.getProcessInstanceId()) + "," + qh(bean.getProcessDefinitionKey(getContainer()) + ")" %>
<%
                    for (Map.Entry<String, TaskFormField> field : fields.entrySet())
                    {
                        // TODO add a type that is text area that has "information" for the rows and columns
                        // TODO handle other input field types as well: Date, long, boolean
                        // TODO investigate what the rendered form object is
                        if (field.getValue().getType().getName().equals("string"))
                        {
            %>
        <%= field.getValue().getName() %>
        <br>
        <textarea title="<%= field.getValue().getName() %>" name="<%= field.getValue().getId()%>" rows="10" cols="100"></textarea>
        <%
                        }
                        else if (field.getValue().getType().getName().equals("enum"))
                        {
                            Map<String, String> choices = (Map<String, String>) field.getValue().getType().getInformation("values");
                            if (choices != null && !choices.isEmpty())
                            {
        %>
        <%= field.getValue().getName() %>
        <select title="<%= field.getValue().getName() %>" name="<%= field.getValue().getId() %>">
                <%
                                for (Map.Entry<String, String> choice : ((Map<String, String>) field.getValue().getType().getInformation("values")).entrySet())
                                {
                %>
                <option value="<%= choice.getKey()%>"><%= choice.getValue()%></option>
                    <%
                                }
                %>
        </select>
        <br>
        <%
                            }
                        }
                    }
%>
    <br><br>
    <%= PageFlowUtil.button("Submit").submit(true) %>
</form>
<br>
<%
            }
        }
    }
%>
    <br>
    <br>
<%
    if (getContainer().hasPermission(getUser(), AdminPermission.class))
    {
%>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
<%
    }
%>