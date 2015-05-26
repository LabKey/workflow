/**
 * Created by susanh on 5/20/15.
 */
Ext4.define("Workflow.view.dialog.CompleteTask", {
    extend: 'Ext.window.Window',
    title: 'Complete Task',
    modal: true,
    completeTaskEvent: "submitclick",
    taskId: null,
    name: null,
    parameters: null,

    constructor : function(config) {
        this.callParent([config]);
        this.addEvents([this.completeTaskEvent]);
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
                width : 325,
                height : 50,
                padding: '15px 15px',
                itemId : 'TaskComment',
                fieldLabel : 'Comment ',
                labelWidth : 75,
                listeners : {
                    scope: this,
                    change: function (cmp, newValue)
                    {
                        var trimmedVal = newValue ? newValue.trim() : "";
                        this.down('button#SubmitButton').setDisabled(trimmedVal.length == 0);
                    }
                }
            },
            {
                xtype : 'combobox',
                itemId:'decisionCombo',
                width : 250,
                name : 'decision',
                fieldLabel : 'Decision',
                labelWidth : 75,
                labelSeparator : '',
                store : Ext4.create('Ext.data.Store', {
                    fields: ['name', 'description'],
                    data : [
                        {'name':'approve', 'description':'Approve'},
                        {'name':'deny', 'description':'Deny'}
                    ]
                }),
                queryMode : 'local',
                value : {},
                valueField : 'name',
                displayField : 'description',
                forceSelection : true,
                editable : false,
                scope: this,
                listConfig : {
                    getInnerTpl : function() {
                        return '<div class="x-combo-list-item">{description}</div>';
                    }
                },
                listeners : {
                    select : function(combo, records) {
                        this.up().parameters.decision = combo.value;
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
            jsonData: {
                taskId: taskId,
                processVariables: parameters
            },
            returnUrl: window.location, // TODO this will produce a "this task does not exist" page.  Could return to the process instance, but if this is the last task, the process instance will also not exist.
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

// TODO move out of global scope
function downloadDataGrid(url, parameters) {
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

// TODO remove from global scope
function completeWorkflowTask(taskId, formName, fields)
{
    var form = document.forms[formName];
    var parameters = {};
    for (i = 0, len = fields.length; i < len; i++)
    {
        parameters[fields[i]] = form[fields[i]].value;
    }
    Ext4.Ajax.request({
        url: LABKEY.ActionURL.buildURL('workflow', 'completeTask'),
        method: 'POST',
        jsonData: {
            taskId: taskId,
            processVariables: parameters
        },
        returnUrl: window.location, // TODO this will produce a "this task does not exist" page.  Could return to the process instance, but if this is the last task, the process instance will also not exist.
        scope: this,
        success: function(response) {
            window.location = window.location; // avoid form resubmit
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

function createCompleteTaskWindow(taskId, name, parameters) {
    Ext4.create("Workflow.view.dialog.CompleteTask", {
        taskId: taskId,
        name: name,
        parameters: parameters
    }).show();
}