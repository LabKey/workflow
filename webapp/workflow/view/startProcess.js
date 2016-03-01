/*
 * Copyright (c) 2015-2016 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0
 */
/**
 * Created by susanh on 6/14/15.
 */

function startWorkflowProcess(formName, fields, processDefinitionKey)
{
    var form = document.forms[formName];
    var parameters = {};
    for (i = 0, len = fields.length; i < len; i++)
    {
        parameters[fields[i]] = form[fields[i]].value;
    }
    var returnURLParams = {
        processDefinitionKey: processDefinitionKey
    };
    Ext4.Ajax.request({
        url: LABKEY.ActionURL.buildURL('workflow', 'startProcess.api'),
        method: 'POST',
        jsonData: {
            processDefinitionKey: processDefinitionKey,
            workflowModelModule : parameters.workflowModelModule,
            processVariables: parameters
        },
        returnUrl: LABKEY.ActionURL.buildURL('workflow', 'summary', null, returnURLParams),
        scope: this,
        success: function(response) {
            window.location = LABKEY.ActionURL.buildURL('workflow', 'summary', null, returnURLParams);
        },
        failure: function(response)
        {
            var jsonResp = LABKEY.Utils.decode(response.responseText);
            if (jsonResp && jsonResp.errors)
            {
                var errorHTML = jsonResp.errors[0].message;
                Ext4.Msg.alert('Error', errorHTML);
            }
        }
    });
}