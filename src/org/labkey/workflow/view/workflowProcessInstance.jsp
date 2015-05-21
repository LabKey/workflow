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
<%@ page import="org.json.JSONObject" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.model.WorkflowProcess" %>
<%@ page import="org.labkey.workflow.model.WorkflowTask" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowProcess bean = (WorkflowProcess) me.getModelBean();
%>
<%
    if (bean.getProcessInstanceId() == null)
    {
%>

There is no active process with id <%= h(bean.getId()) %>
<%
}
else
{
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>
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

<br>
<br>
<table class="labkey-proj">
    <tr>
        <td>Initiator</td>
        <td><%= h(bean.getInitiator()) %></td>
    </tr>


<%
    if (!bean.getProcessVariables().isEmpty())
    {
        Map<String, Object> displayVariables = WorkflowProcess.getDisplayVariables(bean.getProcessVariables());

        for (Map.Entry<String, Object> variable : displayVariables.entrySet())
        {
            if (variable.getKey().equalsIgnoreCase("Get Data"))
                continue;
%>
    <tr>
        <td><%= h(variable.getKey()) %></td>
        <td><%= h(variable.getValue()) %></td>
    </tr>
<%
        }
        if (displayVariables.containsKey("Get Data"))
        {
            HashMap dataAccess = (HashMap) displayVariables.get("Get Data");
%>
    <tr>
        <td><%= PageFlowUtil.button("Get Data").onClick(" getData(" + PageFlowUtil.qh((String) dataAccess.get("url")) + ", '" + (new JSONObject(dataAccess.get("parameters")).toString()) + "'); return false;") %></td>
    </tr>
    <%

        }
    }
    %>
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
        <td><%= PageFlowUtil.textLink(task.getName(), new ActionURL(WorkflowController.TaskAction.class, getViewContext().getContainer()).addParameter("taskId", task.getId())) %></td>
            <%
        }
    %>
</table>

<br>
<strong>Process Diagram</strong>
<br><br>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId())%>">
<%
    }
%>
<br>
<br>
<%= PageFlowUtil.textLink("Return to process list", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>