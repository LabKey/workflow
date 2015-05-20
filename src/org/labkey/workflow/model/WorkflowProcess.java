package org.labkey.workflow.model;

import org.activiti.engine.runtime.ProcessInstance;
import org.labkey.api.action.HasViewContext;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.view.ViewContext;
import org.labkey.workflow.WorkflowManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/3/15.
 */
public class WorkflowProcess implements HasViewContext
{
    private ProcessInstance _engineProcessInstance;

    public static final String INITIATOR_ID = "initiatorId";
    public static final String CONTAINER_ID = "container";
    public static final String CREATED_DATE = "created";

    private String _processDefinitionKey;
    private String _id;
    private Map<String, Object> _processVariables = new HashMap<>();
    private Integer _initiatorId;
    private String _processInstanceId;
    private ViewContext _viewContext;
    private String _name; // the name for this process instance
    private List<WorkflowTask> _currentTasks;

    public WorkflowProcess()
    {
    }

    public WorkflowProcess(String id, User user, Container container) throws Exception
    {
        this(WorkflowManager.get().getProcessInstance(id), user, container);
    }

    public WorkflowProcess(ProcessInstance engineProcessInstance, User user, Container container) throws Exception
    {
        _engineProcessInstance = engineProcessInstance;
        _processVariables = WorkflowManager.get().getProcessInstanceVariables(engineProcessInstance.getProcessInstanceId());
        if (_processVariables.get(INITIATOR_ID) != null)
            this.setInitiatorId(Integer.valueOf((String) _processVariables.get(INITIATOR_ID)));
        this.setCurrentTasks(WorkflowManager.get().getCurrentProcessTasks(engineProcessInstance.getProcessInstanceId(), user, container));
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

    public void setProcessDefintionKey(String processKey)
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
}
