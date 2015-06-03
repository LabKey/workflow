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
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("workflow/view/reassignTask.js"));

        return resources;
    }
%>
<%
    HttpView me = HttpView.currentView();
    WorkflowController.WorkflowRequestForm bean = (WorkflowController.WorkflowRequestForm) me.getModelBean();
%>

<labkey:errors></labkey:errors>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
<%
    if (bean.getProcessDefinitionKey() != null)
    {
%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink(h(bean.getProcessDefinitionName()), new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
<%
    }
%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("Process instance list", new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("My tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("query.assignee_~eq", getUser().getUserId()))%>
<br>
<br>

<%
    if (me.getView("workflowListQueryView") != null)
    {
%>
<div>
    <% me.include(me.getView("workflowListQueryView"),out); %>
</div>
<br><br>
<%
    }
%>
<%= PageFlowUtil.textLink("All workflows", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
<%
    if (bean.getProcessDefinitionKey() != null)
    {
%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink(h(bean.getProcessDefinitionName()), new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
<%
    }
%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("Process instance list", new ActionURL(WorkflowController.InstanceListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
&nbsp;&nbsp;
<%= PageFlowUtil.textLink("My tasks", new ActionURL(WorkflowController.TaskListAction.class, getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()).addParameter("query.assignee_~eq", getUser().getUserId()))%>

