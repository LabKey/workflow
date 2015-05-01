package org.labkey.workflow.view;

import org.labkey.api.security.User;

import java.util.List;

/**
 * Created by susanh on 4/30/15.
 */
public class ExportRequestDetailsBean
{
    private String _requestId;
    private User _user;
    private Integer _dataSetId;
    private String _reason;
    private List<String> _currentTasks;
    private boolean _isReviewer;

    public void setCurrentTasks(List<String> currentTasks)
    {
        _currentTasks = currentTasks;
    }

    public boolean isReviewer()
    {
        return _isReviewer;
    }

    public void setIsReviewer(boolean isReviewer)
    {
        _isReviewer = isReviewer;
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

    public String getRequestId()
    {
        return _requestId;
    }

    public void setRequestId(String requestId)
    {
        _requestId = requestId;
    }
}
