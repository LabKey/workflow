package org.labkey.workflow.parse;

import org.activiti.bpmn.model.*;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;
import org.activiti.engine.parse.BpmnParseHandler;
import org.apache.log4j.Logger;

import org.labkey.api.security.SecurityManager;
import org.labkey.api.security.User;
import org.labkey.api.security.UserManager;
import org.labkey.api.security.ValidEmail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Created by susanh on 5/4/15.
 */
public class CandidateAssigneeParseHandler implements BpmnParseHandler
{
    private static Logger logger = Logger.getLogger(CandidateAssigneeParseHandler.class);


    @Override
    public Collection<Class<? extends BaseElement>> getHandledTypes()
    {
        ArrayList<Class<? extends BaseElement>> types = new ArrayList<>();
        types.add(UserTask.class);
        return types;
    }

    @Override
    public void parse(BpmnParse bpmnParse, BaseElement element)
    {
        UserTask userTask = (UserTask) element;
        userTask.setCandidateGroups(getGroupIds(userTask));
        userTask.setCandidateUsers(getUserIds(userTask));
    }

    protected List<String> getGroupIds(UserTask userTask)
    {
        List<String> groupIds = new ArrayList<>();
        for (String group : userTask.getCandidateGroups())
        {
            Integer groupId = SecurityManager.getGroupId(null, group, false);
            if (groupId != null)
            {
                groupIds.add(String.valueOf(groupId));
            }
            else
            {
                groupIds.add(group);
                logger.warn("No site-wide group found for group '" + group + "'");
            }
        }
        return groupIds;
    }

    protected List<String> getUserIds(UserTask userTask)
    {
        List<String> userIds = new ArrayList<String>();
        for (String email : userTask.getCandidateUsers())
        {
            User user = null;
            try
            {
                user = UserManager.getUser(new ValidEmail(email));
            }
            catch (ValidEmail.InvalidEmailException e)
            {
                logger.warn("Removing invalid email address for user: '" + email);
            }
            if (user == null)
            {
                logger.warn("No such user: '" + email + "'; removing");
            }
            else
            {
                userIds.add(String.valueOf(user.getUserId()));
            }
        }
        return userIds;
    }
}
