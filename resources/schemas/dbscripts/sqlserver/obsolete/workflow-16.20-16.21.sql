/*
 * Copyright (c) 2017 LabKey Corporation
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