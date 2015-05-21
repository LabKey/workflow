/**
 * Created by susanh on 5/19/15.
 */

Ext4.define("Workflow.view.dialog.ReassignTask", {
    extend: 'Ext.window.Window',
    title: 'Reassign Task',
    modal: true,
    reassignEvent : "reassignclick",

    constructor : function(config) {
        this.callParent([config]);
        this.addEvents([this.reassignEvent]);
        this.taskId = config['taskId'];
    },

    initComponent : function()
    {
        this.border = false;

        this.userCombo = Ext4.create('Ext.form.field.ComboBox', {
            store: this.getUserStore(),
            name: 'reassign',
            itemId: 'reassign',
            allowBlank: true,
            valueField: 'userId',
            displayField: 'displayName',
            padding: '15px 15px',
            fieldLabel: 'User',
            triggerAction: 'all',
            labelWidth: 75,
            typeAhead: true,
            forceSelection: true,
            tpl: Ext4.create('Ext.XTemplate',
                    '<tpl for=".">',
                    '<div class="x4-boundlist-item">{displayName:htmlEncode}</div>',
                    '</tpl>')
        });

        this.items = [
            {
                itemId: 'instructions',
                xtype: 'box',
                width: 400,
                padding: '15px 15px',
                tpl: new Ext4.XTemplate (
                    'You can reassign a task to any user within this project.  ',
                    'When delegating, you will retain ownership of the task for further review after the task is completed.'
                ),
                data: {
                    reassignmentType: this.reassignmentType,
                    taskId: this.taskId
                }

            },
            this.userCombo
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
            text: 'Claim',
            itemId: 'ClaimButton',
            disabled: false,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, "Claim");
                this.close();
            }
        },{
            text: 'Delegate',
            itemId: 'DelegateButton',
            disabled: false,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, "Delegate");
                this.close();
            }
        },{
            text: 'Assign',
            itemId: 'AssignButton',
            disabled: false,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, 'Assign');
                this.close();
            }
        }
        ]

        this.callParent();

        this.on(this.reassignEvent, this.makeReassignmentRequest, this);
    },

    makeReassignmentRequest: function(taskId, reassignmentType)
    {
        LABKEY.Ajax.request({
            url: LABKEY.ActionURL.buildURL('workflow', reassignmentType + 'Task'),
            method: 'POST',
            params: {
                taskId: taskId,
                ownerId: LABKEY.user.id,
                assigneeId: reassignmentType == 'Claim' ? LABKEY.user.id : this.userCombo.getValue(),
                returnUrl: window.location
            },
            scope: this,
            success: function(response) {
                window.location = window.location; // avoid form resubmit
            },
            failure: function(response){
                var jsonResp = LABKEY.Utils.decode(response.responseText);
                if (jsonResp && jsonResp.errors)
                {
                    var errorHTML = jsonResp.errors[0].message;
                    Ext4.Msg.alert('Error', errorHTML);
                }
            }
        });
    },

    getUserStore: function(){
        // define data models
        if (!Ext4.ModelManager.isRegistered('LABKEY.Workflow.ReassignmentUsers')) {
            Ext4.define('LABKEY.Workflow.ReassignmentUsers', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'userId', type: 'integer'},
                    {name: 'displayName', type: 'string'}
                ]
            });
        }

        return Ext4.create('Ext.data.Store', {
            model: 'LABKEY.Workflow.ReassignmentUsers',
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: LABKEY.ActionURL.buildURL('user', 'getUsers', LABKEY.container.path),
                reader: {
                    type: 'json',
                    root: 'users'
                }
            }
        });
    }

}
);

// helper used in display column
function createReassignTaskWindow(taskId) {
    Ext4.create("Workflow.view.dialog.ReassignTask", {
        taskId: taskId
    }).show();
}