/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 */
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
        this.assgineeId = config['assigneeId'];
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
                    '</tpl>'),
            listeners : {
                scope: this,
                'select': function (field, value)
                {
                    this.down('button#DelegateButton').setDisabled(value.length == 0);
                    this.down('button#AssignButton').setDisabled(value.length == 0);
                }
            }
        });

        this.instructions = Ext4.create('Ext.view.View', {
            itemId: 'instructions',
            xtype: 'box',
            width: 400,
            padding: '15px 15px',
            store : this.getPermissionsStore(),
            tpl: new Ext4.XTemplate (
                    'You can reassign a task to any user with one of the following permissions within this folder:  ',
                    '<ul>',
                    '<tpl for=".">',
                    '<li>{name:htmlEncode}',
                    '</tpl>',
                    '</ul>',
                    'When delegating, you will retain ownership of the task for further review after the task is completed.'
            )
        });

        this.items = [
            this.instructions,
            this.userCombo
        ];

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
            disabled: this.assigneeId != "" && this.assigneeId != null,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, "Claim");
                this.close();
            }
        },{
            text: 'Delegate',
            itemId: 'DelegateButton',
            disabled: true,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, "Delegate");
                this.close();
            }
        },{
            text: 'Assign',
            itemId: 'AssignButton',
            disabled: true,
            scope: this,
            handler : function() {
                this.fireEvent(this.reassignEvent, this.taskId, 'Assign');
                this.close();
            }
        }
        ];

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
                window.location.reload();
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

    getPermissionsStore : function() {
        // define data models
        if (!Ext4.ModelManager.isRegistered('LABKEY.Workflow.ReassignmentPermissions')) {
            Ext4.define('LABKEY.Workflow.ReassignmentPermissions', {
                extend: 'Ext.data.Model',
                fields: [
                    {name: 'name', type: 'string'}
                ]
            });
        }

        return Ext4.create('Ext.data.Store', {
            model: 'LABKEY.Workflow.ReassignmentPermissions',
            autoLoad: true,
            proxy: {
                type: 'ajax',
                url: LABKEY.ActionURL.buildURL('workflow', 'getReassignPermissionNames.api', LABKEY.container.path, {taskId: this.taskId}),
                reader: {
                    type: 'json',
                    root: 'data.permissions'
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
                url: LABKEY.ActionURL.buildURL('workflow', 'candidateUsers.api', LABKEY.container.path, {taskId: this.taskId}),
                reader: {
                    type: 'json',
                    root: 'data.users'
                }
            }
        });
    }

}
);

// helper used in display column
function createReassignTaskWindow(taskId, assigneeId) {
    Ext4.create("Workflow.view.dialog.ReassignTask", {
        taskId: taskId,
        assigneeId: assigneeId,
    }).show();
}