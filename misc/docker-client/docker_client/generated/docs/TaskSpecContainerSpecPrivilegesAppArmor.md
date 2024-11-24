# TaskSpecContainerSpecPrivilegesAppArmor

Options for configuring AppArmor on the container

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**mode** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_container_spec_privileges_app_armor import TaskSpecContainerSpecPrivilegesAppArmor

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecContainerSpecPrivilegesAppArmor from a JSON string
task_spec_container_spec_privileges_app_armor_instance = TaskSpecContainerSpecPrivilegesAppArmor.from_json(json)
# print the JSON string representation of the object
print(TaskSpecContainerSpecPrivilegesAppArmor.to_json())

# convert the object into a dict
task_spec_container_spec_privileges_app_armor_dict = task_spec_container_spec_privileges_app_armor_instance.to_dict()
# create an instance of TaskSpecContainerSpecPrivilegesAppArmor from a dict
task_spec_container_spec_privileges_app_armor_from_dict = TaskSpecContainerSpecPrivilegesAppArmor.from_dict(task_spec_container_spec_privileges_app_armor_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


