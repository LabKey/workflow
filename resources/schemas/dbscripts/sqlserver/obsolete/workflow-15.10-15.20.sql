/*
 * Activiti BPM Platform
 * Copyright 2010-2015 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */
/* workflow-15.10-15.11.sql */

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