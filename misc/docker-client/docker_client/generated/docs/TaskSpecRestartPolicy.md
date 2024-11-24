# TaskSpecRestartPolicy

Specification for the restart policy which applies to containers created as part of this service. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**condition** | **str** | Condition for restart. | [optional] 
**delay** | **int** | Delay between restart attempts. | [optional] 
**max_attempts** | **int** | Maximum attempts to restart a given container before giving up (default value is 0, which is ignored).  | [optional] [default to 0]
**window** | **int** | Windows is the time window used to evaluate the restart policy (default value is 0, which is unbounded).  | [optional] [default to 0]

## Example

```python
from docker_client.generated.models.task_spec_restart_policy import TaskSpecRestartPolicy

# TODO update the JSON string below
json = "{}"
# create an instance of TaskSpecRestartPolicy from a JSON string
task_spec_restart_policy_instance = TaskSpecRestartPolicy.from_json(json)
# print the JSON string representation of the object
print(TaskSpecRestartPolicy.to_json())

# convert the object into a dict
task_spec_restart_policy_dict = task_spec_restart_policy_instance.to_dict()
# create an instance of TaskSpecRestartPolicy from a dict
task_spec_restart_policy_from_dict = TaskSpecRestartPolicy.from_dict(task_spec_restart_policy_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


