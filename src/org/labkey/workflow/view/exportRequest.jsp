<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
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
<%@ page extends="org.labkey.api.jsp.JspBase" %>

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