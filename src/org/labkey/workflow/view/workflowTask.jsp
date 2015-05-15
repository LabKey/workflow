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
<%@ page import="org.labkey.workflow.view.WorkflowTaskBean" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    WorkflowTaskBean bean = (WorkflowTaskBean) me.getModelBean();
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>

<br>
<br>
<table class="labkey-proj">

    <tr>
        <td>
            <strong><%= bean.getName() %></strong>
        </td>
        <td>
            <%= PageFlowUtil.textLink("Process Instance", new ActionURL(WorkflowController.ProcessInstanceAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId()))%>
        </td>
    </tr>
    <%
        if (bean.getDescription() != null)
        {
    %>
    <tr>
        <td colspan="2"><%= bean.getDescription() %></td>
    </tr>
    <%
        }
    %>
    <tr>
    <td>Created: <%= bean.getCreateTime() %></td>
    <%
        if (bean.getDueDate() != null)
        {
    %>
    <td><strong>   Due: </strong> <%= bean.getDueDate() %></td>
    <%
        }
    %>
    </tr>
</table>

<table class="labkey-proj">
    <%
        if (bean.getOwner() != null)
        {
    %>
    <tr>
        <td>Owned by</td>
        <td><%= bean.getOwner() %></td>
    </tr>
    <%
        }
        if (bean.isDelegated())

        {
    %>
    <tr>
        <td>Delegated to</td>
        <td><%= bean.getAssignee() %></td>
    </tr>
    <%
        }
        else if (bean.getAssignee() != null)
        {
    %>
    <tr>
        <td>Assigned to</td>
        <td>
            <%= bean.getAssignee() %>
        </td>
    </tr>
    <tr>
        <td colspan="2">
            <%
                if (bean.canClaim(getUser(), getViewContext().getContainer()))
            %>
            <%= PageFlowUtil.textLink("Claim", new ActionURL(WorkflowController.ClaimTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("assigneeId", getUser().getUserId()) )%>
            <%
                if (bean.canAssign(getUser(), getViewContext().getContainer()))
            %>
            <%= PageFlowUtil.textLink("Assign", new ActionURL(WorkflowController.AssignTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("assigneeId", getUser().getUserId()) )%>
            <%
                if (bean.canDelegate(getUser(), getViewContext().getContainer()))
            %>
            <%= PageFlowUtil.textLink("Delegate", new ActionURL(WorkflowController.DelegateTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("assigneeId", getUser().getUserId()) )%>
        </td>
    </tr>
    <%
        }
        else
        {

        }
    %>

    <%
        if (bean.getVariables() != null)
        {
    %>
    </table>
    <strong>Task Details</strong>
    <table class="labkey-proj">
     <%
            for (Map.Entry<String, Object> variable : bean.getVariables().entrySet())
            {
                if (!"initiatorId".equalsIgnoreCase(variable.getKey())  &&
                        !"userId".equalsIgnoreCase(variable.getKey()) &&
                        !"container".equalsIgnoreCase(variable.getKey())) {

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
</table>
<br><br>

<%
    if ("handleExportRequest".equals(bean.getTaskDefinitionKey()))
    {
%>
<%= PageFlowUtil.textLink("Approve", new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("approved", "true"))%>
<%= PageFlowUtil.textLink("Deny", new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()).addParameter("approved", "false"))%>

<%
    }
    else
    {
%>
<%= PageFlowUtil.textLink(bean.getName(), new ActionURL(WorkflowController.CompleteTaskAction.class, getViewContext().getContainer()).addParameter("taskId", bean.getId()))%>
<%
    }
%>
<br><br>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", bean.getProcessDefinitionKey()))%>