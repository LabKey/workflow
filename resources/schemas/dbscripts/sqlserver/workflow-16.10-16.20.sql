/*
 * Activiti BPM Platform
 * Copyright 2010-2016 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */
/* workflow-16.10-16.11.sql */

DELETE FROM workflow.act_ru_variable WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_hi_varinst WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_ge_bytearray WHERE name_ LIKE '%var-processInstanceUrl';

/* workflow-16.11-16.12.sql */

DELETE FROM workflow.act_hi_detail WHERE name_ = 'processInstanceUrl';

/* workflow-16.12-16.13.sql */

/*
 * Activiti BPM Platform
 * Copyright 2010-2016 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */
update workflow.ACT_GE_PROPERTY set VALUE_ = '5.20.0.2' where NAME_ = 'schema.version';

create index ACT_IDX_HI_TASK_INST_PROCINST on workflow.ACT_HI_TASKINST(PROC_INST_ID_);

create table workflow.ACT_PROCDEF_INFO (
	ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64) not null,
    REV_ int,
    INFO_JSON_ID_ nvarchar(64),
    primary key (ID_)
);

create index ACT_IDX_INFO_PROCDEF on workflow.ACT_PROCDEF_INFO(PROC_DEF_ID_);

alter table workflow.ACT_PROCDEF_INFO 
    add constraint ACT_FK_INFO_JSON_BA 
    foreign key (INFO_JSON_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

alter table workflow.ACT_PROCDEF_INFO 
    add constraint ACT_FK_INFO_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);
    
alter table workflow.ACT_PROCDEF_INFO
    add constraint ACT_UNIQ_INFO_PROCDEF
    unique (PROC_DEF_ID_);

update workflow.ACT_RU_EVENT_SUBSCR set PROC_DEF_ID_ = CONFIGURATION_ where EVENT_TYPE_ = 'message' and PROC_INST_ID_ is null and EXECUTION_ID_ is null;