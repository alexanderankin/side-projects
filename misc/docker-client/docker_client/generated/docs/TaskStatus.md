# TaskStatus

represents the status of a task.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**timestamp** | **str** |  | [optional] 
**state** | [**TaskState**](TaskState.md) |  | [optional] 
**message** | **str** |  | [optional] 
**err** | **str** |  | [optional] 
**container_status** | [**ContainerStatus**](ContainerStatus.md) |  | [optional] 
**port_status** | [**PortStatus**](PortStatus.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.task_status import TaskStatus

# TODO update the JSON string below
json = "{}"
# create an instance of TaskStatus from a JSON string
task_status_instance = TaskStatus.from_json(json)
# print the JSON string representation of the object
print(TaskStatus.to_json())

# convert the object into a dict
task_status_dict = task_status_instance.to_dict()
# create an instance of TaskStatus from a dict
task_status_from_dict = TaskStatus.from_dict(task_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


