<%
/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
%>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
<%@ page import="org.labkey.api.view.HttpView" %>
<%@ page import="org.labkey.api.view.template.ClientDependency" %>
<%@ page import="org.labkey.workflow.WorkflowController" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page extends="org.labkey.workflow.view.WorkflowViewBase" %>
<%@ taglib prefix="labkey" uri="http://www.labkey.org/taglib" %>
<%!
    public LinkedHashSet<ClientDependency> getClientDependencies()
    {
        LinkedHashSet<ClientDependency> resources = new LinkedHashSet<>();
        resources.add(ClientDependency.fromPath("Ext4"));
        resources.add(ClientDependency.fromPath("workflow/view/reassignTask.js"));

        return resources;
    }
%>
<%
    HttpView me = HttpView.currentView();
    WorkflowController.WorkflowRequestForm bean = (WorkflowController.WorkflowRequestForm) me.getModelBean();
%>

<labkey:errors></labkey:errors>
<%= navigationLinks(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(), null) %>
<br>
<br>

<%
    if (me.getView("workflowListQueryView") != null)
    {
%>
<div>
    <% me.include(me.getView("workflowListQueryView"),out); %>
</div>
<br><br>
<%
    }
%>
<%= navigationLinks(bean.getProcessDefinitionName(), bean.getProcessDefinitionKey(), null) %>

