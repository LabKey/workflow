/*
 * Copyright (c) 2015-2016 LabKey Corporation
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
package org.labkey.workflow.model;


import org.activiti.engine.form.FormProperty;
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

    @Override
    public String getId()
    {
        return _engineFormProperty.getId();
    }

    @Override
    public String getName()
    {
        return _engineFormProperty.getName();
    }

    @Override
    public String getType()
    {
        return _engineFormProperty.getType().getName();
    }

    @Override
    public Object getInformation(String key) { return _engineFormProperty.getType().getInformation(key); }

    @Override
    public String getValue()
    {
        return _engineFormProperty.getValue();
    }

    @Override
    public boolean isReadable()
    {
        return _engineFormProperty.isReadable();
    }

    @Override
    public boolean isWritable()
    {
        return _engineFormProperty.isWritable();
    }

    @Override
    public boolean isRequired()
    {
        return _engineFormProperty.isRequired();
    }
}
