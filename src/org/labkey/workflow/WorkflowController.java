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
import org.labkey.api.data.Container;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.Group;
import org.labkey.api.security.PrincipalType;
import org.labkey.api.security.RequiresPermissionClass;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.UserPrincipal;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.workflow.query.WorkflowQuerySchema;
import org.labkey.workflow.model.WorkflowProcess;
import org.labkey.workflow.model.WorkflowSummary;
import org.labkey.workflow.model.WorkflowTask;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Marshal(Marshaller.Jackson)
public class WorkflowController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(WorkflowController.class);
    public static final String NAME = "workflow";



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
            AllWorkflowsBean bean = new AllWorkflowsBean(getContainer());
            return new JspView("/org/labkey/workflow/view/allWorkflows.jsp", bean);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    public class AllWorkflowsBean
    {
        private Map<String, String> _workflowDefinitions = new HashMap<>();

        public AllWorkflowsBean() {}

        public AllWorkflowsBean(Container container)
        {
            setWorkflowDefinitions(WorkflowManager.get().getProcessDefinitionNames(container));
        }

        public Map<String, String> getWorkflowDefinitions()
        {
            return _workflowDefinitions;
        }

        public void setWorkflowDefinitions(Map<String, String> workflowDefinitions)
        {
            _workflowDefinitions = workflowDefinitions;
        }
    }

    /**
     * Shows a summary of a given workfow for the current container and user, including the number of tasks
     * and number of workflow instances currently active.
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class SummaryAction extends SimpleViewAction<WorkflowRequestForm>
    {
        private String _navLabel = "Workflow Summary";

        public ModelAndView getView(WorkflowRequestForm form, BindException errors) throws Exception
        {
            WorkflowSummary bean = new WorkflowSummary(form.getProcessDefinitionKey(), getUser(), getContainer());

            return new JspView("/org/labkey/workflow/view/workflowSummary.jsp", bean);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class TaskListAction extends SimpleViewAction<WorkflowRequestForm>
    {
        private String _navLabel = "Tasks";

        @Override
        public ModelAndView getView(WorkflowRequestForm bean, BindException errors) throws Exception
        {
            JspView jsp = new JspView("/org/labkey/workflow/view/workflowList.jsp", bean);
            jsp.setTitle("Assigned Tasks");

            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), WorkflowQuerySchema.NAME);
            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, WorkflowQuerySchema.TABLE_TASK);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);

            jsp.setView("workflowListQueryView", queryView);

            return jsp;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }

    }

    @RequiresPermissionClass(ReadPermission.class)
    public class InstanceListAction extends SimpleViewAction<WorkflowRequestForm>
    {

        @Override
        public ModelAndView getView(WorkflowRequestForm bean, BindException errors) throws Exception
        {
            UserSchema schema = QueryService.get().getUserSchema(getUser(), getContainer(), WorkflowQuerySchema.NAME);
            JspView jsp = new JspView("/org/labkey/workflow/view/workflowList.jsp", bean);
            jsp.setTitle("Active Processes");

            QuerySettings settings = schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, WorkflowQuerySchema.TABLE_PROCESS_INSTANCE);
            QueryView queryView = schema.createView(getViewContext(), settings, errors);

            jsp.setView("workflowListQueryView", queryView);

            return jsp;
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return null;
        }

    }


    /**
     * Shows the data about a task if the user has permissions to see this task
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class TaskAction extends SimpleViewAction<WorkflowTaskForm>
    {
        private String _navLabel = "Task details";

        public ModelAndView getView(WorkflowTaskForm form, BindException errors) throws Exception
        {
            return new JspView("/org/labkey/workflow/view/workflowTask.jsp", new WorkflowTask(WorkflowManager.get().getTask(form.getTaskId())), errors);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    /**
     * View the details of a process instance.
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class ProcessInstanceAction extends SimpleViewAction<ProcessInstanceDetailsForm>
    {
        private String _navLabel = "Workflow process instance details";

        public ModelAndView getView(ProcessInstanceDetailsForm form, BindException errors) throws Exception
        {
            WorkflowProcess bean = new WorkflowProcess(form.getProcessInstanceId(), getUser(), getContainer());

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

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
        }
    }

    /**
     * Retrieves the process diagram associated with a particular processInstance or processName as a png.
     * If there is no such diagram, a plain text response will be returned indicating that there is no such image.
     */
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
            else if (getViewContext().getRequest().getParameter("processDefinitionKey") != null)
                stream = WorkflowManager.get().getProcessDiagramByKey(getViewContext().getRequest().getParameter("processDefinitionKey"), getContainer());
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

    /**
     * Claims a task for a user, making that user the owner and the assignee.
     */
    @RequiresPermissionClass(UpdatePermission.class)
    public class ClaimTaskAction extends ApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
           WorkflowManager.get().claimTask(form.getTaskId(), form.getAssigneeId());
            return success();
        }
    }

    /**
     * Delegates a task to a particular user.  The owner of the task remains unchanged.
     */
    @RequiresPermissionClass(UpdatePermission.class)
    public class DelegateTaskAction extends ApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            WorkflowManager.get().delegateTask(form.getTaskId(), form.getOwnerId(), form.getAssigneeId());
            return success();
        }
    }

    /**
     * Assigns a task to a user
     */
    @RequiresPermissionClass(UpdatePermission.class)
    public class AssignTaskAction extends ApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            WorkflowManager.get().assignTask(form.getTaskId(), form.getAssigneeId());
            return success();
        }
    }

    private static class WorkflowTaskForm
    {
        private String _taskId;
        private int _ownerId;
        private int _assigneeId;

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public int getAssigneeId()
        {
            return _assigneeId;
        }

        public void setAssigneeId(int assigneeId)
        {
            _assigneeId = assigneeId;
        }

        public int getOwnerId()
        {
            return _ownerId;
        }

        public void setOwnerId(int ownerId)
        {
            _ownerId = ownerId;
        }
    }

    /**
     * Retrieves the tasks for a given PrincipalUser.  Depending on the taskInvolvement settings, retrieves only those
     * tasks for the principal or includes tasks that have the principal's groups as candidate groups.
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class GetTasksAction extends ApiAction<TaskListRequestForm>
    {
        @Override
        public Object execute(TaskListRequestForm form, BindException errors) throws Exception
        {
            Map<UserPrincipal, List<Task>> tasks = new HashMap<>();
            if (form.getPrincipalType() == PrincipalType.USER)
            {
                User user = UserManager.getUser(form.getPrincipalId());
                if (user != null)
                {
                    tasks.put(user, WorkflowManager.get().getTaskList(user, getContainer(), form.getInvolvement()));
                }
            }
            else if (form.getPrincipalType() == PrincipalType.GROUP)
            {
                Group group = SecurityManager.getGroup(form.getPrincipalId());
                if (group != null)
                {
                    tasks.put(group, WorkflowManager.get().getTaskList(group));
                }
            }
            return success(tasks);
        }
    }

    public static class WorkflowRequestForm
    {
        private String _processDefinitionKey;

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }
    }

    private static class TaskListRequestForm extends WorkflowRequestForm
    {
        private Integer _principalId;
        private PrincipalType _principalType;
        private Set<WorkflowManager.TaskInvolvement> _involvement;

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

        public Set<WorkflowManager.TaskInvolvement> getInvolvement()
        {
            return _involvement;
        }

        public void setInvolvement(Set<String> involvement)
        {
            _involvement = new HashSet<>();
            for (String item : involvement)
            {
                _involvement.add(WorkflowManager.TaskInvolvement.valueOf(item.toUpperCase()));
            }
        }

    }

    /**
     * Creates a new instance of a process with a given processKey and returns the id of the new instance on success.
     */
    @RequiresPermissionClass(UpdatePermission.class)
    public class StartProcessAction extends ApiAction<WorkflowProcess>
    {
        @Override
        public Object execute(WorkflowProcess form, BindException errors) throws Exception
        {
            if (form.getProcessDefinitionKey() == null)
                throw new Exception("No process key provided");

            ApiSimpleResponse response = new ApiSimpleResponse();

            String instanceId = WorkflowManager.get().startWorkflow(form, getContainer());
            response.put("processInstanceId", instanceId);
            return success(response);
        }
    }

    /**
     * Deletes a particular process instance.  This is allowed for the initiator of the
     * process and for administrators.
     */
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

    /**
     * Complete a task in a workflow.  This is allowed only if the task is currently assigned
     * to the current user ???or one of the groups the user is in is a candidate???
     */
    @RequiresPermissionClass(ReadPermission.class)
    public class CompleteTaskAction extends ApiAction<TaskCompletionForm>
    {
        @Override
        public Object execute(TaskCompletionForm form, BindException errors) throws Exception
        {
            // TODO check if the task is assigned to the user or the user's group before allowing it to be completed
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getTaskId() == null) // TODO convert to "validate" method
                throw new Exception("Task id cannot be null.");
            else if (form.getProcessVariables() != null)
            {
                WorkflowManager.get().updateProcessVariables(form.getTaskId(), form.getProcessVariables());
                WorkflowManager.get().completeTask(form.getTaskId());
                response.put("status", "success");
            }
            else // TODO remove this hack and use variables from the form
            {
                Map<String, Object> variables = null;
                if (form.getApproved() != null)
                {
                    variables = form.getProcessVariables();
                    if (variables == null)
                        variables = new HashMap<String, Object>();
                    variables.put("approved", form.getApproved());
                }
                WorkflowManager.get().updateProcessVariables(form.getTaskId(), variables);
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

    /**
     * Creates a new deployment of a process definition
     */
    @RequiresPermissionClass(AdminPermission.class)
    public class DeployAction extends ApiAction<DeploymentForm>
    {
        @Override
        public Object execute(DeploymentForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getProcessDefinitionKey() != null)
            {
                response.put("deploymentId", WorkflowManager.get().deployWorkflow(form.getProcessDefinitionKey(), getContainer()));
            }
            return success(response);
        }
    }

    public static class DeploymentForm
    {
        private String _processDefinitionKey;

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }
    }

    // TODO the methods and classes below here are specific to the data export example.
    private static final String ARGOS_PROCESS_KEY = "argosDataExportSimple";

    @RequiresPermissionClass(ReadPermission.class)
    public class StartExportAction extends SimpleViewAction
    {
        private String _navLabel = "Workflow Summary";

        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            return new JspView("/org/labkey/workflow/view/exportRequest.jsp", null);
        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class RequestExportAction extends SimpleViewAction<ExportRequestDetailsBean>
    {
        private String _navLabel = "Data Export Request";

        public ModelAndView getView(ExportRequestDetailsBean form, BindException errors) throws Exception
        {
            if (form.getProcessInstanceId() != null)
            {
                form = new ExportRequestDetailsBean(form.getProcessInstanceId(), getUser(), getContainer());
            }

            return new JspView("/org/labkey/workflow/view/requestExport.jsp", form, errors);

        }

        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild(_navLabel);
        }
    }

    @RequiresPermissionClass(ReadPermission.class)
    public class SubmitRequestAction extends SimpleViewAction<ExportRequestDetailsBean>
    {

        @Override
        public ModelAndView getView(ExportRequestDetailsBean form, BindException errors) throws Exception
        {
            if (form.getDataSetId() != null)
            {
                WorkflowProcess process = new WorkflowProcess();
                process.setProcessDefintionKey(ARGOS_PROCESS_KEY);
                process.setInitiatorId(getUser().getUserId());

                Map<String, Object> variables = new HashMap<String, Object>();
                variables.put("userId", String.valueOf(getUser().getUserId())); // N.B. This needs to be a string if used as a variable for the candidate assignment
                variables.put("approverGroupId", "-1");  // -1 is the Administrator group  HACK!
                variables.put("dataSetId", form.getDataSetId());
                variables.put("reason", form.getReason());
                variables.put("container", getContainer().getId());
                process.setProcessVariables(variables);
                process.setName("Request from " + getUser() + " for export of data set " + form.getDataSetId());

                String instanceId = WorkflowManager.get().startWorkflow(process, getContainer());

                form.setProcessInstanceId(instanceId);

                WorkflowSummary bean = new WorkflowSummary(ARGOS_PROCESS_KEY, getUser(), getContainer());

                return new JspView("/org/labkey/workflow/view/workflowSummary.jsp", bean);
            }
            else
                throw new Exception("Data set id cannot be null");
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root;
        }
    }

    public static class ExportRequestDetailsBean
    {
        private String _processInstanceId;
        private User _user;
        private Integer _dataSetId;
        private String _reason;
        private List<WorkflowTask> _currentTasks;
        private String _taskId;
        private String _taskState;

        public ExportRequestDetailsBean()
        {
        }

        public ExportRequestDetailsBean(String processInstanceId, User user, Container container) throws Exception
        {
            Map<String, Object> details = WorkflowManager.get().getProcessInstanceVariables(processInstanceId);
            this.setDataSetId((Integer) details.get("dataSetId"));
            this.setReason((String) details.get("reason"));
            this.setUser((User) details.get("requester"));
            this.setProcessInstanceId(processInstanceId);
            this.setCurrentTasks(WorkflowManager.get().getCurrentProcessTasks(processInstanceId, user, container));
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

        public void setCurrentTasks(List<WorkflowTask> currentTasks)
        {
            _currentTasks = currentTasks;
        }

        public List<WorkflowTask> getCurrentTasks()
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