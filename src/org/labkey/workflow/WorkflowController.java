/*
 * Copyright (c) 2015-2019 LabKey Corporation
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

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.ApiResponse;
import org.labkey.api.action.ApiSimpleResponse;
import org.labkey.api.action.BaseViewAction;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.action.MutatingApiAction;
import org.labkey.api.action.ReadOnlyApiAction;
import org.labkey.api.action.SimpleErrorView;
import org.labkey.api.action.SimpleResponse;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.action.SpringActionController;
import org.labkey.api.data.Container;
import org.labkey.api.query.QueryService;
import org.labkey.api.query.QuerySettings;
import org.labkey.api.query.QueryView;
import org.labkey.api.query.UserSchema;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.security.permissions.Permission;
import org.labkey.api.security.permissions.ReadPermission;
import org.labkey.api.security.permissions.UpdatePermission;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.UnauthorizedException;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.WorkflowRegistry;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.model.WorkflowEngineTaskImpl;
import org.labkey.workflow.model.WorkflowHistoricTaskImpl;
import org.labkey.workflow.model.WorkflowProcessImpl;
import org.labkey.workflow.model.WorkflowSummary;
import org.labkey.workflow.query.WorkflowQuerySchema;
import org.labkey.workflow.view.WorkflowWebPart;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Marshal(Marshaller.Jackson)
public class WorkflowController extends SpringActionController
{
    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(WorkflowController.class);
    public static final String NAME = "workflow";

    private static final String PROCESS_DEFINITION_KEY_MISSING = "Process definition key is required";
    private static final String TASK_ID_MISSING = "Task id is required";
    private static final String TASK_ID_UNKNOWN = "Task id is unknown";
    private static final String PROCESS_INSTANCE_ID_MISSING = "Process instance id is required";
    private static final String ASSIGNEE_ID_MISSING = "Assignee id is required";
    private static final String MODULE_NAME_MISSING = "Module name is required";
    private static final String NO_SUCH_TASK_ERROR = "No active task with the given id";
    private static final String NO_SUCH_INSTANCE_ERROR = "No active process instance with the given id";
    private static final String NO_SUCH_DEFINITION_ERROR = "No process definition with the given key";


    public WorkflowController()
    {
        setActionResolver(_actionResolver);
    }

    /**
     * Shows a summary of the workflows for the current container and user
     */
    @RequiresPermission(ReadPermission.class)
    public class BeginAction extends SimpleViewAction
    {
        @Override
        public ModelAndView getView(Object o, BindException errors) throws Exception
        {
            setTitle("Workflows");
            WorkflowWebPart wp = new WorkflowWebPart(getContainer());
            return wp;
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild("Workflow Process List");
        }
    }

    public static class AllWorkflowsBean
    {
        private Map<String, String> _workflowDefinitions = new HashMap<>();

        public AllWorkflowsBean() {}

        public AllWorkflowsBean(@Nullable Container container)
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
     * Shows a summary of a given workflow for the current container and user, including the number of tasks
     * and number of workflow instances currently active for this user.
     */
    @RequiresPermission(ReadPermission.class)
    public class SummaryAction extends SimpleViewAction<WorkflowRequestForm>
    {
        private String _navLabel = "Workflow Summary";

        @Override
        public ModelAndView getView(WorkflowRequestForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);
            WorkflowSummary bean = new WorkflowSummary(form.getProcessDefinitionKey(), getUser(), getContainer());
            _navLabel = bean.getName();

            return new JspView<>("/org/labkey/workflow/view/workflowSummary.jsp", bean, errors);
        }

        @Override
        public void validate(WorkflowRequestForm workflowRequestForm, BindException errors)
        {
            if (workflowRequestForm.getProcessDefinitionKey() == null)
                errors.rejectValue("processDefinitionKey", ERROR_MSG, PROCESS_DEFINITION_KEY_MISSING);
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild(_navLabel);
        }
    }


    @RequiresPermission(ReadPermission.class)
    public class StartProcessFormAction extends SimpleViewAction<StartWorkflowProcessForm>
    {
        @Override
        public ModelAndView getView(StartWorkflowProcessForm form, BindException errors) throws Exception
        {
            WorkflowProcess bean = new WorkflowProcessImpl(form.getProcessDefinitionKey(), form.getWorkflowModelModule());
            JspView jsp = new JspView<>("/org/labkey/workflow/view/workflowProcessStart.jsp", bean, errors);
            return jsp;
        }

        @Override
        public void validate(StartWorkflowProcessForm form, BindException errors)
        {
            form.validate(getUser(), getContainer(), errors);
        }

        @Override
        public void addNavTrail(NavTree root)
        {
        }
    }

    /**
     * Shows a list of tasks that are associated with a particular workflow.
     */
    @RequiresPermission(ReadPermission.class)
    public class TaskListAction extends SimpleViewAction<WorkflowRequestForm>
    {
        private UserSchema _schema;

        @Override
        public ModelAndView getView(WorkflowRequestForm bean, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            JspView jsp = new JspView<>("/org/labkey/workflow/view/workflowList.jsp", bean, errors);

            ProcessDefinition definition = WorkflowManager.get().getProcessDefinition(bean.getProcessDefinitionKey(), getContainer());
            if (definition != null)
            {
                bean.setProcessDefinitionName(definition.getName());

                jsp.setTitle("Task List for '" + bean.getProcessDefinitionName() + "' workflow instances ");
            }
            else
            {
                jsp.setTitle("Task List for workflow instances");
            }

            if (_schema != null)
            {
                QuerySettings settings = _schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, WorkflowQuerySchema.TABLE_TASK);
                QueryView queryView = _schema.createView(getViewContext(), settings, errors);

                jsp.setView("workflowListQueryView", queryView);
            }

            return jsp;
        }

        @Override
        public void validate(WorkflowRequestForm workflowRequestForm, BindException errors)
        {
            if (workflowRequestForm.getProcessDefinitionKey() == null)
                errors.rejectValue("processDefinitionKey", ERROR_MSG, PROCESS_DEFINITION_KEY_MISSING);
            else
            {
                _schema = QueryService.get().getUserSchema(getUser(), getContainer(), WorkflowQuerySchema.NAME);
                if (_schema == null)
                {
                    errors.reject(ERROR_MSG, WorkflowQuerySchema.SCHEMA_NOT_DEFINED_ERROR);
                }
            }
        }

        @Override
        public void addNavTrail(NavTree root)
        {
        }
    }

    /**
     * Shows a list of the current workflow instances for a given workflow model key
     */
    @RequiresPermission(ReadPermission.class)
    public class InstanceListAction extends SimpleViewAction<WorkflowRequestForm>
    {
        private UserSchema _schema;

        @Override
        public ModelAndView getView(WorkflowRequestForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
            {
                return new SimpleErrorView(errors);
            }

            ProcessDefinition processDefinition = WorkflowManager.get().getProcessDefinition(form.getProcessDefinitionKey(), getContainer());
            form.setProcessDefinitionName(processDefinition.getName());
            JspView jsp = new JspView<>("/org/labkey/workflow/view/workflowList.jsp", form);
            jsp.setTitle("Active Processes");

            QuerySettings settings = _schema.getSettings(getViewContext(), QueryView.DATAREGIONNAME_DEFAULT, WorkflowQuerySchema.TABLE_PROCESS_INSTANCE);
            QueryView queryView = _schema.createView(getViewContext(), settings, errors);

            jsp.setView("workflowListQueryView", queryView);

            return jsp;
        }

        @Override
        public void validate(WorkflowRequestForm form, BindException errors)
        {
            String errorMessage = validateProcessDefinitionKey(form);
            if (errorMessage != null)
                errors.rejectValue("processDefinitionKey", ERROR_MSG, errorMessage);

            _schema = QueryService.get().getUserSchema(getUser(), getContainer(), WorkflowQuerySchema.NAME);
            if (_schema == null)
            {
                errors.reject(ERROR_MSG, WorkflowQuerySchema.SCHEMA_NOT_DEFINED_ERROR);
            }
        }

        @Override
        public void addNavTrail(NavTree root)
        {
        }
    }

    private String validateProcessDefinitionKey(WorkflowRequestForm form)
    {
        if (form.getProcessDefinitionKey() == null)
        {
            return PROCESS_DEFINITION_KEY_MISSING;
        }
        else
        {
            ProcessDefinition processDefinition = WorkflowManager.get().getProcessDefinition(form.getProcessDefinitionKey(), getContainer());
            if (processDefinition == null)
                return NO_SUCH_DEFINITION_ERROR;
        }

        return null;
    }


    /**
     * Shows the data about a task if the user has permissions to see this task
     */
    @RequiresPermission(ReadPermission.class)
    public class TaskAction extends SimpleViewAction<WorkflowTaskForm>
    {
        private String _navLabel = "Task details";
        private WorkflowTask _task;

        @Override
        public ModelAndView getView(WorkflowTaskForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            if (_task.getName() != null)
                _navLabel = "'" + _task.getName() + "' " + (_task.isActive() ? "active" : " inactive ") + " task details";

            return new JspView<>("/org/labkey/workflow/view/workflowTask.jsp", _task, errors);
        }

        @Override
        public void validate(WorkflowTaskForm workflowTaskForm, BindException errors)
        {
            if (workflowTaskForm.getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
            _task = new WorkflowEngineTaskImpl(workflowTaskForm.getTaskId(), getContainer());
            if (!_task.isActive())
                _task = new WorkflowHistoricTaskImpl(workflowTaskForm.getTaskId(), getContainer());
            if (_task.getName() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_UNKNOWN);
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild(_navLabel);
        }
    }

    /**
     * View the details of a process instance.
     */
    @RequiresPermission(ReadPermission.class)
    public class ProcessInstanceAction extends SimpleViewAction<ProcessInstanceDetailsForm>
    {
        private String _navLabel = "Workflow process instance details";

        @Override
        public void validate(ProcessInstanceDetailsForm form, BindException errors)
        {
            if (form.getProcessInstanceId() == null)
                errors.rejectValue("processInstanceId", ERROR_MSG, PROCESS_INSTANCE_ID_MISSING);
        }

        @Override
        public ModelAndView getView(ProcessInstanceDetailsForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            WorkflowProcessImpl bean = new WorkflowProcessImpl(form.getProcessInstanceId());
            if (form.getProcessDefinitionKey() != null && bean.getProcessDefinitionKey() == null)
            {
                bean.setProcessDefinitionKey(form.getProcessDefinitionKey());
            }
            if (bean.getProcessDefinitionName() != null)
                _navLabel = "'" + bean.getProcessDefinitionName() + "' " + (bean.isActive() ? "active" : "inactive") + " process instance details";

            return new JspView<>("/org/labkey/workflow/view/workflowProcessInstance.jsp", bean, errors);
        }

        @Override
        public void addNavTrail(NavTree root)
        {
            root.addChild(_navLabel);
        }
    }


    private static class ProcessInstanceDetailsForm
    {
        private String _processInstanceId;
        private String _processDefinitionKey;
        private boolean _includeCompletedTasks;

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
        }

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }

        public boolean includeCompletedTasks()
        {
            return _includeCompletedTasks;
        }

        public void setIncludeCompletedTasks(boolean includeCompletedTasks)
        {
            _includeCompletedTasks = includeCompletedTasks;
        }
    }

    /**
     * Retrieves the process diagram associated with a particular processInstance or processName as a png.
     * If there is no such diagram, a plain text response will be returned indicating that there is no such image.
     */
    @RequiresPermission(ReadPermission.class)
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
                stream = new ByteArrayInputStream("Unable to retrieve process diagram.  Perhaps you need to deploy the process.".getBytes("UTF-8"));
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
            HttpServletRequest request = getViewContext().getRequest();
            if (request.getParameter("processInstanceId") == null && request.getParameter("processDefinitionKey") == null)
                errors.reject(ERROR_MSG, "Either a process instance id or a process definition key must be provided");
        }
    }


    /**
     * Shows the data about a process instance if the user has permissions to see this task
     */
    @RequiresPermission(ReadPermission.class)
    public class ProcessInstanceDataAction extends ReadOnlyApiAction<ProcessInstanceDetailsForm>
    {
        private WorkflowProcess _processInstance;


        @Override
        public void validateForm(ProcessInstanceDetailsForm form, Errors errors)
        {
            if (form.getProcessInstanceId() == null)
                errors.rejectValue("processInstanceId", ERROR_MSG, PROCESS_INSTANCE_ID_MISSING);
            else
            {
                HistoricProcessInstance historicProcessInstance = WorkflowManager.get().getHistoricProcessInstance(form.getProcessInstanceId());
                if (historicProcessInstance == null)
                {
                    errors.reject(ERROR_MSG, NO_SUCH_INSTANCE_ERROR);
                }
                else
                {
                    _processInstance = new WorkflowProcessImpl(historicProcessInstance, form.includeCompletedTasks());
                    if (!_processInstance.canView(getUser(), getContainer()))
                        errors.reject(ERROR_MSG, "User does not have permission to view process instance data for this process");
                }
            }
        }

        @Override
        public SimpleResponse execute(ProcessInstanceDetailsForm processInstanceDetailsForm, BindException errors) throws Exception
        {
            ensureProcessUserAccessData(_processInstance, getUser(), getContainer());
            return success(_processInstance);
        }
    }

    private void ensureProcessUserAccessData(WorkflowProcess process, User user, Container container)
    {
        // remove the data access parameters if the user does not have permission to access the data or if it is inactive
        if (!process.isActive() || !process.canAccessData(user, container))
        {
            Map<String, Object> variables = process.getProcessVariables();
            variables.remove(WorkflowProcess.DATA_ACCESS_KEY);
        }
    }


    /**
     * Shows the data about the list of process instances the user has permissions to see for a given process definition
     */
    @RequiresPermission(ReadPermission.class)
    public class ProcessInstanceListDataAction extends ReadOnlyApiAction<WorkflowRequestForm>
    {
        @Override
        public void validateForm(WorkflowRequestForm form, Errors errors)
        {
            String errorMessage = validateProcessDefinitionKey(form);
            if (errorMessage != null)
                errors.rejectValue("processDefinitionKey", ERROR_MSG, errorMessage);
        }

        @Override
        public ApiResponse execute(WorkflowRequestForm form, BindException errors) throws Exception
        {
            List<WorkflowProcess> workflowProcessList = new ArrayList<>();

            // use the historical process instance query so we get all process instances (active and inactive)
            List<HistoricProcessInstance> processInstanceList = WorkflowManager.get().getHistoricProcessInstanceList(form.getProcessDefinitionKey(), getContainer(), false);
            for (HistoricProcessInstance historicProcessInstance : processInstanceList)
            {
                WorkflowProcess workflowProcess = new WorkflowProcessImpl(historicProcessInstance, form.includeCompletedTasks());
                if (workflowProcess.canView(getUser(), getContainer()))
                {
                    ensureProcessUserAccessData(workflowProcess, getUser(), getContainer());
                    workflowProcessList.add(workflowProcess);
                }
            }

            ApiSimpleResponse resp = new ApiSimpleResponse();
            resp.put("processes", workflowProcessList);
            resp.put("success", true);
            return resp;

        }
    }


    /**
     * Shows the data about a task if the user has permissions to see this task
     */
    @RequiresPermission(ReadPermission.class)
    public class TaskDataAction extends ReadOnlyApiAction<WorkflowTaskForm>
    {
        private WorkflowTask _task;

        @Override
        public Object execute(WorkflowTaskForm workflowTaskForm, BindException errors) throws Exception
        {
            // remove the data access parameters if the user does not have permission to data access the data
            if (!_task.canAccessData(getUser(), getContainer()))
            {
                Map<String, Object> variables = _task.getProcessVariables();
                variables.remove(WorkflowProcess.DATA_ACCESS_KEY);
            }
            return success(_task);
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            if (form.getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
            else
            {
                _task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
                if (!_task.canView(getUser(), getContainer()))
                    errors.reject(ERROR_MSG, "User does not have permission to view task data for this task");
            }
        }
    }

    @RequiresPermission(ReadPermission.class)
    public class GetReassignPermissionNamesAction extends ReadOnlyApiAction<WorkflowTaskForm>
    {
        private WorkflowTask _task;


        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            Set<Class<? extends Permission>> permissionClasses = _task.getReassignPermissions(getUser(), getContainer());
            List<Map<String, String>> names = new ArrayList<>();
            for (Class<? extends Permission> permissionClass : permissionClasses)
            {
                Map<String, String> permData = new HashMap<>();
                Permission permission = (Permission) newInstance(permissionClass);
                permData.put("name", permission.getName());
                names.add(permData);
            }
            response.put("permissions", names);

            return success(response);
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            if (form.getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
            else
            {
                _task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
                if (!_task.isActive())
                    errors.reject(ERROR_MSG, NO_SUCH_TASK_ERROR);
            }
        }
    }


    @RequiresPermission(ReadPermission.class)
    public class CandidateUsersAction extends ReadOnlyApiAction<WorkflowTaskForm>
    {
        protected static final String PROP_USER_ID = "userId";
        protected static final String PROP_USER_NAME = "displayName";

        private WorkflowTask _task;


        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            ApiSimpleResponse response = new ApiSimpleResponse();
            User currentUser = getUser();
            boolean includeEmail = SecurityManager.canSeeUserDetails(getContainer(), currentUser);
            List<User> users = SecurityManager.getUsersWithOneOf(getContainer(), _task.getReassignPermissions(getUser(), getContainer()));
            List<Map<String, Object>> userResponseList = new ArrayList<>();
            for (User user : users)
            {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put(PROP_USER_ID, user.getUserId());

                //force sanitize of the display name, even for logged-in users
                userInfo.put(PROP_USER_NAME, user.getDisplayName(currentUser));
                //include email address, if user is allowed to see them
                if (includeEmail)
                    userInfo.put("email", user.getEmail());
                userResponseList.add(userInfo);

            }
            response.put("users", userResponseList);
            return success(response);
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            if (form.getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
            else
            {
                _task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
                if (!_task.isActive())
                    errors.reject(ERROR_MSG, NO_SUCH_TASK_ERROR);
            }
        }
    }

    /**
     * Claims a task for a user, making that user the owner and the assignee.
     */
    @RequiresPermission(UpdatePermission.class)
    public class ClaimTaskAction extends MutatingApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            WorkflowManager.get().claimTask(form.getTaskId(), form.getAssigneeId(), getContainer());
            return success();
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            form.validate(errors);
        }
    }

    /**
     * Delegates a task to a particular user.  The owner of the task remains unchanged.
     */
    @RequiresPermission(UpdatePermission.class)
    public class DelegateTaskAction extends MutatingApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            WorkflowManager.get().delegateTask(form.getTaskId(), getUser(), form.getAssigneeId(), getContainer());
            return success();
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            form.validate(errors);
        }
    }


    public static class WorkflowRequestForm
    {
        private String _processDefinitionKey;
        private String _processDefinitionName;
        private boolean _includeCompletedTasks;

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }

        public String getProcessDefinitionName()
        {
            return _processDefinitionName;
        }

        public void setProcessDefinitionName(String processDefinitionName)
        {
            _processDefinitionName = processDefinitionName;
        }

        public boolean includeCompletedTasks()
        {
            return _includeCompletedTasks;
        }

        public void setIncludeCompletedTasks(boolean includeCompletedTasks)
        {
            _includeCompletedTasks = includeCompletedTasks;
        }
    }

    /**
     * Assigns a task to a user
     */
    @RequiresPermission(UpdatePermission.class)
    public class AssignTaskAction extends MutatingApiAction<WorkflowTaskForm>
    {
        @Override
        public Object execute(WorkflowTaskForm form, BindException errors) throws Exception
        {
            if (form.getProcessVariables() != null)
            {
                WorkflowManager.get().updateProcessVariables(form.getTaskId(), form.getProcessVariables());
            }
            WorkflowManager.get().assignTask(form.getTaskId(), form.getAssigneeId(), getUser(), getContainer());
            return success();
        }

        @Override
        public void validateForm(WorkflowTaskForm form, Errors errors)
        {
            form.validate(errors);
        }
    }

    private static class WorkflowTaskForm
    {
        private String _taskId;
        private Integer _ownerId;
        private Integer _assigneeId;
        private Map<String, Object> _processVariables;

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public Integer getAssigneeId()
        {
            return _assigneeId;
        }

        public void setAssigneeId(int assigneeId)
        {
            _assigneeId = assigneeId;
        }

        public Integer getOwnerId()
        {
            return _ownerId;
        }

        public void setOwnerId(int ownerId)
        {
            _ownerId = ownerId;
        }

        public Map<String, Object> getProcessVariables()
        {
            return _processVariables;
        }

        public void setProcessVariables(Map<String, Object> processVariables)
        {
            _processVariables = processVariables;
        }

        public void validate(Errors errors)
        {
            if (getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
            if (getAssigneeId() == null)
                errors.rejectValue("assigneeId", ERROR_MSG, ASSIGNEE_ID_MISSING);

        }
    }


    /**
     * Creates a new instance of a process with a given processKey and returns the id of the new instance on success.
     */
    @RequiresPermission(UpdatePermission.class)
    public class StartProcessAction extends MutatingApiAction<StartWorkflowProcessForm>
    {
        @Override
        public Object execute(StartWorkflowProcessForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            form.setInitiatorId(getUser().getUserId());
            form.setContainerId(getContainer().getId());
            String instanceId = WorkflowManager.get().startWorkflow(form.getWorkflowModelModule(), form.getProcessDefinitionKey(), form.getName(), form.getProcessVariables(), getContainer());
            ApiSimpleResponse response = new ApiSimpleResponse();
            response.put("processInstanceId", instanceId);
            return success(response);
        }

        @Override
        public void validateForm(StartWorkflowProcessForm form, Errors errors)
        {
            if (form != null)
                form.validate(getUser(), getContainer(), errors);
        }
    }

    public static class StartWorkflowProcessForm
    {
        private String _workflowModelModule;
        private String _processDefinitionKey;
        private String _name;
        private Map<String, Object> _processVariables;

        public String getName()
        {
            return _name;
        }

        public void setName(String name)
        {
            _name = name;
        }

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }

        public String getWorkflowModelModule()
        {
            return _workflowModelModule;
        }

        public void setWorkflowModelModule(String module)
        {
            _workflowModelModule = module;
        }

        public void setInitiatorId(int userId)
        {
            if (_processVariables == null)
            {
                _processVariables = new HashMap<>();
            }
            _processVariables.put(WorkflowProcess.INITIATOR_ID, String.valueOf(userId));
        }

        public void setContainerId(String containerId)
        {
            if (_processVariables == null)
            {
                _processVariables = new HashMap<>();
            }
            _processVariables.put(WorkflowProcess.CONTAINER_ID, containerId);
        }

        public Map<String, Object> getProcessVariables()
        {
            return _processVariables;
        }

        public void setProcessVariables(Map<String, Object> processVariables)
        {
            _processVariables = processVariables;
        }

        public void validate(User user, Container container, Errors errors)
        {
            if (getProcessDefinitionKey() == null)
                errors.rejectValue("processDefinitionKey", ERROR_MSG, PROCESS_DEFINITION_KEY_MISSING);
            else if (getWorkflowModelModule() == null)
                errors.rejectValue("workflowModelModule", ERROR_MSG, MODULE_NAME_MISSING);
            else
            {
                PermissionsHandler handler = WorkflowRegistry.get().getPermissionsHandler(getWorkflowModelModule(), user, container);
                if (!handler.canStartProcess(getProcessDefinitionKey()))
                    throw new UnauthorizedException("User does not have permission to start a process with key " + getProcessDefinitionKey() + " from module " + getWorkflowModelModule());
            }
        }
    }

    /**
     * Deletes a particular process instance.  This is allowed for the initiator of the
     * process and for administrators.
     */
    @RequiresPermission(UpdatePermission.class)
    public class RemoveProcessInstanceAction extends MutatingApiAction<RemoveWorkflowProcessForm>
    {
        @Override
        public Object execute(RemoveWorkflowProcessForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            String removalMsg = "Removed by user " + getUser() + " on " + (new Date()) + ".  ";
            if (form.getComment() != null)
                removalMsg += "Reason: " + form.getComment();
            form.setComment(removalMsg);
            WorkflowProcess process = new WorkflowProcessImpl(WorkflowManager.get().getProcessInstance(form.getProcessInstanceId()));
            if (!process.canDelete(getUser(), getContainer()))
            {
                throw new UnauthorizedException("You do not have permission to delete this process instance");
            }
            WorkflowManager.get().deleteProcessInstance(form.getProcessInstanceId(), form.getComment());
            return success();
        }

        @Override
        public void validateForm(RemoveWorkflowProcessForm form, Errors errors)
        {
            form.validate(errors);
        }
    }

    public static class RemoveWorkflowProcessForm
    {
        private String _processInstanceId;
        private String _comment;

        public String getProcessInstanceId()
        {
            return _processInstanceId;
        }

        public void setProcessInstanceId(String processInstanceId)
        {
            _processInstanceId = processInstanceId;
        }

        public String getComment()
        {
            return _comment;
        }

        public void setComment(String comment)
        {
            _comment = comment;
        }

        public void validate(Errors errors)
        {
            if (getProcessInstanceId() == null)
                errors.rejectValue("processInstanceId", ERROR_MSG, PROCESS_INSTANCE_ID_MISSING);
        }
    }

    @RequiresPermission(UpdatePermission.class)
    public class UpdateVariablesAction extends MutatingApiAction<ProcessVariablesForm>
    {
        private WorkflowTask _task;

        @Override
        public Object execute(ProcessVariablesForm form, BindException errors) throws Exception
        {
            _task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
            if (_task.canUpdate(getUser(), getContainer()))
            {
                WorkflowManager.get().updateProcessVariables(form.getTaskId(), form.getProcessVariables());
            }
            ApiSimpleResponse response = new ApiSimpleResponse();
            return success();
        }

        @Override
        public void validateForm(ProcessVariablesForm form, Errors errors)
        {
            form.validate(errors);
            if (!errors.hasErrors())
            {
                _task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
                if (_task == null || !_task.isActive())
                    errors.reject(ERROR_MSG, NO_SUCH_TASK_ERROR);
                if (!_task.canUpdate(getUser(), getContainer()))
                    throw new UnauthorizedException("User does not have permission to update task " + form.getTaskId());

            }
        }
    }

    public static class ProcessVariablesForm
    {
        private String _taskId;
        private Map<String, Object> _processVariables;

        public String getTaskId()
        {
            return _taskId;
        }

        public void setTaskId(String taskId)
        {
            _taskId = taskId;
        }

        public Map<String, Object> getProcessVariables()
        {
            return _processVariables;
        }

        public void setProcessVariables(Map<String, Object> processVariables)
        {
            _processVariables = processVariables;
        }

        public void validate(Errors errors)
        {
            if (getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
        }
    }

    /**
     * Complete a task in a workflow.  If the task is currently unassigned, it will be assigned to the current user.
     */
    @RequiresPermission(UpdatePermission.class)
    public class CompleteTaskAction extends MutatingApiAction<TaskCompletionForm>
    {
        @Override
        public Object execute(TaskCompletionForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            ApiSimpleResponse response = new ApiSimpleResponse();

            WorkflowTask task = WorkflowManager.get().getTask(form.getTaskId(), getContainer());
            if (!task.isActive())
                throw new Exception("No active task with id " + form.getTaskId());
            if (!task.canComplete(getUser(), getContainer()))
            {
                throw new Exception("User " + getUser() + " does not have permission to complete this task (id: " + form.getTaskId() + ")");
            }
            if (!task.isAssigned(getUser())) // if user can complete it but it is not assigned to this user, we should change that.
            {
                WorkflowManager.get().assignTask(form.getTaskId(), getUser().getUserId(), getUser(), getContainer());
                task.setAssignee(getUser());
            }
            WorkflowManager.get().updateProcessVariables(form.getTaskId(), form.getProcessVariables());
            WorkflowManager.get().completeTask(form.getTaskId(), getUser(), getContainer());
            response.put("status", "success");

            return response;
        }

        @Override
        public void validateForm(TaskCompletionForm form, Errors errors)
        {
            if (form.getTaskId() == null)
                errors.rejectValue("taskId", ERROR_MSG, TASK_ID_MISSING);
        }
    }

    public static class TaskCompletionForm
    {
        private String _taskId;
        private String _processInstanceId;
        private String _processDefinitionKey;
        private Map<String, Object> _processVariables;

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

        public String getProcessDefinitionKey()
        {
            return _processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey)
        {
            _processDefinitionKey = processDefinitionKey;
        }
    }

    /**
     * Creates a new deployment of a process definition
     */
    @RequiresPermission(AdminPermission.class)
    public class DeployAction extends MutatingApiAction<DeploymentForm>
    {
        @Override
        public Object execute(DeploymentForm form, BindException errors) throws Exception
        {
            if (errors.hasErrors())
                return new SimpleErrorView(errors);

            ApiSimpleResponse response = new ApiSimpleResponse();
            if (form.getFile() != null)
            {
                File modelFile = new File(form.getFile());
                response.put("deploymentId", WorkflowManager.get().deployWorkflow(form.getModuleName(), modelFile, getContainer()));
            }
            else
            {
                throw new Exception("No process specified for deployment");
            }
            return success(response);
        }

        @Override
        public void validateForm(DeploymentForm form, Errors errors)
        {
            if (form.getFile() == null)
                errors.rejectValue("file", ERROR_MSG, "File name is required");
        }
    }

    public static class DeploymentForm
    {
        private String _file;
        private String _moduleName;

        public String getFile()
        {
            return _file;
        }

        public void setFile(String file)
        {
            _file = file;
        }

        public String getModuleName()
        {
            return _moduleName;
        }

        public void setModuleName(String moduleName)
        {
            _moduleName = moduleName;
        }
    }

}