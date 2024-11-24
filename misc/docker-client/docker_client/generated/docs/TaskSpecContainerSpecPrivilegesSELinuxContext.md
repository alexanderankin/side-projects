# TaskSpecContainerSpecPrivilegesSELinuxContext

SELinux labels of the container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**disable** | **bool** | Disable SELinux | [optional] 
**user** | **str** | SELinux user label | [optional] 
**role** | **str** | SELinux role label | [optional] 
**type** | **str** | SELinux type label | [optional] 
**level** | **str** | SELinux level label | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_container_spec_privileges_se_linux_context import TaskSpecContainerSpecPrivilegesSELinuxContext

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecPrivilegesSELinuxContext from a JSON string
task_spec_container_spec_privileges_se_linux_context_instance = TaskSpecContainerSpecPrivilegesSELinuxContext.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecPrivilegesSELinuxContext.to_json())

# convert the object into a dict
task_spec_container_spec_privileges_se_linux_context_dict = task_spec_container_spec_privileges_se_linux_context_instance.to_dict()
# create an instance of TaskSpecContainerSpecPrivilegesSELinuxContext from a dict
task_spec_container_spec_privileges_se_linux_context_from_dict = TaskSpecContainerSpecPrivilegesSELinuxContext.from_dict(task_spec_container_spec_privileges_se_linux_context_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


