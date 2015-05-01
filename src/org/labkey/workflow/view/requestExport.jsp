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
<%@ page import="org.labkey.api.security.User" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.JspView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%
    User user = getUser();
    JspView<WorkflowController.ExportRequestForm> me = (JspView<WorkflowController.ExportRequestForm>)HttpView.currentView();
    WorkflowController.ExportRequestForm bean = me.getModelBean();
%>
Make a data request (process id: <%= bean.getProcessId() %>
<form id="requestForm" action="<%= new ActionURL(WorkflowController.SubmitRequestAction.class, getContainer())%>">
    <input type="hidden" name="processInstanceId" value="<%= bean.getProcessInstanceId() %>">
    Data set id: <input type="text" name="dataSetId" value="<%= bean.getDataSetId() %>">
    <br>

    <input type="submit" value="Make request">
</form>

<textarea name="reason" form="requestForm">Enter your reason for requesting this data here ...</textarea>

