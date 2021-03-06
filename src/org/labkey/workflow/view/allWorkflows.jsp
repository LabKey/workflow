<%
/*
 * Copyright (c) 2015-2019 LabKey Corporation
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
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%
    HttpView me = HttpView.currentView();

    WorkflowController.AllWorkflowsBean bean = (WorkflowController.AllWorkflowsBean) me.getModelBean();
    Map<String, String> processDefinitions = bean.getWorkflowDefinitions();
%>
<labkey:errors></labkey:errors>
<%
    if (processDefinitions.isEmpty())
    {
%>
There are currently no workflows deployed in this container.
<%
    }
    else
    {
%>
The following workflows are currently deployed in this container:

<%
        for (Map.Entry<String, String> entry : processDefinitions.entrySet())
        {
%>
<ul>
    <li>
        <%=link(entry.getValue(), urlFor(WorkflowController.SummaryAction.class).addParameter("processDefinitionKey", entry.getKey()))%>
    </li>
</ul>
<%
        }
    }

%>
