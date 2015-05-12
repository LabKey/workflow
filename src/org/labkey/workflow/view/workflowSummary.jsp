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
<%@ page import="org.activiti.engine.task.Task" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.view.ProcessSummaryBean" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.activiti.engine.runtime.ProcessInstance" %>
<%@ page import="org.labkey.workflow.WorkflowManager" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    ProcessSummaryBean bean = (ProcessSummaryBean) me.getModelBean();
%>
<%
    if (!bean.getAssignedTasks().isEmpty())
    {
%>
<br>
<strong>My Tasks</strong>
<table class="labkey-proj">
    <tr>
        <td></td>
        <td><strong>Created</strong></td>
        <td><strong>Description</strong></td>
    </tr>
    <%
        for (Task task : bean.getAssignedTasks())
        {
    %>
    <tr>
        <td><%= PageFlowUtil.textLink(task.getName(), new ActionURL(WorkflowController.ViewTaskAction.class, getViewContext().getContainer()).addParameter("processInstanceId", task.getProcessInstanceId()).addParameter("taskId", task.getId())) %></td>
        <td> <%= h(task.getCreateTime()) %></td>
        <td> <%= h(task.getDescription()) %></td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>
<%
    if (!bean.getInstances().isEmpty())
    {
%>
<br>
<strong>My outstanding requests</strong>
<table class="labkey-proj">
    <tr>
        <td><strong>Id</strong></td>
        <td><strong>Request Type</strong></td>
        <td><strong>Data Set</strong></td>
        <td><strong>Queued Tasks</strong></td>
    </tr>
    <%
        for (ProcessInstance instance : bean.getInstances())
        {
            List<String> links = new ArrayList<>();
            for (Task task : WorkflowManager.get().getCurrentProcessTasks(instance.getProcessInstanceId()))
            {
                links.add(PageFlowUtil.textLink(h(task.getName()), new ActionURL(WorkflowController.ViewTaskAction.class, getViewContext().getContainer()).addParameter("taskId", task.getId())));
            }
    %>
    <tr>
        <td> <%= PageFlowUtil.textLink(h(instance.getProcessInstanceId()), new ActionURL(WorkflowController.ViewProcessInstanceAction.class, getViewContext().getContainer()).addParameter("processInstanceId", instance.getProcessInstanceId())) %></td>
        <td> <%= h(instance.getProcessDefinitionName()) %></td>
        <td> <%= h(instance.getProcessVariables().get("dataSetId")) %></td>
        <td> <%= StringUtils.join(links, ", ") %></td>
    </tr>
    <%
        }
    %>
</table>
<%
    }
%>
<br>
<strong>Data sets</strong>
<table class="labkey-proj">
    <tr>
        <td></td>
        <td><strong>Description</strong></td>
    </tr>
    <tr>
       <td><%= PageFlowUtil.textLink("Request export", new ActionURL(WorkflowController.RequestExportAction.class, getViewContext().getContainer()).addParameter("dataSetId", 1)) %></td>
        <td>Data set 1</td>
    </tr>
    <tr>
        <td><%= PageFlowUtil.textLink("Request export", new ActionURL(WorkflowController.RequestExportAction.class, getViewContext().getContainer()).addParameter("dataSetId", 2)) %></td>
        <td>Data set 2</td>
    </tr>
</table>



<br>
<strong>Current Process Diagram</strong> <%= PageFlowUtil.textLink("Deploy New Version", new ActionURL(WorkflowController.DeployAction.class, getViewContext().getContainer()).addParameter("processName", bean.getCurrentProcessKey())) %>
<br>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getViewContext().getContainer()).addParameter("processName", bean.getCurrentProcessKey())%>">
<!--
<form id="deployForm" action="<%= new ActionURL(WorkflowController.DeployAction.class, getViewContext().getContainer())%>">
    <select name="processName">
        <%
            for (String processName : WorkflowManager.get().getProcessNames())
            {
        %>
        <option value="<%= processName%>"><%= processName %></option>
        <%
            }
        %>
    </select>
    <input type="submit" value="Deploy new process">
</form>
-->