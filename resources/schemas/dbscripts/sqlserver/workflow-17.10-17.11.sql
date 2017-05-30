-- Change the resource and diagram resource names from the old form of an absolute filepath to new form of moduleName:filename
update workflow.ACT_RE_PROCDEF
set resource_name_ = right(category_, charindex(':', reverse(category_)) - 1) + ':' +
                     right(resource_name_, charindex('/', reverse(replace(resource_name_, '\', '/'))) - 1)
  , dgrm_resource_name_ =  right(category_, charindex(':', reverse(category_)) - 1) + ':' +
                           right(dgrm_resource_name_, charindex('/', reverse(replace(dgrm_resource_name_, '\', '/'))) - 1)
where charindex('/', replace(resource_name_, '\', '/')) != 0;  		