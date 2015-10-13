/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
package org.labkey.workflow.model;


import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;
import org.labkey.api.workflow.TaskFormField;

/**
 * Created by susanh on 5/25/15.
 */
public class TaskFormFieldImpl implements TaskFormField
{
    private FormProperty _engineFormProperty;

    public TaskFormFieldImpl(FormProperty engineFormProperty)
    {
        _engineFormProperty = engineFormProperty;
    }

    public String getId()
    {
        return _engineFormProperty.getId();
    }

    public String getName()
    {
        return _engineFormProperty.getName();
    }

    public String getType()
    {
        return _engineFormProperty.getType().getName();
    }

    public Object getInformation(String key) { return _engineFormProperty.getType().getInformation(key); }

    public String getValue()
    {
        return _engineFormProperty.getValue();
    }

    public boolean isReadable()
    {
        return _engineFormProperty.isReadable();
    }

    public boolean isWritable()
    {
        return _engineFormProperty.isWritable();
    }

    public boolean isRequired()
    {
        return _engineFormProperty.isRequired();
    }
}
