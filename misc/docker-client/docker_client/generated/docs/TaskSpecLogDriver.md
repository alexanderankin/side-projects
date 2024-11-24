# TaskSpecLogDriver

Specifies the log driver to use for tasks created from this spec. If not present, the default one for the swarm will be used, finally falling back to the engine default if not specified. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | [optional] 
**options** | **Dict[str, str]** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.task_spec_log_driver import TaskSpecLogDriver

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecLogDriver from a JSON string
task_spec_log_driver_instance = TaskSpecLogDriver.from_json(json)
# print the JSON string representation of the object
print(TaskSpecLogDriver.to_json())

# convert the object into a dict
task_spec_log_driver_dict = task_spec_log_driver_instance.to_dict()
# create an instance of TaskSpecLogDriver from a dict
task_spec_log_driver_from_dict = TaskSpecLogDriver.from_dict(task_spec_log_driver_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


