<%
/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
%>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.api.workflow.TaskFormField" %>
<%@ page import="org.labkey.api.workflow.WorkflowProcess" %>
<%@ page import="org.labkey.workflow.WorkflowManager" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.Map" %>
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
