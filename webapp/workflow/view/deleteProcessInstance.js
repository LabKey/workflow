/**
 * Created by susanh on 5/29/15.
 */
Ext4.define("Workflow.view.dialog.DeleteProcessInstance", {
    extend: 'Ext.window.Window',
    title: 'Remove Process Instance',
    modal: true,
    deleteProcessEvent: "confirmclick",
    processInstanceId: null,
    processDefinitionKey: null,
    name: null,

    constructor : function(config) {
        this.callParent([config]);
        this.addEvents([this.deleteProcessEvent]);
    },

    initComponent : function()
    {
        this.border = false;

        this.items = [
            {
                itemId: 'confirmationMessage',
                xtype: 'box',
                width: 400,
                padding: '15px 15px',
                html: '<strong>Are you sure you want to permanently remove this process instance?</strong><br><br>' + this.name
            },
            {
                xtype : 'textareafield',
                width : 325,
                height : 50,
                padding: '15px 15px',
                itemId : 'DeletionComment',
                fieldLabel : 'Comment (optional)',
                labelWidth : 75
            }
        ]
        this.buttons = [{
            itemId: 'CancelButton',
            disabled: false,
            text: 'Cancel',
            scope: this,
            handler : function() {
                this.close();
            }
        },{
            text: 'Confirm',
            itemId: 'ConfirmButton',
            disabled: false,
            scope: this,
            handler : function() {
                this.fireEvent(this.deleteProcessEvent, this.processInstanceId, this.processDefinitionKey);
                this.close();
            }
        }
        ]

        this.callParent();

        this.on(this.deleteProcessEvent, this.makeDeleteProcessInstanceRequest, this);
    },

    makeDeleteProcessInstanceRequest: function(processInstanceId, processDefinitionKey)
    {
        var parameters = {
            processDefinitionKey: processDefinitionKey
        }
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('workflow', 'removeProcessInstance.api'),
            method: 'POST',
            jsonData: {
                processInstanceId: processInstanceId,
                comment: this.down('textfield#DeletionComment').getValue()
            },
            returnUrl: window.location,
            scope: this,
            success: function(response) {
                window.location = LABKEY.ActionURL.buildURL('workflow', 'instanceList', LABKEY.containerPath, parameters)
            },
            failure: this.actionFailure
        });
    },

    actionFailure : function(response){
        var jsonResp = LABKEY.Utils.decode(response.responseText);
        if (jsonResp && jsonResp.errors)
        {
            var errorHTML = jsonResp.errors[0].message;
            Ext4.Msg.alert('Error', errorHTML);
        }
    }

});

function createDeleteProcessInstanceConfirmationWindow(processInstanceId, processDefinitionKey, name)
{
    Ext4.create("Workflow.view.dialog.DeleteProcessInstance", {
        processInstanceId: processInstanceId,
        processDefinitionKey: processDefinitionKey,
        name: name
    }).show();
}
