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
<%@ page import="org.labkey.api.security.permissions.AdminPermission" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.model.WorkflowSummary" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowSummary bean = (WorkflowSummary) me.getModelBean();
%>
<labkey:errors></labkey:errors>
<%
    if (getContainer().hasPermission(getUser(), AdminPermission.class))
    {
%>
<%=link("All workflows", WorkflowController.BeginAction.class)%>
<br>
<br>
<%
    }
%>
<%
    if (bean.getDescription() != null)
    {
%>
<br>
<%= h(bean.getDescription()) %>
<%
    }
%>
<ul>
    <li>
        <%=link("Active processes", urlFor(WorkflowController.InstanceListAction.class).addParameter("processDefinitionKey", bean.getProcessDefinitionKey())) %>
    </li>

    <li>Tasks</li>
    <ul>
        <li>
            <%=link("All tasks", urlFor(WorkflowController.TaskListAction.class).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
        </li>

        <li>
            <%=link("Assigned tasks", urlFor(WorkflowController.TaskListAction.class).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("assignee", getUser().getUserId()))%>
        </li>
        <li>
            <%=link("Owned tasks", urlFor(WorkflowController.TaskListAction.class).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("owner", getUser().getUserId())) %>
        </li>
        <li>
            <%=link("Unassigned tasks", urlFor(WorkflowController.TaskListAction.class).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("assignee", "_blank")) %>
        </li>
    </ul>

</ul>

<br>
<strong>Current Process Diagram</strong>
<br>
<%= h(bean.getModelFile()) %>
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
<%=link("All workflows", WorkflowController.BeginAction.class)%>
<%
    }
%>