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
GO

CREATE TABLE workflow.ACT_GE_PROPERTY (
    NAME_ nvarchar(64),
    VALUE_ nvarchar(300),
    REV_ int,
    primary key (NAME_)
);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('schema.version', '5.17.0.2', 1);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('schema.history', 'create(5.17.0.2)', 1);

INSERT INTO workflow.ACT_GE_PROPERTY
values ('next.dbid', '1', 1);

CREATE TABLE workflow.ACT_GE_BYTEARRAY (
    ID_ nvarchar(64),
    REV_ int,
    NAME_ nvarchar(255),
    DEPLOYMENT_ID_ nvarchar(64),
    BYTES_  varbinary(max),
    GENERATED_ tinyint,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_DEPLOYMENT (
    ID_ nvarchar(64),
    NAME_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    TENANT_ID_ nvarchar(255) default '',
    DEPLOY_TIME_ datetime,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_MODEL (
    ID_ nvarchar(64) not null,
    REV_ int,
    NAME_ nvarchar(255),
    KEY_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    CREATE_TIME_ datetime,
    LAST_UPDATE_TIME_ datetime,
    VERSION_ int,
    META_INFO_ nvarchar(4000),
    DEPLOYMENT_ID_ nvarchar(64),
    EDITOR_SOURCE_VALUE_ID_ nvarchar(64),
    EDITOR_SOURCE_EXTRA_VALUE_ID_ nvarchar(64),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_EXECUTION (
    ID_ nvarchar(64),
    REV_ int,
    PROC_INST_ID_ nvarchar(64),
    BUSINESS_KEY_ nvarchar(255),
    PARENT_ID_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    SUPER_EXEC_ nvarchar(64),
    ACT_ID_ nvarchar(255),
    IS_ACTIVE_ tinyint,
    IS_CONCURRENT_ tinyint,
    IS_SCOPE_ tinyint,
    IS_EVENT_SCOPE_ tinyint,
    SUSPENSION_STATE_ tinyint,
    CACHED_ENT_STATE_ int,
    TENANT_ID_ nvarchar(255) default '',
    NAME_ nvarchar(255),
    LOCK_TIME_ datetime,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_JOB (
    ID_ nvarchar(64) NOT NULL,
  REV_ int,
    TYPE_ nvarchar(255) NOT NULL,
    LOCK_EXP_TIME_ datetime,
    LOCK_OWNER_ nvarchar(255),
    EXCLUSIVE_ bit,
    EXECUTION_ID_ nvarchar(64),
    PROCESS_INSTANCE_ID_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    RETRIES_ int,
    EXCEPTION_STACK_ID_ nvarchar(64),
    EXCEPTION_MSG_ nvarchar(4000),
    DUEDATE_ datetime NULL,
    REPEAT_ nvarchar(255),
    HANDLER_TYPE_ nvarchar(255),
    HANDLER_CFG_ nvarchar(4000),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RE_PROCDEF (
    ID_ nvarchar(64) not null,
    REV_ int,
    CATEGORY_ nvarchar(255),
    NAME_ nvarchar(255),
    KEY_ nvarchar(255) not null,
    VERSION_ int not null,
    DEPLOYMENT_ID_ nvarchar(64),
    RESOURCE_NAME_ nvarchar(4000),
    DGRM_RESOURCE_NAME_ nvarchar(4000),
    DESCRIPTION_ nvarchar(4000),
    HAS_START_FORM_KEY_ tinyint,
    HAS_GRAPHICAL_NOTATION_ tinyint,
    SUSPENSION_STATE_ tinyint,
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_TASK (
    ID_ nvarchar(64),
    REV_ int,
    EXECUTION_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    NAME_ nvarchar(255),
    PARENT_TASK_ID_ nvarchar(64),
    DESCRIPTION_ nvarchar(4000),
    TASK_DEF_KEY_ nvarchar(255),
    OWNER_ nvarchar(255),
    ASSIGNEE_ nvarchar(255),
    DELEGATION_ nvarchar(64),
    PRIORITY_ int,
    CREATE_TIME_ datetime,
    DUE_DATE_ datetime,
    CATEGORY_ nvarchar(255),
    SUSPENSION_STATE_ int,
    TENANT_ID_ nvarchar(255) default '',
    FORM_KEY_ nvarchar(255),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_IDENTITYLINK (
    ID_ nvarchar(64),
    REV_ int,
    GROUP_ID_ nvarchar(255),
    TYPE_ nvarchar(255),
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_VARIABLE (
    ID_ nvarchar(64) not null,
    REV_ int,
    TYPE_ nvarchar(255) not null,
    NAME_ nvarchar(255) not null,
    EXECUTION_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_RU_EVENT_SUBSCR (
    ID_ nvarchar(64) not null,
    REV_ int,
    EVENT_TYPE_ nvarchar(255) not null,
    EVENT_NAME_ nvarchar(255),
    EXECUTION_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    ACTIVITY_ID_ nvarchar(64),
    CONFIGURATION_ nvarchar(255),
    CREATED_ datetime not null,
    PROC_DEF_ID_ nvarchar(64),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_EVT_LOG (
    LOG_NR_ numeric(19,0) IDENTITY(1,1),
    TYPE_ nvarchar(64),
    PROC_DEF_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    TIME_STAMP_ datetime not null,
    USER_ID_ nvarchar(255),
    DATA_ varbinary(max),
    LOCK_OWNER_ nvarchar(255),
    LOCK_TIME_ datetime null,
    IS_PROCESSED_ tinyint default 0,
    primary key (LOG_NR_)
);

create index ACT_IDX_EXEC_BUSKEY on workflow.ACT_RU_EXECUTION(BUSINESS_KEY_);
create index ACT_IDX_TASK_CREATE on workflow.ACT_RU_TASK(CREATE_TIME_);
create index ACT_IDX_IDENT_LNK_USER on workflow.ACT_RU_IDENTITYLINK(USER_ID_);
create index ACT_IDX_IDENT_LNK_GROUP on workflow.ACT_RU_IDENTITYLINK(GROUP_ID_);
create index ACT_IDX_EVENT_SUBSCR_CONFIG_ on workflow.ACT_RU_EVENT_SUBSCR(CONFIGURATION_);
create index ACT_IDX_VARIABLE_TASK_ID on workflow.ACT_RU_VARIABLE(TASK_ID_);
create index ACT_IDX_ATHRZ_PROCEDEF on workflow.ACT_RU_IDENTITYLINK(PROC_DEF_ID_);
create index ACT_IDX_EXECUTION_PROC on workflow.ACT_RU_EXECUTION(PROC_DEF_ID_);
create index ACT_IDX_EXECUTION_PARENT on workflow.ACT_RU_EXECUTION(PARENT_ID_);
create index ACT_IDX_EXECUTION_SUPER on workflow.ACT_RU_EXECUTION(SUPER_EXEC_);
create index ACT_IDX_EXECUTION_IDANDREV on workflow.ACT_RU_EXECUTION(ID_, REV_);
create index ACT_IDX_VARIABLE_BA on workflow.ACT_RU_VARIABLE(BYTEARRAY_ID_);
create index ACT_IDX_VARIABLE_EXEC on workflow.ACT_RU_VARIABLE(EXECUTION_ID_);
create index ACT_IDX_VARIABLE_PROCINST on workflow.ACT_RU_VARIABLE(PROC_INST_ID_);
create index ACT_IDX_IDENT_LNK_TASK on workflow.ACT_RU_IDENTITYLINK(TASK_ID_);
create index ACT_IDX_IDENT_LNK_PROCINST on workflow.ACT_RU_IDENTITYLINK(PROC_INST_ID_);
create index ACT_IDX_TASK_EXEC on workflow.ACT_RU_TASK(EXECUTION_ID_);
create index ACT_IDX_TASK_PROCINST on workflow.ACT_RU_TASK(PROC_INST_ID_);
create index ACT_IDX_EXEC_PROC_INST_ID on workflow.ACT_RU_EXECUTION(PROC_INST_ID_);
create index ACT_IDX_TASK_PROC_DEF_ID on workflow.ACT_RU_TASK(PROC_DEF_ID_);
create index ACT_IDX_EVENT_SUBSCR_EXEC_ID on workflow.ACT_RU_EVENT_SUBSCR(EXECUTION_ID_);
create index ACT_IDX_JOB_EXCEPTION_STACK_ID on workflow.ACT_RU_JOB(EXCEPTION_STACK_ID_);

ALTER TABLE workflow.ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL 
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_);

ALTER TABLE workflow.ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);
    
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PARENT 
    foreign key (PARENT_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);
    
ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_SUPER 
    foreign key (SUPER_EXEC_) 
    references workflow.ACT_RU_EXECUTION (ID_);

ALTER TABLE workflow.ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);

ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK 
    foreign key (TASK_ID_) 
    references workflow.ACT_RU_TASK (ID_);
    
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_);
    
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);       
    
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_EXE
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION (ID_);
    
ALTER TABLE workflow.ACT_RU_TASK
    add constraint ACT_FK_TASK_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION (ID_);
    
ALTER TABLE workflow.ACT_RU_TASK
  add constraint ACT_FK_TASK_PROCDEF
  foreign key (PROC_DEF_ID_)
  references workflow.ACT_RE_PROCDEF (ID_);
  
ALTER TABLE workflow.ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_EXE 
    foreign key (EXECUTION_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_);

ALTER TABLE workflow.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION(ID_);

ALTER TABLE workflow.ACT_RU_VARIABLE 
    add constraint ACT_FK_VAR_BYTEARRAY 
    foreign key (BYTEARRAY_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);
  
ALTER TABLE workflow.ACT_RU_JOB 
    add constraint ACT_FK_JOB_EXCEPTION 
    foreign key (EXCEPTION_STACK_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);
    
ALTER TABLE workflow.ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION(ID_);
    
ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE 
    foreign key (EDITOR_SOURCE_VALUE_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_SOURCE_EXTRA 
    foreign key (EDITOR_SOURCE_EXTRA_VALUE_ID_) 
    references workflow.ACT_GE_BYTEARRAY (ID_);

ALTER TABLE workflow.ACT_RE_MODEL 
    add constraint ACT_FK_MODEL_DEPLOYMENT 
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_);    

CREATE TABLE workflow.ACT_HI_PROCINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    BUSINESS_KEY_ nvarchar(255),
    PROC_DEF_ID_ nvarchar(64) not null,
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    START_USER_ID_ nvarchar(255),
    START_ACT_ID_ nvarchar(255),
    END_ACT_ID_ nvarchar(255),
    SUPER_PROCESS_INSTANCE_ID_ nvarchar(64),
    DELETE_REASON_ nvarchar(4000),
    TENANT_ID_ nvarchar(255) default '',
    NAME_ nvarchar(255),
    primary key (ID_),
    unique (PROC_INST_ID_)
);

CREATE TABLE workflow.ACT_HI_ACTINST (
    ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64) not null,
    EXECUTION_ID_ nvarchar(64) not null,
    ACT_ID_ nvarchar(255) not null,
    TASK_ID_ nvarchar(64),
    CALL_PROC_INST_ID_ nvarchar(64),
    ACT_NAME_ nvarchar(255),
    ACT_TYPE_ nvarchar(255) not null,
    ASSIGNEE_ nvarchar(255),
    START_TIME_ datetime not null,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_TASKINST (
    ID_ nvarchar(64) not null,
    PROC_DEF_ID_ nvarchar(64),
    TASK_DEF_KEY_ nvarchar(255),
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    NAME_ nvarchar(255),
    PARENT_TASK_ID_ nvarchar(64),
    DESCRIPTION_ nvarchar(4000),
    OWNER_ nvarchar(255),
    ASSIGNEE_ nvarchar(255),
    START_TIME_ datetime not null,
    CLAIM_TIME_ datetime,
    END_TIME_ datetime,
    DURATION_ numeric(19,0),
    DELETE_REASON_ nvarchar(4000),
    PRIORITY_ int,
    DUE_DATE_ datetime,
    FORM_KEY_ nvarchar(255),
    CATEGORY_ nvarchar(255),
    TENANT_ID_ nvarchar(255) default '',
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_VARINST (
    ID_ nvarchar(64) not null,
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(100),
    REV_ int,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    CREATE_TIME_ datetime,
    LAST_UPDATED_TIME_ datetime,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_DETAIL (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255) not null,
    PROC_INST_ID_ nvarchar(64),
    EXECUTION_ID_ nvarchar(64),
    TASK_ID_ nvarchar(64),
    ACT_INST_ID_ nvarchar(64),
    NAME_ nvarchar(255) not null,
    VAR_TYPE_ nvarchar(255),
    REV_ int,
    TIME_ datetime not null,
    BYTEARRAY_ID_ nvarchar(64),
    DOUBLE_ double precision,
    LONG_ numeric(19,0),
    TEXT_ nvarchar(4000),
    TEXT2_ nvarchar(4000),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_COMMENT (
    ID_ nvarchar(64) not null,
    TYPE_ nvarchar(255),
    TIME_ datetime not null,
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    ACTION_ nvarchar(255),
    MESSAGE_ nvarchar(4000),
    FULL_MSG_ varbinary(max),
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_ATTACHMENT (
    ID_ nvarchar(64) not null,
    REV_ integer,
    USER_ID_ nvarchar(255),
    NAME_ nvarchar(255),
    DESCRIPTION_ nvarchar(4000),
    TYPE_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
    URL_ nvarchar(4000),
    CONTENT_ID_ nvarchar(64),
    TIME_ datetime,
    primary key (ID_)
);

CREATE TABLE workflow.ACT_HI_IDENTITYLINK (
    ID_ nvarchar(64),
    GROUP_ID_ nvarchar(255),
    TYPE_ nvarchar(255),
    USER_ID_ nvarchar(255),
    TASK_ID_ nvarchar(64),
    PROC_INST_ID_ nvarchar(64),
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

EXEC core.fn_dropifexists 'ACT_GE_BYTEARRAY', 'workflow', 'CONSTRAINT', 'ACT_FK_BYTEARR_DEPL' ;

ALTER TABLE workflow.ACT_GE_BYTEARRAY
    add constraint ACT_FK_BYTEARR_DEPL
    foreign key (DEPLOYMENT_ID_) 
    references workflow.ACT_RE_DEPLOYMENT (ID_)
    ON DELETE CASCADE;

EXEC core.fn_dropifexists 'ACT_RU_IDENTITYLINK', 'workflow', 'CONSTRAINT', 'ACT_FK_TSKASS_TASK';
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_TSKASS_TASK
    foreign key (TASK_ID_) 
    references workflow.ACT_RU_TASK (ID_)
    ON DELETE CASCADE;
    
EXEC core.fn_dropifexists 'ACT_RU_IDENTITYLINK', 'workflow', 'CONSTRAINT', 'ACT_FK_ATHRZ_PROCEDEF'; 
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_ATHRZ_PROCEDEF
    foreign key (PROC_DEF_ID_) 
    references workflow.ACT_RE_PROCDEF (ID_)
    ON DELETE CASCADE;
    
EXEC core.fn_dropifexists 'ACT_RU_IDENTITYLINK', 'workflow', 'CONSTRAINT', 'ACT_FK_IDL_PROCINST'; 
ALTER TABLE workflow.ACT_RU_IDENTITYLINK
    add constraint ACT_FK_IDL_PROCINST
    foreign key (PROC_INST_ID_) 
    references workflow.ACT_RU_EXECUTION (ID_)
    ON DELETE CASCADE;    


EXEC core.fn_dropifexists 'ACT_RU_VARIABLE', 'workflow', 'CONSTRAINT', 'ACT_FK_VAR_PROCINST'; 
ALTER TABLE workflow.ACT_RU_VARIABLE
    add constraint ACT_FK_VAR_PROCINST
    foreign key (PROC_INST_ID_)
    references workflow.ACT_RU_EXECUTION(ID_)
    ON DELETE CASCADE;

EXEC core.fn_dropifexists 'ACT_RU_EVENT_SUBSCR', 'workflow', 'CONSTRAINT', 'ACT_FK_EVENT_EXEC'; 
ALTER TABLE workflow.ACT_RU_EVENT_SUBSCR
    add constraint ACT_FK_EVENT_EXEC
    foreign key (EXECUTION_ID_)
    references workflow.ACT_RU_EXECUTION(ID_)
    ON DELETE CASCADE;

EXEC core.fn_dropifexists 'ACT_RE_MODEL', 'workflow', 'CONSTRAINT', 'ACT_FK_MODEL_DEPLOYMENT'; 
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

/* workflow-16.20-16.30.sql */

CREATE NONCLUSTERED INDEX IDX_ACT_HI_PROCINST_STARTTIME_PROCINST ON workflow.ACT_HI_PROCINST
(START_TIME_, PROC_INST_ID_);

CREATE NONCLUSTERED INDEX IDX_ACT_HI_VARINST_NAME_LONG ON workflow.ACT_HI_VARINST
(NAME_, LONG_);

CREATE NONCLUSTERED INDEX IDX_ACT_HI_VARINST_NAME_TEXT ON workflow.ACT_HI_VARINST
(NAME_, TEXT_);

CREATE NONCLUSTERED INDEX IDX_ACT_HI_VARINST_PROCINST_NAME_LONG ON workflow.ACT_HI_VARINST
(PROC_INST_ID_, NAME_, LONG_);

CREATE NONCLUSTERED INDEX IDX_ACT_HI_VARINST_PROCINST_NAME_TEXT ON workflow.ACT_HI_VARINST
(PROC_INST_ID_, NAME_, TEXT_);

CREATE NONCLUSTERED INDEX IDX_ACT_RU_TASK_PROCINST_TASKDEFKEY ON workflow.ACT_RU_TASK
(PROC_INST_ID_, TASK_DEF_KEY_);

CREATE NONCLUSTERED INDEX IDX_ACT_RU_VARIABLE_PROCINST_NAME_LONG ON workflow.ACT_RU_VARIABLE
(NAME_, PROC_INST_ID_, LONG_);