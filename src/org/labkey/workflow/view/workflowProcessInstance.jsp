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
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.workflow.WorkflowProcess" %>
<%@ page import="org.labkey.api.workflow.WorkflowTask" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("workflow/view/reassignTask.js"));
        resources.add(ClientDependency.fromPath("workflow/view/completeTask.js"));
        resources.add(ClientDependency.fromPath("workflow/view/deleteProcessInstance.js"));

        return resources;
    }
%>

<%
    HttpView me = HttpView.currentView();
    WorkflowProcess bean = (WorkflowProcess) me.getModelBean();
%>
<labkey:errors></labkey:errors>
<%
    if (bean.getProcessInstanceId() == null)
    {
%>
There is no active process with id <%= h(bean.getId()) %>
<br><br>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink(h(bean.getProcessDefinitionName()), new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
<%
    }
    else if (!bean.canView(getUser(), getContainer()))
    {
%>
<%= h(getUser()) %> does not have permission to view this process instance.
<%
    }
    else
    {
%>
<%= navigationLinks(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(), null) %>

<br>
<br>
<%
    if (bean.getName() == null)
    {
%>
<strong>Process <%= h(bean.getId()) %></strong>
<%
    }
    else
    {
%>
<strong><%= h(bean.getName()) %></strong>
<%
    }
%>
<%
    if (bean.canDelete(getUser(), getContainer()))
    {
%>
&nbsp;&nbsp;<%= PageFlowUtil.button("Delete").onClick(" createDeleteProcessInstanceConfirmationWindow(" + q(bean.getProcessInstanceId()) + ", " + q(bean.getProcessDefinitionKey()) + ", " + q(bean.getName()) + ")") %>
<%
    }
%>
<br>
<br>
<strong>Process Instance Details</strong>
<br><br>
<table class="labkey-proj">
    <tr>
        <td>Initiator</td>
        <td><%= h(bean.getInitiator()) %></td>
    </tr>

<%= variablesTableRows(bean.getVariables()) %>

<tr>
    <td>Current Task(s)</td>
    <td></td>
</tr>
<%
        for (WorkflowTask task: bean.getCurrentTasks())
        {
%>
<tr>
    <td></td>
    <td>
    <%
            if (task.canView(getUser(), getContainer()))
            {
    %>
        <%= PageFlowUtil.textLink(h(task.getName()), new ActionURL(WorkflowController.TaskAction.class, getContainer()).addParameter("taskId", task.getId())) %>
    <%
            }
            else
            {
    %>
        <%= task.getName() %>
    <%
            }
            if (task.getAssignee() != null)
            {
    %>
    (assigned to <%= h(task.getAssignee()) %>)
    <%
            }
            else
            {
    %>
        (currently unassigned)
        <%
            }
        %>
    </td>
</tr>
</table>
<%
        }
%>
<%= dataAccessTable(bean.getVariables(), bean.canAccessData(getUser(), getContainer())) %>
<%
        if (bean.hasDiagram(getContainer()))
        {
%>
<br>
<strong>Process Diagram</strong>
<br><br>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId())%>">
<%

        }
    }
%>
