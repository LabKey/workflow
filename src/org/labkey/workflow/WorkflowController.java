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

import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.labkey.api.action.ApiAction;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.BaseViewAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.security.Group;
import org.labkey.api.security.PrincipalType;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Marshal(Marshaller.Jackson)
public class WorkflowController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(WorkflowController.class);
    public static final String NAME = "workflow";
    private static final String ARGOS_PROCESS_KEY = "argosDataExport";

    public WorkflowController()
    {
        setActionResolver(_actionResolver);
    }

    /**
     * Shows a summary of the workflows for the current container and user
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        private String _navLabel = "Workflow Summary";

        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return new JspView("/org/labkey/workflow/view/workflowSummary.jsp", WorkflowManager.get().getProcessSummary(getUser(), getContainer()));
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }


    /**
     * Shows the data about a task
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class ViewTaskAction extends SimpleViewAction<ProcessInstanceDetailsForm>
    {
        private String _navLabel = "View task details";

        public ModelAndView getView(ProcessInstanceDetailsForm form, BindException errors) throws Exception
        {
            WorkflowTask bean = getTaskDetails(form.getTaskId(), getUser());

            return new JspView("/org/labkey/workflow/view/workflowTask.jsp", bean, errors);
        }

        private WorkflowTask getTaskDetails(String taskId, User user) throws Exception
        {
            Task engineTask = WorkflowManager.get().getTask(taskId);
            if (engineTask != null)
            {
                WorkflowTask bean = new WorkflowTask(engineTask.getId(), engineTask.getTaskDefinitionKey(), engineTask.getProcessInstanceId(), engineTask.getDescription(), engineTask.getProcessVariables());
                return bean;
            }
            else
            {
                throw new Exception("No such task: " + taskId);
            }
        }


        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class ViewProcessInstanceAction extends SimpleViewAction<ProcessInstanceDetailsForm>
    {
        private String _navLabel = "View workflow process details";

        public ModelAndView getView(ProcessInstanceDetailsForm form, BindException errors) throws Exception
        {
            ExportRequestDetailsBean bean = new ExportRequestDetailsBean(form.getProcessInstanceId());

            return new JspView("/org/labkey/workflow/view/workflowProcessInstance.jsp", bean, errors);
        }


        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }


    private static class ProcessInstanceDetailsForm
    {
        private String _processInstanceId;
        private String _taskId;

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
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
            if (getViewContext().getRequest().getParameter("processInstanceId") != null)
                stream = WorkflowManager.get().getProcessDiagram(getViewContext().getRequest().getParameter("processInstanceId"));
            else if (getViewContext().getRequest().getParameter("processName") != null)
                stream = WorkflowManager.get().getProcessDiagramByKey(getViewContext().getRequest().getParameter("processName"), getContainer());
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

    @RequiresPermissionClass(ReadPermission.class)
    public class AssignTaskAction extends ApiAction<TaskAssignmentForm>
    {
        @Override
        public Object execute(TaskAssignmentForm form, BindException errors) throws Exception
        {
            Task engineTask = WorkflowManager.get().getTask(form.getTaskId());
            engineTask.setAssignee(String.valueOf(form.getUserId()));
            return success();
        }
    }

    public static class TaskAssignmentForm
    {
        private String _taskId;
        private int _userId;

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public int getUserId()
        {
            return _userId;
        }

        public void setUserId(int userId)
        {
            _userId = userId;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class GetTasksAction extends ApiAction<TaskListRequestForm>
    {
        @Override
        public Object execute(TaskListRequestForm form, BindException errors) throws Exception
        {
            List<Task> tasks = new ArrayList<Task>();
            if (form.getPrincipalType() == PrincipalType.USER)
            {
                User user = UserManager.getUser(form.getPrincipalId());
                tasks.addAll(WorkflowManager.get().getTaskList(user, getContainer()));
                if (form.getIncludeGroupTasks() )
                {
                    tasks.addAll(WorkflowManager.get().getGroupTasks(user));
                }
            }
            if (form.getPrincipalType() == PrincipalType.GROUP)
            {
                Group group = SecurityManager.getGroup(form.getPrincipalId());
                tasks.addAll(WorkflowManager.get().getTaskList(group));
                if (form.getIncludeGroupTasks())
                {
                    tasks.addAll(WorkflowManager.get().getGroupTasks(group));
                }
            }
            return success(tasks);
        }
    }

    private static class TaskListRequestForm
    {
        private Integer _principalId;
        private PrincipalType _principalType;
        private Boolean _includeGroupTasks;

        public Boolean getIncludeGroupTasks()
        {
            return _includeGroupTasks;
        }

        public void setIncludeGroupTasks(Boolean includeGroupTasks)
        {
            _includeGroupTasks = includeGroupTasks;
        }

        public void setPrincipalId(Integer principalId)
        {
            _principalId = principalId;
        }

        public PrincipalType getPrincipalType()
        {
            return _principalType;
        }

        public void setPrincipalType(PrincipalType principalType)
        {
            _principalType = principalType;
        }

        public Integer getPrincipalId()
        {
            return _principalId;
        }

        public void setPrincipalId(int principalId)
        {
            _principalId = principalId;
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class StartProcessAction extends ApiAction<WorkflowProcess>
    {
        @Override
        public Object execute(WorkflowProcess form, BindException errors) throws Exception
        {
            if (form.getProcessKey() == null)
                throw new Exception("No process key provided");

            ApiSimpleResponse response = new ApiSimpleResponse();

            String instanceId = WorkflowManager.get().startWorkflow(form, getContainer());
            response.put("processInstanceId", instanceId);
            return success(response);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class RemoveProcessAction extends ApiAction<WorkflowProcess>
    {
        @Override
        public Object execute(WorkflowProcess form, BindException errors) throws Exception
        {
            if (form.getId() == null)
                throw new Exception("No process id provided");

            WorkflowManager.get().deleteProcessInstance(form.getProcessInstanceId(), null);
            return success();
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class CompleteTaskAction extends ApiAction<TaskCompletionForm>
    {
        @Override
        public Object execute(TaskCompletionForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getTaskId() == null) // TODO convert to "validate" method
                throw new Exception("Task id cannot be null.");
            else
            {
                // TODO remove this hack and use variables from the form
                Map<String, Object> variables = null;
                if (form.getApproved() != null)
                {
                    variables = form.getProcessVariables();
                    if (variables == null)
                        variables = new HashMap<String, Object>();
                    variables.put("approved", form.getApproved());
                }
                WorkflowManager.get().updateProcessVariables(form.getTaskId(), variables);
                // TODO check if the task is assigned to the user or the user's group before allowing it to be completed
                WorkflowManager.get().completeTask(form.getTaskId());
                response.put("status", "success");
            }
            return response;
        }
    }

    public static class TaskCompletionForm
    {
        private String _taskId;
        private String _processInstanceId;
        private Map<String, Object> _processVariables;
        private Boolean _approved; // TODO remove this hack

        public Boolean getApproved()
        {
            return _approved;
        }

        public void setApproved(Boolean approved)
        {
            _approved = approved;
        }

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
        }

        public Map<String, Object> getProcessVariables()
        {
            return _processVariables;
        }

        public void setProcessVariables(Map<String, Object> processVariables)
        {
            _processVariables = processVariables;
        }
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
                response.put("deploymentId", WorkflowManager.get().deployWorkflow(form.getProcessName(), getContainer()));
            }
            return success(response);
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

    // TODO the methods below here are specific to the data export example.
    // TODO change ExportRequestDetailsForm to generic WorkflowForm?
    @RequiresPermissionClass(ReadPermission.class)
    public class RequestExportAction extends SimpleViewAction<ExportRequestDetailsBean>
    {
        private String _navLabel = "Data Export Request";

        public ModelAndView getView(ExportRequestDetailsBean form, BindException errors) throws Exception
        {
            if (form.getProcessInstanceId() != null)
            {
                form = new ExportRequestDetailsBean(form.getProcessInstanceId());
            }

            return new JspView("/org/labkey/workflow/view/requestExport.jsp", form, errors);

        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class SubmitRequestAction extends ApiAction<ExportRequestDetailsBean>
    {
        @Override
        public Object execute(ExportRequestDetailsBean form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getDataSetId() != null)
            {
                WorkflowProcess process = new WorkflowProcess();
                process.setProcessKey(ARGOS_PROCESS_KEY);

                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("requester", getUser());
                variables.put("requesterId", getUser().getUserId());
                variables.put("dataSetId", form.getDataSetId());
                variables.put("reason", form.getReason());
                variables.put("requester", form.getUser());
                variables.put("container", getContainer().getId());
                process.setProcessVariables(variables);


                String instanceId = WorkflowManager.get().startWorkflow(process, getContainer());

                form.setProcessInstanceId(instanceId);

                response.put("processInstanceId", form.getProcessInstanceId());
                return success(response);
            }
            else
                throw new Exception("Data set id cannot be null");
        }
    }

    public static class ExportRequestDetailsBean
    {
        private String _processInstanceId;
        private User _user;
        private Integer _dataSetId;
        private String _reason;
        private List<String> _currentTasks;
        private String _taskId;
        private String _taskState;

        public ExportRequestDetailsBean()
        {
        }

        public ExportRequestDetailsBean(String processInstanceId) throws Exception
        {
            Map<String, Object> details = WorkflowManager.get().getProcessInstanceDetails(processInstanceId);
            this.setDataSetId((Integer) details.get("dataSetId"));
            this.setReason((String) details.get("reason"));
            this.setUser((User) details.get("requester"));
            this.setProcessInstanceId(processInstanceId);
            this.setCurrentTasks((List<String>) details.get("currentTasks"));
        }

        public String getTaskState()
        {
            return _taskState;
        }

        public void setTaskState(String taskState)
        {
            _taskState = taskState;
        }

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public void setCurrentTasks(List<String> currentTasks)
        {
            _currentTasks = currentTasks;
        }

        public List<String> getCurrentTasks()
        {
            return _currentTasks;
        }

        public User getUser()
        {
            return _user;
        }

        public void setUser(User user)
        {
            _user = user;
        }

        public Integer getDataSetId()
        {
            return _dataSetId;
        }

        public void setDataSetId(Integer dataSetId)
        {
            _dataSetId = dataSetId;
        }

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
    }

}