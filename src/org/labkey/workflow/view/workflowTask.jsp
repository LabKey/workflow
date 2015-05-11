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
<%@ page import="org.labkey.workflow.WorkflowTask" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowTask bean = (WorkflowTask) me.getModelBean();
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>

<br>
<br>
<strong>Task Details</strong>
<table class="labkey-proj">
    <tr>
        <td>Id</td>
        <td><%= bean.getId() %></td>
    </tr>
    <tr>
        <td>Description</td>
        <td><%= bean.getDescription() %></td>
    </tr>
    <tr>
        <td>Reason for request</td>
        <td><%= bean.getTaskParameters().get("reason")%></td>
    </tr>
</table>

<%
    if ("reviewExportRequest".equals(bean.getTaskDefinitionId()) || "handleExportRequest".equals(bean.getTaskDefinitionId()))
    {
%>
<%= PageFlowUtil.textLink("Approve", new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("approved", "true"))%>
<%= PageFlowUtil.textLink("Deny", new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("approved", "false"))%>

<%
    }
    else if ("downloadDataSet".equals(bean.getTaskDefinitionId()))
    {
%>
<%= PageFlowUtil.textLink("Download data set", new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()))%>
<%
    }
%>