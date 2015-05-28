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
<%@ page import="org.labkey.api.security.UserPrincipal" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.model.WorkflowSummary" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.labkey.api.security.permissions.AdminPermission" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowSummary bean = (WorkflowSummary) me.getModelBean();
%>
<%
    if (getContainer().hasPermission(getUser(), AdminPermission.class))
    {
%>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getContainer()))%>
<br>
<br>
<%
    }
%>

<%--<strong><%= bean.getName() %></strong>--%>
<br>
<%= bean.getDescription() %>
<ul>

    <li>
        <%
            if (bean.getNumTotalTasks() == 0)
            {
        %>
        No current tasks
        <%
        }
        else
        {
        %>
        <%= PageFlowUtil.textLink("All tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
        <%
            }
        %>
    </li>

    <li>
        <%
            if (bean.getNumAssignedTasks() == 0)
            {
        %>
        No currently assigned tasks
        <%
            }
            else
            {
        %>
        <%= PageFlowUtil.textLink("Assigned tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("query.assignee_~eq", getUser().getUserId()))%>
        <%
            }
        %>
    </li>
    <li>
        <%
            if (bean.getNumOwnedTasks() == 0)
            {
        %>
        No currently owned tasks
        <%
            }
            else
            {
        %>
        <%= PageFlowUtil.textLink("Owned tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("query.owner_~eq", getUser().getUserId())) %>
        <%
            }
        %>
    </li>
    <li>
        <%
            if (bean.getNumGroupTasks().isEmpty())
            {
        %>
        No unassigned tasks associated with your groups
        <%
            }
            else
            {
        %>
        Unassigned tasks
        <ul>
        <%
                for (Map.Entry<UserPrincipal, Long> entry : bean.getNumGroupTasks().entrySet())
                {
        %>
        <li> <%= PageFlowUtil.textLink(entry.getKey().getName(), new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("query.assignee_/DisplayName~isblank", true).addParameter("query.group~eq", entry.getKey().getUserId())) %>
        <%
                }
        %>
            </ul>
        <%
            }
        %>

    </li>
    <li>
        <%
            if (bean.getNumInstances() == 0)
            {
        %>
        No currently active processes associated with this user
        <%
            }
            else
            {
        %>
        <%= PageFlowUtil.textLink("Active processes ", new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("query.proc_def_id_~contains", bean.getProcessDefinitionKey() + ":").addParameter("processDefinitionKey", bean.getProcessDefinitionKey())) %>
       <%
            }
       // TODO add the start form
       %>
        <%--&nbsp;&nbsp;<%= PageFlowUtil.button("Start new process").href(new ActionURL(WorkflowController.StartProcessAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("approverGroupId", "-1"))%>--%>

    </li>
</ul>

<br>
<strong>Current Process Diagram</strong>
<br>
<%= bean.getModelFile() %>
<br>
<%
    if (bean.hasDiagram())
    {
%>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey())%>">
<%
    }
    else
    {
%>
No process diagram available.
<%

    }
%>
<br>
<br>
<%
    if (getContainer().hasPermission(getUser(), AdminPermission.class))
    {
%>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getContainer()))%>
<%
    }
%>