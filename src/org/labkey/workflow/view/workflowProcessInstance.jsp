<%
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
%>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.api.workflow.WorkflowJob" %>
<%@ page import="org.labkey.api.workflow.WorkflowProcess" %>
<%@ page import="org.labkey.api.workflow.WorkflowTask" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("workflow/view/workflow.css");
        dependencies.add("workflow/view/reassignTask.js");
        dependencies.add("workflow/view/completeTask.js");
        dependencies.add("workflow/view/deleteProcessInstance.js");
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
<%=link("All workflows", WorkflowController.BeginAction.class)%>
<%
    }
    else if (!bean.canView(getUser(), getContainer()))
    {
%>
<%= h(getUser().getDisplayName(getUser())) %> does not have permission to view this process instance.
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
&nbsp;&nbsp;<%= button("Delete").onClick(" createDeleteProcessInstanceConfirmationWindow(" + q(bean.getProcessInstanceId()) + ", " + q(bean.getProcessDefinitionKey()) + ", " + q(bean.getName()) + ")") %>
<%
        }
%>
<br>
<br>
<strong>Process Instance Details</strong>
<br><br>
<table class="labkey-proj">
    <tr class="<%=h(nextRowClass())%>">
        <td>Status</td>
        <td><strong><%= unsafe(bean.isActive() ? "Active" : "Inactive")%></strong></td>
    </tr>
    <%
        if (bean.getInitiator() != null)
        {
    %>

    <tr class="<%=h(nextRowClass())%>">
        <td>Initiator</td>
        <td><%= h(bean.getInitiator().getDisplayName(getUser())) %></td>
    </tr>
    <%
        }
    %>

<%= unsafe(variablesTableRows(bean.getVariables())) %>
<tr class="<%=h(nextRowClass())%>">
    <td class="labkey-workflow-detail-label">Current Job(s)</td>

    <%
        if (bean.getCurrentJobs().isEmpty())
        {
            out.println(unsafe("<td>None</td></tr>"));
        }
        else
        {
            out.println(unsafe("<td>"));
            for (WorkflowJob job: bean.getCurrentJobs())
            {
    %>
            <%= h(job.getId()) %>: Due date <%= formatDateTime(job.getDueDate()) %>
            <br>
    <%
            }
            out.println(unsafe("</td></tr>"));
        }
    %>
<tr class="<%=h(nextRowClass())%>">
    <td class="labkey-workflow-detail-label">Current Task(s)</td>
<%
    if (bean.getCurrentTasks().isEmpty())
    {
        out.println(unsafe("<td>None</td></tr>"));
    }
    else
    {

        out.println(unsafe("<td>"));
        for (WorkflowTask task: bean.getCurrentTasks())
        {
%>
    <%
            if (task.canView(getUser(), getContainer()))
            {
    %>
        <%=link(task.getName(), urlFor(WorkflowController.TaskAction.class).addParameter("taskId", task.getId())) %>
    <%
            }
            else
            {
    %>
        <%= h(task.getName()) %>
    <%
            }
            if (task.getAssignee() != null)
            {
    %>
    (assigned to <%= h(task.getAssignee().getDisplayName(getUser())) %>)
    <%
            }
            else
            {
    %>
        (currently unassigned)
        <%
            }
        %>
        <br>
<%
        }
        out.println(unsafe("</td></tr>"));
    }
%>
<tr class="<%=h(nextRowClass())%>">
    <td class="labkey-workflow-detail-label">Completed Task(s)</td>
<%
    if (bean.getCompletedTasks().isEmpty())
    {
        out.println(unsafe("<td>None</td></tr>"));
    }
    else
    {
        out.println(unsafe("<td>"));
        for (WorkflowTask completedTask: bean.getCompletedTasks())
        {
%>
            <%
                if (completedTask.canView(getUser(), getContainer()))
                {
            %>
            <%=link(completedTask.getName(), urlFor(WorkflowController.TaskAction.class).addParameter("taskId", completedTask.getId())) %>
            <%
            }
            else
            {
            %>
            <%= h(completedTask.getName()) %>
            <%
                }
                if (completedTask.getAssignee() != null)
                {
            %>
            (completed by <%= h(completedTask.getAssignee().getDisplayName(getUser())) %>)
            <%
            }
            else
            {
            %>
            (unassigned)
            <%
            }
            %>
        <br>
    <%
        }
        out.println(unsafe("</td></tr>"));
    %>

<%
    }
%>
</table>
<%= unsafe(dataAccessTable(bean.getVariables(), bean.canAccessData(getUser(), getContainer()))) %>
<%
        if (bean.hasDiagram(getContainer()))
        {
%>
<br>
<strong>Process Diagram</strong>
<br><br>
<img src="<%=h(urlFor(WorkflowController.ProcessDiagramAction.class).addParameter("processInstanceId", bean.getProcessInstanceId()))%>">
<%

        }
    }
%>
