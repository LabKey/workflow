<%
/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
<%@ page import="org.labkey.api.view.template.ClientDependencies" %>
<%@ page import="org.labkey.api.workflow.TaskFormField" %>
<%@ page import="org.labkey.api.workflow.WorkflowProcess" %>
<%@ page import="org.labkey.workflow.WorkflowManager" %>
<%@ page import="java.io.FileNotFoundException" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    @Override
    public void addClientDependencies(ClientDependencies dependencies)
    {
        dependencies.add("Ext4");
        dependencies.add("workflow/view/startProcess.js");
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

<%= text(navigationLinks(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(), null)) %>
<br><br>
<%
    if (!bean.isDeployed(getContainer()))
    {
        if (bean.canDeploy(getUser(), getContainer()))
        {
            try
            {
                WorkflowManager.get().makeContainerDeployment(bean.getProcessDefinitionModule(), bean.getProcessDefinitionKey(), getContainer());
                fields = WorkflowManager.get().getStartFormFields(bean.getProcessDefinitionKey());
            }
            catch (FileNotFoundException e)
            {
%>
No process deployed with key <%= h(bean.getProcessDefinitionKey()) %>
<%
            }
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
<%= text(actionForm(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(),
        "javascript:startWorkflowProcess(" + qh(bean.getProcessDefinitionKey()) + ", ['" + StringUtils.join(fields.keySet(), "', '") + "', '" + StringUtils.join(hiddenFields.keySet(), "', '") + "'], " + qh(bean.getProcessDefinitionKey()) + ")",
        fields, hiddenFields))
%>
<%
    }
%>
