/*
 * Copyright (c) 2015 LabKey Corporation. All rights reserved. No portion of this work may be reproduced in
 * any form or by any electronic or mechanical means without written permission from LabKey Corporation.
 * This is a fake comment to touch the file so that the copyright will be updated.
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


