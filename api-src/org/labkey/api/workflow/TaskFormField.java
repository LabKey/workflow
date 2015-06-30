/*
 * Copyright (c) 2015 LabKey Corporation
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
package org.labkey.api.workflow;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a form field as defined in a BPMN 2.0 XML file with the Activiti extensions
 *
 * Created by susanh on 6/14/15.
 */
public interface TaskFormField
{

    /**
     * @return id of this form field
     */
    String getId();

    /**
     *
     * @return name of this form field
     */
    String getName();

    /**
     * @return the type of field (one of "string", "enum", "Date", "long", "boolean")
     */
    String getType();

    /**
     * @return the extra information used in defining this form field.
     *      * For an enum, use "value" as the key to return a Map<String, String> of the choices, mapping from the choice key to the display string
     *      * For a date, use "datePattern" as the key to return the date display format
     */
    @Nullable
    Object getInformation(String key);

    /**
     * @return the value for the form field
     */
    String getValue();

    /**
     * @return indication of whether the field is readable or not
     */
    boolean isReadable();

    /**
     * @return indication of whether the field is writable or not
     */
    boolean isWritable();

    /**
     * @return indication of whether the field is required or not
     */
    boolean isRequired();
}
