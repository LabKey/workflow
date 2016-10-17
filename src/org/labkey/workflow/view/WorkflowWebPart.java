package org.labkey.workflow.view;

import org.labkey.api.data.Container;
import org.labkey.api.view.JspView;
import org.labkey.api.view.ViewContext;
import org.labkey.workflow.WorkflowController;

/**
 * Created by susanh on 9/30/16.
 */
public class WorkflowWebPart extends JspView
{
    public WorkflowWebPart(Container container)
    {
        super("/org/labkey/workflow/view/allWorkflows.jsp");
        this.setModelBean( new WorkflowController.AllWorkflowsBean(container));
        setTitle("Workflow List");
    }

    public WorkflowWebPart(ViewContext v)
    {
        this(v.getContainer());
    }
}

