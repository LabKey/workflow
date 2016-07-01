/*
 * Copyright (c) 2016 LabKey Corporation
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
UPDATE workflow.ACT_GE_PROPERTY SET VALUE_ = '5.20.0.2' WHERE NAME_ = 'schema.version';

CREATE INDEX ACT_IDX_HI_TASK_INST_PROCINST ON workflow.ACT_HI_TASKINST(PROC_INST_ID_);

create table workflow.ACT_PROCDEF_INFO (
	ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    REV_ integer,
    INFO_JSON_ID_ varchar(64),
    primary key (ID_)
);

create index ACT_IDX_PROCDEF_INFO_JSON on workflow.ACT_PROCDEF_INFO(INFO_JSON_ID_);
alter table workflow.ACT_PROCDEF_INFO 
    add constraint ACT_FK_INFO_JSON_BA 
    foreign key (INFO_JSON_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_PROCDEF_INFO_PROC on workflow.ACT_PROCDEF_INFO(PROC_DEF_ID_);
alter table workflow.ACT_PROCDEF_INFO 
    add constraint ACT_FK_INFO_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);
    
alter table workflow.ACT_PROCDEF_INFO
    add constraint ACT_UNIQ_INFO_PROCDEF
    unique (PROC_DEF_ID_);

update workflow.ACT_RU_EVENT_SUBSCR set PROC_DEF_ID_ = CONFIGURATION_ where EVENT_TYPE_ = 'message' and PROC_INST_ID_ is null and EXECUTION_ID_ is null;