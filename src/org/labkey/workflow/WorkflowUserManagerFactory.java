package org.labkey.workflow;

import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;
import org.activiti.engine.impl.persistence.entity.UserIdentityManager;

/**
 * Created by susanh on 5/1/15.
 *
 * Currently not in use, but started to see what is involved in creating our own user and group managers
 * following these two posts:
 *  http://stackoverflow.com/questions/24925051/activiti-engine-intergation-with-custom-user-group-data-table
 *  http://developer4life.blogspot.com/2012/02/activiti-authentication-and-identity.html
 *
 * Since we won't be using activiti to manage users or groups, this is not necessary, as long as we avoid
 * queries that try to access the act_id_* tables.
 */
public class WorkflowUserManagerFactory implements SessionFactory
{
    @Override
    public Class<?> getSessionType()
    {
        return UserIdentityManager.class;
    }

    @Override
    public Session openSession()
    {
        return new WorkflowUserManager();
    }
}
