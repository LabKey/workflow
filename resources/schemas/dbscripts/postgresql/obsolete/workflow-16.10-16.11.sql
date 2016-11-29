/*
 * Activiti BPM Platform
 * Copyright 2010-2016 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0- http://www.apache.org/licenses/LICENSE-2.0
 */
DELETE FROM workflow.act_ru_variable WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_hi_varinst WHERE name_ = 'processInstanceUrl';
DELETE FROM workflow.act_ge_bytearray WHERE name_ LIKE '%var-processInstanceUrl';