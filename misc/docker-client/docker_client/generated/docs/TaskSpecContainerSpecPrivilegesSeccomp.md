# TaskSpecContainerSpecPrivilegesSeccomp

Options for configuring seccomp on the container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**mode** | **str** |  | [optional] 
**profile** | **str** | The custom seccomp profile as a json object | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_container_spec_privileges_seccomp import TaskSpecContainerSpecPrivilegesSeccomp

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecPrivilegesSeccomp from a JSON string
task_spec_container_spec_privileges_seccomp_instance = TaskSpecContainerSpecPrivilegesSeccomp.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecPrivilegesSeccomp.to_json())

# convert the object into a dict
task_spec_container_spec_privileges_seccomp_dict = task_spec_container_spec_privileges_seccomp_instance.to_dict()
# create an instance of TaskSpecContainerSpecPrivilegesSeccomp from a dict
task_spec_container_spec_privileges_seccomp_from_dict = TaskSpecContainerSpecPrivilegesSeccomp.from_dict(task_spec_container_spec_privileges_seccomp_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


