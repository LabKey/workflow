/**
 * Created by susanh on 5/20/15.
 */
Ext4.define("Workflow.view.dialog.CompleteTask", {
    extend: 'Ext.window.Window',
    title: 'Complete Task',
    modal: true,
    completeTaskEvent: "submitclick",

    constructor : function(config) {
        this.callParent([config]);
        this.addEvents([this.completeTaskEvent]);
        this.taskId = config['taskId'];
        this.name = config['name'];
    },

    initComponent : function()
    {
        this.border = false;

        this.items = [
            {
                itemId: 'taskDescription',
                xtype: 'box',
                width: 400,
                padding: '15px 15px',
                html: this.name
            },
            {
                xtype : 'textareafield',
                width : 400,
                height : 50,
                itemId : 'TaskComment',
                fieldLabel : 'Comment',
                labelWidth : 60,
                listeners : {
                    scope: this,
                    change: function (cmp, newValue)
                    {
                        var trimmedVal = newValue ? newValue.trim() : "";
                        this.down('button#SubmitButton').setDisabled(trimmedVal.length == 0);
                    }
                }
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
            text: 'Submit',
            itemId: 'SubmitButton',
            disabled: false,
            scope: this,
            handler : function() {
                this.fireEvent(this.completeTaskEvent, this.taskId, this.parameters);
                this.close();
            }
        }
        ]

        this.callParent();

        this.on(this.completeTaskEvent, this.makeTaskCompletionRequest, this);
    },

    makeTaskCompletionRequest: function(taskId, parameters)
    {
        parameters.comment = this.down('textfield#TaskComment').getValue();
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('workflow', 'completeTask'),
            method: 'POST',
            params: {
                taskId: taskId,
                processVariables: parameters,
                returnUrl: window.location
            },
            scope: this,
            success: function(response) {
                window.location = window.location; // avoid form resubmit
            },
            failure: this.taskActionFailure
        });
    },

    taskActionFailure : function(response){
        var jsonResp = LABKEY.Utils.decode(response.responseText);
        if (jsonResp && jsonResp.errors)
        {
            var errorHTML = jsonResp.errors[0].message;
            Ext4.Msg.alert('Error', errorHTML);
        }
    }

});

function getData(url, parameters) {
    var newForm = document.createElement('form');
    document.body.appendChild(newForm);
    Ext4.Ajax.request({
        url: url,
        method: 'POST',
        form: newForm,
        isUpload: true,
        params: parameters,
        success: function (response)
        {
            console.log('Successful retrieval of data');
        },
        failure: function(response)
        {
            var jsonResp = LABKEY.Utils.decode(response.responseText);
            if (jsonResp && jsonResp.errors)
            {
                var errorHTML = jsonResp.errors[0].message;
                Ext4.Msg.alert('Error', errorHTML);
            }
        },
        scope: this
    });
}

function createCompleteTaskWindow(taskId, name, parameters) {
    Ext4.create("Workflow.view.dialog.CompleteTask", {
        taskId: taskId,
        name: name,
        parameters: parameters
    }).show();
}