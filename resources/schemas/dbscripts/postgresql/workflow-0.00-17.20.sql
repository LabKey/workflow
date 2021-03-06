/* workflow-00.00-15.10.sql */

/*
 * Activiti BPM Platform
 * Copyright 2010-2014 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */
-- @SkipLabKeySyntaxCheck
-- Create schema, tables, indexes, and constraints used for Workflow module here
-- All SQL VIEW definitions should be created in workflow-create.sql and dropped in workflow-drop.sql
CREATE SCHEMA workflow;

CREATE TABLE workflow.ACT_GE_PROPERTY (
    NAME_ varchar(64),
    VALUE_ varchar(300),
    REV_ integer,
    primary key (NAME_)
);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('schema.version', '5.17.0.2', 1);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('schema.history', 'create(5.17.0.2)', 1);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

CREATE TABLE workflow.ACT_GE_BYTEARRAY (
    ID_ varchar(64),
    REV_ integer,
    NAME_ varchar(255),
    DEPLOYMENT_ID_ varchar(64),
    BYTES_ bytea,
    GENERATED_ boolean,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_DEPLOYMENT (
    ID_ varchar(64),
    NAME_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    DEPLOY_TIME_ timestamp,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_MODEL (
    ID_ varchar(64) not null,
    REV_ integer,
    NAME_ varchar(255),
    KEY_ varchar(255),
    CATEGORY_ varchar(255),
    CREATE_TIME_ timestamp,
    LAST_UPDATE_TIME_ timestamp,
    VERSION_ integer,
    META_INFO_ varchar(4000),
    DEPLOYMENT_ID_ varchar(64),
    EDITOR_SOURCE_VALUE_ID_ varchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_EXECUTION (
    ID_ varchar(64),
    REV_ integer,
    PROC_INST_ID_ varchar(64),
    BUSINESS_KEY_ varchar(255),
    PARENT_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    SUPER_EXEC_ varchar(64),
    ACT_ID_ varchar(255),
    IS_ACTIVE_ boolean,
    IS_CONCURRENT_ boolean,
    IS_SCOPE_ boolean,
    IS_EVENT_SCOPE_ boolean,
    SUSPENSION_STATE_ integer,
    CACHED_ENT_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    LOCK_TIME_ timestamp,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_JOB (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    TYPE_ varchar(255) NOT NULL,
    LOCK_EXP_TIME_ timestamp,
    LOCK_OWNER_ varchar(255),
    EXCLUSIVE_ boolean,
    EXECUTION_ID_ varchar(64),
    PROCESS_INSTANCE_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    RETRIES_ integer,
    EXCEPTION_STACK_ID_ varchar(64),
    EXCEPTION_MSG_ varchar(4000),
    DUEDATE_ timestamp,
    REPEAT_ varchar(255),
    HANDLER_TYPE_ varchar(255),
    HANDLER_CFG_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_PROCDEF (
    ID_ varchar(64) NOT NULL,
    REV_ integer,
    CATEGORY_ varchar(255),
    NAME_ varchar(255),
    KEY_ varchar(255) NOT NULL,
    VERSION_ integer NOT NULL,
    DEPLOYMENT_ID_ varchar(64),
    RESOURCE_NAME_ varchar(4000),
    DGRM_RESOURCE_NAME_ varchar(4000),
    DESCRIPTION_ varchar(4000),
    HAS_START_FORM_KEY_ boolean,
    HAS_GRAPHICAL_NOTATION_ boolean,
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_TASK (
    ID_ varchar(64),
    REV_ integer,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    TASK_DEF_KEY_ varchar(255),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    DELEGATION_ varchar(64),
    PRIORITY_ integer,
    CREATE_TIME_ timestamp,
    DUE_DATE_ timestamp,
    CATEGORY_ varchar(255),
    SUSPENSION_STATE_ integer,
    TENANT_ID_ varchar(255) default '',
    FORM_KEY_ varchar(255),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_IDENTITYLINK (
    ID_ varchar(64),
    REV_ integer,
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    PROC_DEF_ID_ varchar (64),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_VARIABLE (
    ID_ varchar(64) not null,
    REV_ integer,
    TYPE_ varchar(255) not null,
    NAME_ varchar(255) not null,
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    TASK_ID_ varchar(64),
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_EVENT_SUBSCR (
    ID_ varchar(64) not null,
    REV_ integer,
    EVENT_TYPE_ varchar(255) not null,
    EVENT_NAME_ varchar(255),
    EXECUTION_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTIVITY_ID_ varchar(64),
    CONFIGURATION_ varchar(255),
    CREATED_ timestamp not null,
    PROC_DEF_ID_ varchar(64),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_EVT_LOG (
    LOG_NR_ SERIAL PRIMARY KEY,
    TYPE_ varchar(64),
    PROC_DEF_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    TIME_STAMP_ timestamp not null,
    USER_ID_ varchar(255),
    DATA_ bytea,
    LOCK_OWNER_ varchar(255),
    LOCK_TIME_ timestamp null,
    IS_PROCESSED_ smallint default 0
);

create index ACT_IDX_EXEC_BUSKEY on workflow.ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_TASK_CREATE on workflow.ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on workflow.ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on workflow.ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on workflow.ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_VARIABLE_TASK_ID on workflow.ACT_RU_VARIABLE(TASK_ID_);

create index ACT_IDX_BYTEAR_DEPL on workflow.ACT_GE_BYTEARRAY(DEPLOYMENT_ID_);
ALTER TABLE workflow.ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_);

ALTER TABLE workflow.ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);
    
create index ACT_IDX_EXE_PROCINST on workflow.ACT_RU_EXECUTION(PROC_INST_ID_);
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCINST 
    foreign key (PROC_INST_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PARENT on workflow.ACT_RU_EXECUTION(PARENT_ID_);
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT
    foreign key (PARENT_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);
    
create index ACT_IDX_EXE_SUPER on workflow.ACT_RU_EXECUTION(SUPER_EXEC_);
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER
    foreign key (SUPER_EXEC_) 
    references workflow.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_EXE_PROCDEF on workflow.ACT_RU_EXECUTION(PROC_DEF_ID_); 
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);    
    

create index ACT_IDX_TSKASS_TASK on workflow.ACT_RU_IDENTITYLINK(TASK_ID_);
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
    foreign key (TASK_ID_) 
    references workflow.ACT_RU_TASK (ID_);
    
create index ACT_IDX_ATHRZ_PROCEDEF on workflow.ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);
    
create index ACT_IDX_IDL_PROCINST on workflow.ACT_RU_IDENTITYLINK(PROC_INST_ID_);
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);    
    
create index ACT_IDX_TASK_EXEC on workflow.ACT_RU_TASK(EXECUTION_ID_);
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION (ID_);
    
create index ACT_IDX_TASK_PROCINST on workflow.ACT_RU_TASK(PROC_INST_ID_);
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION (ID_);
    
create index ACT_IDX_TASK_PROCDEF on workflow.ACT_RU_TASK(PROC_DEF_ID_);
ALTER TABLE workflow.ACT_RU_TASK
  add constraint ACT_FK_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references workflow.ACT_RE_PROCDEF (ID_);
  
create index ACT_IDX_VAR_EXE on workflow.ACT_RU_VARIABLE(EXECUTION_ID_);
ALTER TABLE workflow.ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_EXE
    foreign key (EXECUTION_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);

create index ACT_IDX_VAR_PROCINST on workflow.ACT_RU_VARIABLE(PROC_INST_ID_);
ALTER TABLE workflow.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION(ID_);

create index ACT_IDX_VAR_BYTEARRAY on workflow.ACT_RU_VARIABLE(BYTEARRAY_ID_);
ALTER TABLE workflow.ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_JOB_EXCEPTION on workflow.ACT_RU_JOB(EXCEPTION_STACK_ID_);
ALTER TABLE workflow.ACT_RU_JOB 
    add constraint ACT_FK_JOB_EXCEPTION
    foreign key (EXCEPTION_STACK_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_EVENT_SUBSCR on workflow.ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
ALTER TABLE workflow.ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION(ID_);

create index ACT_IDX_MODEL_SOURCE on workflow.ACT_RE_MODEL(EDITOR_SOURCE_VALUE_ID_);
ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

create index ACT_IDX_MODEL_SOURCE_EXTRA on workflow.ACT_RE_MODEL(EDITOR_SOURCE_EXTRA_VALUE_ID_);
ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);
    
create index ACT_IDX_MODEL_DEPLOYMENT on workflow.ACT_RE_MODEL(DEPLOYMENT_ID_);    
ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_);        

CREATE TABLE workflow.ACT_HI_PROCINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    BUSINESS_KEY_ varchar(255),
    PROC_DEF_ID_ varchar(64) not null,
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    START_USER_ID_ varchar(255),
    START_ACT_ID_ varchar(255),
    END_ACT_ID_ varchar(255),
    SUPER_PROCESS_INSTANCE_ID_ varchar(64),
    DELETE_REASON_ varchar(4000),
    TENANT_ID_ varchar(255) default '',
    NAME_ varchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

CREATE TABLE workflow.ACT_HI_ACTINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64) not null,
    EXECUTION_ID_ varchar(64) not null,
    ACT_ID_ varchar(255) not null,
    TASK_ID_ varchar(64),
    CALL_PROC_INST_ID_ varchar(64),
    ACT_NAME_ varchar(255),
    ACT_TYPE_ varchar(255) not null,
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    END_TIME_ timestamp,
    DURATION_ bigint,
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_TASKINST (
    ID_ varchar(64) not null,
    PROC_DEF_ID_ varchar(64),
    TASK_DEF_KEY_ varchar(255),
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    NAME_ varchar(255),
    PARENT_TASK_ID_ varchar(64),
    DESCRIPTION_ varchar(4000),
    OWNER_ varchar(255),
    ASSIGNEE_ varchar(255),
    START_TIME_ timestamp not null,
    CLAIM_TIME_ timestamp,
    END_TIME_ timestamp,
    DURATION_ bigint,
    DELETE_REASON_ varchar(4000),
    PRIORITY_ integer,
    DUE_DATE_ timestamp,
    FORM_KEY_ varchar(255),
    CATEGORY_ varchar(255),
    TENANT_ID_ varchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_VARINST (
    ID_ varchar(64) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(100),
    REV_ integer,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    CREATE_TIME_ timestamp,
    LAST_UPDATED_TIME_ timestamp,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_DETAIL (
    ID_ varchar(64) not null,
    TYPE_ varchar(255) not null,
    PROC_INST_ID_ varchar(64),
    EXECUTION_ID_ varchar(64),
    TASK_ID_ varchar(64),
    ACT_INST_ID_ varchar(64),
    NAME_ varchar(255) not null,
    VAR_TYPE_ varchar(64),
    REV_ integer,
    TIME_ timestamp not null,
    BYTEARRAY_ID_ varchar(64),
    DOUBLE_ double precision,
    LONG_ bigint,
    TEXT_ varchar(4000),
    TEXT2_ varchar(4000),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_COMMENT (
    ID_ varchar(64) not null,
    TYPE_ varchar(255),
    TIME_ timestamp not null,
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    ACTION_ varchar(255),
    MESSAGE_ varchar(4000),
    FULL_MSG_ bytea,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_ATTACHMENT (
    ID_ varchar(64) not null,
    REV_ integer,
    USER_ID_ varchar(255),
    NAME_ varchar(255),
    DESCRIPTION_ varchar(4000),
    TYPE_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    URL_ varchar(4000),
    CONTENT_ID_ varchar(64),
    TIME_ timestamp,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_IDENTITYLINK (
    ID_ varchar(64),
    GROUP_ID_ varchar(255),
    TYPE_ varchar(255),
    USER_ID_ varchar(255),
    TASK_ID_ varchar(64),
    PROC_INST_ID_ varchar(64),
    primary key (ID_)
);


create index ACT_IDX_HI_PRO_INST_END on workflow.ACT_HI_PROCINST(END_TIME_);
create index ACT_IDX_HI_PRO_I_BUSKEY on workflow.ACT_HI_PROCINST(BUSINESS_KEY_);
create index ACT_IDX_HI_ACT_INST_START on workflow.ACT_HI_ACTINST(START_TIME_);
create index ACT_IDX_HI_ACT_INST_END on workflow.ACT_HI_ACTINST(END_TIME_);
create index ACT_IDX_HI_DETAIL_PROC_INST on workflow.ACT_HI_DETAIL(PROC_INST_ID_);
create index ACT_IDX_HI_DETAIL_ACT_INST on workflow.ACT_HI_DETAIL(ACT_INST_ID_);
create index ACT_IDX_HI_DETAIL_TIME on workflow.ACT_HI_DETAIL(TIME_);
create index ACT_IDX_HI_DETAIL_NAME on workflow.ACT_HI_DETAIL(NAME_);
create index ACT_IDX_HI_DETAIL_TASK_ID on workflow.ACT_HI_DETAIL(TASK_ID_);
create index ACT_IDX_HI_PROCVAR_PROC_INST on workflow.ACT_HI_VARINST(PROC_INST_ID_);
create index ACT_IDX_HI_PROCVAR_NAME_TYPE on workflow.ACT_HI_VARINST(NAME_, VAR_TYPE_);
create index ACT_IDX_HI_PROCVAR_TASK_ID on workflow.ACT_HI_VARINST(TASK_ID_);
create index ACT_IDX_HI_ACT_INST_PROCINST on workflow.ACT_HI_ACTINST(PROC_INST_ID_, ACT_ID_);
create index ACT_IDX_HI_ACT_INST_EXEC on workflow.ACT_HI_ACTINST(EXECUTION_ID_, ACT_ID_);
create index ACT_IDX_HI_IDENT_LNK_USER on workflow.ACT_HI_IDENTITYLINK(USER_ID_);
create index ACT_IDX_HI_IDENT_LNK_TASK on workflow.ACT_HI_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_HI_IDENT_LNK_PROCINST on workflow.ACT_HI_IDENTITYLINK(PROC_INST_ID_);

/* workflow-15.10-15.20.sql */

/*
 * Activiti BPM Platform
 * Copyright 2010-2015 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */

ALTER TABLE workflow.ACT_GE_BYTEARRAY
    DROP constraint IF EXISTS ACT_FK_BYTEARR_DEPL;
ALTER TABLE workflow.ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_)
    ON DELETE CASCADE;

ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    DROP constraint IF EXISTS ACT_FK_TSKASS_TASK;
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
    foreign key (TASK_ID_) 
    references workflow.ACT_RU_TASK (ID_)
    ON DELETE CASCADE;
    
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    DROP constraint IF EXISTS ACT_FK_ATHRZ_PROCEDEF;
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_)
    ON DELETE CASCADE;
    
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    DROP constraint IF EXISTS ACT_FK_IDL_PROCINST;
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_)
    ON DELETE CASCADE;    
    
ALTER TABLE workflow.ACT_RU_TASK
    DROP constraint IF EXISTS ACT_FK_TASK_EXE;
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION (ID_)
    ON DELETE NO ACTION;

ALTER TABLE workflow.ACT_RU_TASK
    DROP constraint IF EXISTS ACT_FK_TASK_PROCINST;
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION (ID_)
    ON DELETE CASCADE;

ALTER TABLE workflow.ACT_RU_VARIABLE
    DROP constraint IF EXISTS ACT_FK_VAR_PROCINST;
ALTER TABLE workflow.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION(ID_)
    ON DELETE CASCADE;

ALTER TABLE workflow.ACT_RU_EVENT_SUBSCR
    DROP constraint IF EXISTS ACT_FK_EVENT_EXEC;
ALTER TABLE workflow.ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION(ID_)
    ON DELETE CASCADE;

ALTER TABLE workflow.ACT_RE_MODEL 
    DROP constraint IF EXISTS ACT_FK_MODEL_DEPLOYMENT;
ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_)
    ON DELETE CASCADE;

/* workflow-16.10-16.20.sql */

/*
 * Activiti BPM Platform
 * Copyright 2010-2016 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */

DELETE FROM workflow.act_ru_variable WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_hi_varinst WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_ge_bytearray WHERE name_ LIKE '%var-processInstanceUrl';

DELETE FROM workflow.act_hi_detail WHERE name_ = 'processInstanceUrl';

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

/* workflow-16.20-16.30.sql */

CREATE INDEX IDX_ACT_HI_PROCINST_STARTTIME_PROCINST ON workflow.ACT_HI_PROCINST
(START_TIME_, PROC_INST_ID_);

CREATE INDEX IDX_ACT_HI_VARINST_NAME_LONG ON workflow.ACT_HI_VARINST
(NAME_, LONG_);

CREATE INDEX IDX_ACT_HI_VARINST_NAME_TEXT ON workflow.ACT_HI_VARINST
(NAME_, TEXT_);

CREATE INDEX IDX_ACT_HI_VARINST_PROCINST_NAME_LONG ON workflow.ACT_HI_VARINST
(PROC_INST_ID_, NAME_, LONG_);

CREATE INDEX IDX_ACT_HI_VARINST_PROCINST_NAME_TEXT ON workflow.ACT_HI_VARINST
(PROC_INST_ID_, NAME_, TEXT_);

CREATE INDEX IDX_ACT_RU_TASK_PROCINST_TASKDEFKEY ON workflow.ACT_RU_TASK
(PROC_INST_ID_, TASK_DEF_KEY_);

CREATE INDEX IDX_ACT_RU_VARIABLE_PROCINST_NAME_LONG ON workflow.ACT_RU_VARIABLE
(NAME_, PROC_INST_ID_, LONG_);

/* workflow-17.10-17.20.sql */

-- Change the resource and diagram resource names from the old form of an absolute filepath to new form of moduleName:filename
update workflow.ACT_RE_PROCDEF
set resource_name_ = right(category_, strpos(reverse(category_), ':') - 1) || ':' ||
                     right(resource_name_, strpos(reverse(replace(resource_name_, '\', '/')), '/') - 1)
  , dgrm_resource_name_ = right(category_, strpos(reverse(category_), ':') - 1) || ':' ||
                          right(dgrm_resource_name_, strpos(reverse(replace(dgrm_resource_name_, '\', '/')), '/') - 1)
where strpos(replace(resource_name_, '\', '/'), '/') != 0;