package org.labkey.workflow;

import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.UserQueryImpl;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.persistence.entity.IdentityInfoEntity;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;

import java.util.List;
import java.util.Map;

/**
 * Created by susanh on 5/1/15.
 *
 * Could be made as a delegating class to the LabKey UserManager class if necessary or convenient.
 * Currently not used.
 */
public class WorkflowUserManager implements UserIdentityManager, Session
{
    @Override
    public User createNewUser(String userId)
    {
        return null;
    }

    @Override
    public void insertUser(User user)
    {

    }

    @Override
    public void updateUser(User updatedUser)
    {

    }

    @Override
    public User findUserById(String userId)
    {
        return null;
    }

    @Override
    public void deleteUser(String userId)
    {

    }

    @Override
    public List<User> findUserByQueryCriteria(UserQueryImpl query, Page page)
    {
        return null;
    }

    @Override
    public long findUserCountByQueryCriteria(UserQueryImpl query)
    {
        return 0;
    }

    @Override
    public List<Group> findGroupsByUser(String userId)
    {
        return null;
    }

    @Override
    public UserQuery createNewUserQuery()
    {
        return null;
    }

    @Override
    public IdentityInfoEntity findUserInfoByUserIdAndKey(String userId, String key)
    {
        return null;
    }

    @Override
    public List<String> findUserInfoKeysByUserIdAndType(String userId, String type)
    {
        return null;
    }

    @Override
    public Boolean checkPassword(String userId, String password)
    {
        return null;
    }

    @Override
    public List<User> findPotentialStarterUsers(String proceDefId)
    {
        return null;
    }

    @Override
    public List<User> findUsersByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults)
    {
        return null;
    }

    @Override
    public long findUserCountByNativeQuery(Map<String, Object> parameterMap)
    {
        return 0;
    }

    @Override
    public boolean isNewUser(User user)
    {
        return false;
    }

    @Override
    public Picture getUserPicture(String userId)
    {
        return null;
    }

    @Override
    public void setUserPicture(String userId, Picture picture)
    {

    }

    @Override
    public void flush()
    {

    }

    @Override
    public void close()
    {

    }
}
