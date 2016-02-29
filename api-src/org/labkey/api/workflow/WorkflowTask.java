/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
 */
package org.labkey.api.workflow;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.labkey.api.data.Container;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.Permission;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Represents a single task within a workflow instance.
 *
 * Created by susanh on 6/14/15.
 */
public interface WorkflowTask
{
    String getId();

    String getName();

    String getDescription();

    String getProcessDefinitionKey(Container container);

    String getProcessDefinitionName(Container container);

    @Nullable
    @JsonIgnore
    User getOwner();

    @Nullable
    Integer getOwnerId();

    @Nullable
    @JsonIgnore
    User getAssignee();

    @Nullable
    Integer getAssigneeId();

    void assign(@NotNull Integer assigneeId, User user, Container container) throws Exception;

    String getProcessInstanceId();

    String getProcessDefinitionId();

    Date getCreateTime();

    String getTaskDefinitionKey();

    Date getDueDate();

    Date getEndDate();

    String getParentTaskId();

    Map<String, Object> getTaskLocalVariables();

    Map<String, Object> getProcessVariables();

    String getContainer();

    List<Integer> getGroupIds();

    boolean isInCandidateGroups(User user);

    boolean hasCandidateGroups();

    boolean canClaim(User user, Container container);

    boolean canDelegate(User user, Container container);

    boolean canAssign(User user, Container container);

    boolean canView(User user, Container container);

    boolean canAccessData(User user, Container container);

    boolean canComplete(User user, Container container);

    boolean canUpdate(User user, Container container);

    void setName(String name);

    void setDescription(String description);

    /**
     * Set the user responsible for full completion of the task, which may include review of results
     * if the task has been delegated
     * @param owner the owner for this task
     */
    void setOwner(User owner);

    /**
     * Set the user who is currently assigned to work on this task
     * @param assignee the user to assign this task to
     */
    void setAssignee(User assignee);

    boolean isAssigned(User user);

    boolean isDelegated();

    boolean isReadyForReview();

    void setDueDate(Date dueDate);

    void delegate(User user);

    boolean isSuspended();

    Set<Class<? extends Permission>> getReassignPermissions(User user, Container container);

    boolean isActive();

    @NotNull
    Map<String, TaskFormField> getFormFields();

    @Nullable
    Map<String, Object> getVariables();

}
