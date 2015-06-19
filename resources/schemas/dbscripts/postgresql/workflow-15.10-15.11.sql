/*
 * Copyright (c) 2015 LabKey Corporation
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


