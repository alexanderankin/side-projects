# Task


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of the task. | [optional] 
**version** | [**ObjectVersion**](ObjectVersion.md) |  | [optional] 
**created_at** | **str** |  | [optional] 
**updated_at** | **str** |  | [optional] 
**name** | **str** | Name of the task. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**spec** | [**TaskSpec**](TaskSpec.md) |  | [optional] 
**service_id** | **str** | The ID of the service this task is part of. | [optional] 
**slot** | **int** |  | [optional] 
**node_id** | **str** | The ID of the node that this task is on. | [optional] 
**assigned_generic_resources** | [**List[GenericResourcesInner]**](GenericResourcesInner.md) | User-defined resources can be either Integer resources (e.g, &#x60;SSD&#x3D;3&#x60;) or String resources (e.g, &#x60;GPU&#x3D;UUID1&#x60;).  | [optional] 
**status** | [**TaskStatus**](TaskStatus.md) |  | [optional] 
**desired_state** | [**TaskState**](TaskState.md) |  | [optional] 
**job_iteration** | [**ObjectVersion**](ObjectVersion.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.task import Task

# TODO update the JSON string below
json = "{}"
# create an instance of Task from a JSON string
task_instance = Task.from_json(json)
# print the JSON string representation of the object
print(Task.to_json())

# convert the object into a dict
task_dict = task_instance.to_dict()
# create an instance of Task from a dict
task_from_dict = Task.from_dict(task_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


