/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
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