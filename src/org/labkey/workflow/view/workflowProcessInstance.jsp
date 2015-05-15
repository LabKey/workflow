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
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.workflow.view.WorkflowProcessBean" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowProcessBean bean = (WorkflowProcessBean) me.getModelBean();
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefintionKey", bean.getProcessDefinitionKey()))%>
<br>
<br>
<%
    if (bean.getName() == null)
    {
%>
<strong>Process <%= bean.getId() %></strong>
<%
    }
    else
    {
%>
<strong><%= bean.getName() %></strong>
<%
    }
%>

<br>
<strong>Process Instance Details</strong>
<table class="labkey-proj">
    <tr>
        <td>Initiator</td>
        <td><%= bean.getInitiator() %></td>
    </tr>


<%
    if (!bean.getProcessVariables().isEmpty())
    {
%>
        <%
            for (Map.Entry<String, Object> variable : bean.getProcessVariables().entrySet())
            {
                if (!"initiatorId".equalsIgnoreCase(variable.getKey())  &&
                    !"userId".equalsIgnoreCase(variable.getKey()) &&
                    !"container".equalsIgnoreCase(variable.getKey()))
                {

    %>
    <tr>
        <td><%= variable.getKey() %></td>
        <td><%= variable.getValue() %></td>
    </tr>
        <%
                }
            }
        }
    %>
    <tr>
        <td>Current Task(s)</td>
        <td></td>
    </tr>
    <%
        for (Task task: bean.getCurrentTasks())
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
<br>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId())%>">
