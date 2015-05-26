package org.labkey.workflow.model;


import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.FormType;

/**
 * Created by susanh on 5/25/15.
 */
public class TaskFormField
{
    private FormProperty _engineFormProperty;


    public TaskFormField(FormProperty engineFormProperty)
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

    public FormType getType()
    {
        return _engineFormProperty.getType();
    }

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
