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
/* workflow-17.10-17.11.sql */

-- Change the resource and diagram resource names from the old form of an absolute filepath to new form of moduleName:filename
update workflow.ACT_RE_PROCDEF
set resource_name_ = right(category_, strpos(reverse(category_), ':') - 1) || ':' ||
                     right(resource_name_, strpos(reverse(replace(resource_name_, '\', '/')), '/') - 1)
  , dgrm_resource_name_ = right(category_, strpos(reverse(category_), ':') - 1) || ':' ||
                          right(dgrm_resource_name_, strpos(reverse(replace(dgrm_resource_name_, '\', '/')), '/') - 1)
where strpos(replace(resource_name_, '\', '/'), '/') != 0;