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
<%@ page import="org.labkey.api.util.DateUtil" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.workflow.TaskFormField" %>
<%@ page import="org.labkey.api.workflow.WorkflowTask" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
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
<%= h(getUser().getDisplayName(getUser())) %> does not have permission to view this task.
<%
    }
    else
    {
        String assigneeLabel = bean.isDelegated() ? "Delegated to" : "Assigned to";
        String changeAssigneeLabel = bean.getAssigneeId() == null ? "Assign" : "Reassign";
        Boolean canChangeAssignee = bean.canClaim(getUser(), getContainer()) || (bean.isDelegated() && bean.canDelegate(getUser(), getContainer())) || bean.canAssign(getUser(), getContainer());
%>
<%= navigationLinks(bean.getProcessDefinitionName(getContainer()), bean.getProcessDefinitionKey(getContainer()), bean.getProcessInstanceId()) %>
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
    <td>Created: <%= DateUtil.formatDateTime(getContainer(), bean.getCreateTime()) %></td>
    <%
        if (bean.getDueDate() != null)
        {
    %>
    <td><strong>   Due: </strong> <%= DateUtil.formatDateTime(getContainer(), bean.getDueDate()) %></td>
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
        <td><%= h(bean.getOwner().getDisplayName(getUser())) %></td>
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
        <td><%= h(bean.getAssignee().getDisplayName(getUser())) %></td>
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

<%= variablesTableRows(bean.getVariables()) %>
</table>
<%= dataAccessTable(bean.getVariables(), bean.canAccessData(getUser(), getContainer()))%>

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
<%= PageFlowUtil.button("Complete Task").onClick("completeWorkflowTask(" + q(bean.getId()) + "," + qh(bean.getTaskDefinitionKey()) + ", [], " + q(bean.getProcessInstanceId()) + "," + qh(bean.getProcessDefinitionKey(getContainer())) + ")")%>
<%
                }
                else
                {
%>
<%= PageFlowUtil.button(h(bean.getName())).onClick("completeWorkflowTask(" + q(bean.getId()) + "," + qh(bean.getTaskDefinitionKey()) + ", [], " + q(bean.getProcessInstanceId()) + "," + qh(bean.getProcessDefinitionKey(getContainer())) + ")")%>
<%
                }
            }
            else
            {
%>
<%= actionForm(bean.getName(), bean.getTaskDefinitionKey(),
        "javascript:completeWorkflowTask(" + q(bean.getId()) + "," + qh(bean.getTaskDefinitionKey()) + ", ['" + StringUtils.join(fields.keySet(), "', '") + "']," + q(bean.getProcessInstanceId()) + ", " + qh(bean.getProcessDefinitionKey(getContainer())) + ")", fields, null) %>
<br>
<%
            }
        }
    }
%>
