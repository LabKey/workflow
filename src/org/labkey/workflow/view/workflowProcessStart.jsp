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
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.workflow.TaskFormField" %>
<%@ page import="org.labkey.api.workflow.WorkflowProcess" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>
<%@ page import="org.labkey.workflow.WorkflowManager" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));;
        resources.add(ClientDependency.fromPath("workflow/view/startProcess.js"));

        return resources;
    }
%>
<%
    HttpView me = HttpView.currentView();
    WorkflowProcess bean = (WorkflowProcess) me.getModelBean();
    Map<String, TaskFormField> fields = bean.getStartFormFields(getContainer());
    Map<String, String> hiddenFields = new HashMap<String, String>();
    hiddenFields.put("workflowModelModule", bean.getProcessDefinitionModule());
%>
<labkey:errors></labkey:errors>

<%= navigationLinks(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(), null)%>
<br><br>
<%
    if (!bean.isDeployed(getContainer()))
    {
        if (bean.canDeploy(getUser(), getContainer()))
        {
            WorkflowManager.get().makeContainerDeployment(bean.getProcessDefinitionModule(), bean.getProcessDefinitionKey(), getContainer());
            fields = WorkflowManager.get().getStartFormFields(bean.getProcessDefinitionKey());
        }
        else
        {
%>
No process deployed with key <%= h(bean.getProcessDefinitionKey()) %>
<%
        }
    }
    if (bean.isDeployed(getContainer()))
    {
%>
<%= actionForm(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(),
        "javascript:startWorkflowProcess(" + qh(bean.getProcessDefinitionKey()) + ", ['" + StringUtils.join(fields.keySet(), "', '") + "', '" + StringUtils.join(hiddenFields.keySet(), "', '") + "'], " + qh(bean.getProcessDefinitionKey()) + ")",
        fields, hiddenFields)
%>
<%
    }
%>