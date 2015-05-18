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
<%@ page import="org.labkey.workflow.WorkflowManager" %>
<%@ page import="java.io.File" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.api.jsp.JspBase" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("workflow/workflow.js"));

        return resources;
    }
%>
<%
    HttpView me = HttpView.currentView();

    WorkflowController.AllWorkflowsBean bean = (WorkflowController.AllWorkflowsBean) me.getModelBean();
    Map<String, String> processDefinitions = bean.getWorkflowDefinitions();
    List<File> models = WorkflowManager.get().getWorkflowModels(getViewContext().getContainer());

%>
The models currently available in this container are:
<ul>
<%
    for (File model : models)
    {
%>
    <li><%= model.getAbsolutePath() %> <%= PageFlowUtil.textLink("Deploy",
            new ActionURL(WorkflowController.DeployAction.class, getViewContext().getContainer()).addParameter("file", model.getAbsolutePath())) %></li>
<%
    }
%>
</ul>
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
        <%= PageFlowUtil.textLink(entry.getValue(), new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", entry.getKey()))%>
    </li>
</ul>
<%
        }
    }

%>
