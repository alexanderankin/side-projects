# TaskSpecContainerSpecPrivileges

Security options for the container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**credential_spec** | [**TaskSpecContainerSpecPrivilegesCredentialSpec**](TaskSpecContainerSpecPrivilegesCredentialSpec.md) |  | [optional] 
**se_linux_context** | [**TaskSpecContainerSpecPrivilegesSELinuxContext**](TaskSpecContainerSpecPrivilegesSELinuxContext.md) |  | [optional] 
**seccomp** | [**TaskSpecContainerSpecPrivilegesSeccomp**](TaskSpecContainerSpecPrivilegesSeccomp.md) |  | [optional] 
**app_armor** | [**TaskSpecContainerSpecPrivilegesAppArmor**](TaskSpecContainerSpecPrivilegesAppArmor.md) |  | [optional] 
**no_new_privileges** | **bool** | Configuration of the no_new_privs bit in the container | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_container_spec_privileges import TaskSpecContainerSpecPrivileges

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecPrivileges from a JSON string
task_spec_container_spec_privileges_instance = TaskSpecContainerSpecPrivileges.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecPrivileges.to_json())

# convert the object into a dict
task_spec_container_spec_privileges_dict = task_spec_container_spec_privileges_instance.to_dict()
# create an instance of TaskSpecContainerSpecPrivileges from a dict
task_spec_container_spec_privileges_from_dict = TaskSpecContainerSpecPrivileges.from_dict(task_spec_container_spec_privileges_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


