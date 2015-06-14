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
     *      * For a String, use "textArea" to return the dimensions of the text area for this string's input.  This will
     *        be a Pair<Integer, Integer> with the first element the number of rows and the second the number of columns.
     *        If the string's field does not require a textarea, this calls will return null;
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
