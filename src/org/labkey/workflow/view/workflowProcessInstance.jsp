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
<%@ page import="org.labkey.workflow.WorkflowController.ExportRequestDetailsBean" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.api.gwt.client.util.StringUtils" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    HttpView me = HttpView.currentView();
    ExportRequestDetailsBean bean = (ExportRequestDetailsBean) me.getModelBean();
%>
<%= PageFlowUtil.textLink("Return to workflow summary", new ActionURL(WorkflowController.BeginAction.class, getViewContext().getContainer()))%>
<br>
<br>
<strong>Request Details</strong>
<table class="labkey-proj">
    <tr>
        <td>Id</td>
        <td><%= bean.getProcessInstanceId() %></td>
    </tr>
    <tr>
        <td>Requester</td>
        <td><%= bean.getUser() %></td>
    </tr>
    <tr>
        <td>Data set</td>
        <td><%= bean.getDataSetId() %></td>
    </tr>
    <tr>
        <td>Reason</td>
        <td><%= bean.getReason() %></td>
    </tr>
    <tr>
        <td>Current Task(s)</td>
        <td><%= StringUtils.join(bean.getCurrentTasks(), ",") %></td>
    </tr>
</table>

<br>
<strong>Request Diagram</strong>
<br>
<img src="<%= new ActionURL(WorkflowController.ProcessDiagramAction.class, getViewContext().getContainer()).addParameter("processInstanceId", bean.getProcessInstanceId())%>">
