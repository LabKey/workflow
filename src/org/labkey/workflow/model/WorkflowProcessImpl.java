package org.labkey.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.action.HasViewContext;
import org.labkey.api.action.Marshal;
import org.labkey.api.action.Marshaller;
import org.labkey.api.data.Container;
import org.labkey.api.data.ContainerManager;
import org.labkey.api.exp.Lsid;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.util.DateUtil;
import org.labkey.api.util.StringUtilsLabKey;
import org.labkey.api.view.ViewContext;
import org.labkey.api.workflow.WorkflowProcess;
import org.labkey.api.workflow.PermissionsHandler;
import org.labkey.api.workflow.WorkflowTask;
import org.labkey.workflow.WorkflowManager;
import org.labkey.workflow.WorkflowRegistry;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
@Marshal(Marshaller.Jackson)
public class WorkflowProcessImpl implements WorkflowProcess, HasViewContext
{
    private ProcessInstance _engineProcessInstance;

    private String _processDefinitionKey;
    private String _id = null;
    private Map<String, Object> _processVariables = new HashMap<>();
    private Integer _initiatorId;
    private String _processInstanceId;
    private ViewContext _viewContext;
    private String _name; // the name for this process instance
    private List<WorkflowTask> _currentTasks;
    private Container _container;

    public WorkflowProcessImpl()
    {
    }

    public WorkflowProcessImpl(String id, Container container)
    {
        this(WorkflowManager.get().getProcessInstance(id));
        _id = id;
    }

    public WorkflowProcessImpl(ProcessInstance engineProcessInstance)
    {

        _engineProcessInstance = engineProcessInstance;
        if (_engineProcessInstance != null)
        {
            _processVariables = WorkflowManager.get().getProcessInstanceVariables(engineProcessInstance.getProcessInstanceId());
            if (_processVariables.get(CONTAINER_ID) != null)
                _container = ContainerManager.getForId((String) _processVariables.get(CONTAINER_ID));
            if (_processVariables.get(INITIATOR_ID) != null)
                setInitiatorId(Integer.valueOf((String) _processVariables.get(INITIATOR_ID)));
            setCurrentTasks(WorkflowManager.get().getCurrentProcessTasks(engineProcessInstance.getProcessInstanceId(), _container));
        }
    }

    @JsonIgnore
    public Container getContainer()
    {
        return _container;
    }

    public String getId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getId();
        return _id;
    }

    public void setId(String id)
    {
        _id = id;
    }

    public String getProcessDefinitionKey()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getProcessDefinitionKey();
        return _processDefinitionKey;
    }

    public String getProcessDefinitionName()
    {
        if (getProcessDefinitionKey() == null)
            return null;
        return WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), null).getName();
    }

    public String getProcessDefinitionModule()
    {
        if (_engineProcessInstance == null)
            return null;
        Lsid lsid = new Lsid(WorkflowManager.get().getProcessDefinition(getProcessDefinitionKey(), _container).getCategory());
        return lsid.getObjectId();
    }

    public void setProcessDefinitionKey(String processKey)
    {
        _processDefinitionKey = processKey;
    }


    public Map<String, Object> getProcessVariables()
    {
        return _processVariables;
    }

    public void setProcessVariables(Map<String, Object> processVariables)
    {
        _processVariables = processVariables;
    }


    @Override
    public void setViewContext(ViewContext context)
    {
        _viewContext = context;
    }

    @JsonIgnore
    @Override
    public ViewContext getViewContext()
    {
        return _viewContext;
    }

    public Integer getInitiatorId()
    {
        return _initiatorId;
    }

    public void setInitiatorId(Integer initiatorId)
    {
        _initiatorId = initiatorId;
    }

    public String getProcessInstanceId()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getProcessInstanceId();
        return _processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId)
    {
        _processInstanceId = processInstanceId;
    }

    public String getName()
    {
        if (_engineProcessInstance != null)
            return _engineProcessInstance.getName();
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }

    @JsonIgnore
    public User getInitiator()
    {
        if (getInitiatorId() != null)
        {
            return UserManager.getUser(getInitiatorId());
        }
        return null;
    }

    public List<WorkflowTask> getCurrentTasks()
    {
        return _currentTasks;
    }

    public void setCurrentTasks(List<WorkflowTask> currentTasks)
    {
        _currentTasks = currentTasks;
    }

    private PermissionsHandler getPermissionsHandler()
    {
        return WorkflowRegistry.get().getPermissionsHandler(getProcessDefinitionModule());
    }

    public boolean canAccessData(User user, Container container)
    {
        return getPermissionsHandler().canAccessData(this, user, container);
    }

    public boolean canView(User user, Container container)
    {
        return getPermissionsHandler().canView(this, user, container);
    }

    public boolean canDelete(User user, Container container)
    {
        return getPermissionsHandler().canDelete(this, user, container);
    }

    public boolean hasDiagram(Container container)
    {
        if (_engineProcessInstance == null)
            return false;
        ProcessDefinition definition = WorkflowManager.get().getProcessDefinition(_engineProcessInstance.getProcessDefinitionKey(), container);
        return definition != null && definition.getDiagramResourceName() != null;
    }

    public boolean isActive()
    {
        return _engineProcessInstance != null;
    }

    @JsonIgnore
    @Nullable
    public static Map<String, Object> getDisplayVariables(Container container, Map<String, Object> variables)
    {
        String displayKey = null;
        Object displayValue = null;
        Map<String, Object> _displayVariables = new HashMap<String, Object>();
        for (String key : variables.keySet())
        {
            if (CONTAINER_ID.equalsIgnoreCase(key) || INITIATOR_ID.equalsIgnoreCase(key))
                continue;
            else if (key.endsWith("GroupId"))
            {
                displayKey = key.substring(0, key.length() - 2);
                try
                {
                    displayValue = org.labkey.api.security.SecurityManager.getGroup(Integer.valueOf((String) variables.get(key)));
                }
                catch (NumberFormatException e)
                {
                    displayValue = variables.get(key);
                }
            }
            else if (variables.get(key) instanceof Date)
            {
                displayKey = key;
                displayValue = DateUtil.formatDateTime(container, (Date) variables.get(key));
            }
            else
            {
                displayKey = key;
                displayValue = variables.get(key);
            }
            displayKey = StringUtilsLabKey.splitCamelCase(StringUtils.capitalize(displayKey));

            _displayVariables.put(displayKey, displayValue);
        }

        return _displayVariables;

    }


}