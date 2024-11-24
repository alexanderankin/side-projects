# TaskSpecResources

Resource requirements which apply to each individual container created as part of the service. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**limits** | [**Limit**](Limit.md) |  | [optional] 
**reservations** | [**ResourceObject**](ResourceObject.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.task_spec_resources import TaskSpecResources

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecResources from a JSON string
task_spec_resources_instance = TaskSpecResources.from_json(json)
# print the JSON string representation of the object
print(TaskSpecResources.to_json())

# convert the object into a dict
task_spec_resources_dict = task_spec_resources_instance.to_dict()
# create an instance of TaskSpecResources from a dict
task_spec_resources_from_dict = TaskSpecResources.from_dict(task_spec_resources_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


