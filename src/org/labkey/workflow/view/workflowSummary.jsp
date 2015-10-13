<%
/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
%>
<%@ page import="org.labkey.api.security.permissions.AdminPermission" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
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
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getContainer()))%>
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
        <%= PageFlowUtil.textLink("Active processes ", new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey())) %>
<%
        if (bean.canStartProcess(getUser(), getContainer()))
        {

%>
        &nbsp;&nbsp;
        <%= PageFlowUtil.textLink("Start new process", new ActionURL(WorkflowController.StartProcessFormAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("workflowModelModule", bean.getWorkflowModelModule())) %>
<%
        }
%>
    </li>

    <li>Tasks</li>
    <ul>
        <li>
            <%= PageFlowUtil.textLink("All tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
        </li>

        <li>
            <%= PageFlowUtil.textLink("Assigned tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("assignee", getUser().getUserId()))%>
        </li>
        <li>
            <%= PageFlowUtil.textLink("Owned tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("owner", getUser().getUserId())) %>
        </li>
        <li>
            <%= PageFlowUtil.textLink("Unassigned tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("assignee", "_blank")) %>
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
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getContainer()))%>
<%
    }
%>