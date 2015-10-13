<%
/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
%>
<%@ page import="org.labkey.api.util.PageFlowUtil" %>
<%@ page import="org.labkey.api.view.ActionURL" %>
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
        <%= PageFlowUtil.textLink(h(entry.getValue()), new ActionURL(WorkflowController.SummaryAction.class, getViewContext().getContainer()).addParameter("processDefinitionKey", entry.getKey()))%>
    </li>
</ul>
<%
        }
    }

%>
