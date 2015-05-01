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

package org.labkey.workflow;

import org.apache.commons.io.IOUtils;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.BaseViewAction;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.workflow.view.ExportRequestDetailsBean;
import org.labkey.workflow.view.ProcessSummaryBean;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(WorkflowController.class);
    public static final String NAME = "workflow";
    private static final String ARGOS_PROCESS_KEY = "argosDataExport";

    public WorkflowController()
    {
        setActionResolver(_actionResolver);
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            ProcessSummaryBean bean = new ProcessSummaryBean();
            bean.setNumDefinitions(WorkflowManager.get().getProcessDefinitionCount());
            bean.setAssignedTasks(WorkflowManager.get().getTaskList(getUser()));
            bean.setInstances(WorkflowManager.get().getProcessInstances(getUser()));
            return new JspView("/org/labkey/workflow/view/workflowSummary.jsp", bean);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class RequestExportAction extends SimpleViewAction<ExportRequestForm>
    {
        private String _navLabel = "Data Export Request";

        public ModelAndView getView(ExportRequestForm form, BindException errors) throws Exception
        {
            if (form.getProcessInstanceId() == null || form.getReason() == null)
            {
                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("userId", getUser().getUserId());
                variables.put("dataSetId", form.getDataSetId());

                String instanceId = WorkflowManager.get().startWorkflow(ARGOS_PROCESS_KEY, variables, getUser());
                form.setProcessInstanceId(instanceId);

                return new JspView("/org/labkey/workflow/view/requestExport.jsp", form, errors);
            }
            else
            {
                submitReviewRequest(form.getReason(), form.getProcessInstanceId());

                return new HtmlView("Request has been made for data set " + form.getDataSetId());

            }
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class SubmitRequestAction extends ApiAction<ExportRequestForm>
    {
        @Override
        public Object execute(ExportRequestForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getDataSetId() != null)
            {
                submitReviewRequest(form.getReason(), form.getProcessInstanceId());
                response.put("status", "success");
                response.put("dataSetId", form.getDataSetId());
            }
            return response;
        }
    }

    public static class ExportRequestForm
    {
        private Integer _dataSetId;
        private String _processId;
        private String _processInstanceId;
        private String _reason;

        public String getReason()
        {
            return _reason;
        }

        public void setReason(String reason)
        {
            _reason = reason;
        }

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
        }

        public String getProcessId()
        {
            return _processId;
        }

        public void setProcessId(String processId)
        {
            _processId = processId;
        }

        public Integer getDataSetId()
        {
            return _dataSetId;
        }

        public void setDataSetId(Integer dataSetId)
        {
            _dataSetId = dataSetId;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class ReviewRequestAction extends SimpleViewAction<RequestDetailsForm>
    {
        private String _navLabel = "Review export request";

        public ModelAndView getView(RequestDetailsForm form, BindException errors) throws Exception
        {
            ExportRequestDetailsBean bean = getRequestDetails(form.getRequestId(), getUser());

            return new JspView("/org/labkey/workflow/view/workflowRequest.jsp", bean, errors);
        }


        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }

    }

    public ExportRequestDetailsBean getRequestDetails(String requestId, User user) throws Exception
    {
        Map<String, Object> details = WorkflowManager.get().getProcessInstanceDetails(requestId);
        ExportRequestDetailsBean detailsBean = new ExportRequestDetailsBean();
        detailsBean.setDataSetId((Integer) details.get("dataSetId"));
        detailsBean.setReason((String) details.get("reason"));
        detailsBean.setUser(UserManager.getUser(user.getUserId()));
        detailsBean.setRequestId(requestId);
        detailsBean.setCurrentTasks((List<String>) details.get("currentTasks"));
        if (detailsBean.getCurrentTasks().contains("reviewExportRequest")) // TODO determine based on user roles
            detailsBean.setIsReviewer(true);
        return detailsBean;
    }

    private static class RequestDetailsForm
    {
        private String _requestId;

        public String getRequestId()
        {
            return _requestId;
        }

        public void setRequestId(String requestId)
        {
            _requestId = requestId;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class ProcessDiagramAction extends BaseViewAction
    {

        @Override
        protected String getCommandClassMethodName()
        {
            return null;
        }

        @Override
        public ModelAndView handleRequest() throws Exception
        {
            InputStream stream = null;
            String contentType = "image/png";
            if (getViewContext().getRequest().getParameter("requestId") != null)
                stream = WorkflowManager.get().getProcessDiagram(getViewContext().getRequest().getParameter("requestId"));
            else if (getViewContext().getRequest().getParameter("processName") != null)
                stream = WorkflowManager.get().getProcessDiagramByKey(getViewContext().getRequest().getParameter("processName"));
            if (stream == null)
            {
                contentType = "text/plain";
                stream = new ByteArrayInputStream("Unable to retrieve process diagram.  Perhaps you need to deploy the process.".getBytes());
            }
            byte[] imageBytes = IOUtils.toByteArray(stream);
            HttpServletResponse response = getViewContext().getResponse();

            response.setContentType(contentType);
            response.setContentLength(imageBytes.length);
            response.getOutputStream().write(imageBytes);
            return null;
        }

        @Override
        public void validate(Object o, Errors errors)
        {
            return;
        }
    }

    @RequiresPermissionClass(AdminPermission.class)
    public class ApproveRequestAction extends SimpleViewAction<RequestDetailsForm>
    {

        public ModelAndView getView(RequestDetailsForm form, BindException errors) throws Exception
        {
            ExportRequestDetailsBean bean = getRequestDetails(form.getRequestId(), getUser());
            approveRequest(bean);

            return new JspView("/org/labkey/workflow/view/workflowRequest.jsp", bean, errors);
        }


        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }

    }

    public void submitReviewRequest(String reason, String processInstanceId)
    {
        WorkflowManager.get().getRuntimeService().setVariableLocal(processInstanceId, "reason", reason);
        WorkflowManager.get().completeWorkflowTask(processInstanceId, 0);
    }

    public void approveRequest(ExportRequestDetailsBean exportRequest) throws Exception
    {
        WorkflowManager.get().completeWorkflowTask(exportRequest.getRequestId(), 0);
        exportRequest = getRequestDetails(exportRequest.getRequestId(), getUser());
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class DeployAction extends ApiAction<DeploymentForm>
    {
        @Override
        public Object execute(DeploymentForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getProcessName() != null)
            {
                WorkflowManager.get().deployWorkflow(form.getProcessName());
            }
            return response;
        }
    }

    public static class DeploymentForm
    {
        private String _processName;

        public String getProcessName()
        {
            return _processName;
        }

        public void setProcessName(String processName)
        {
            _processName = processName;
        }
    }
}