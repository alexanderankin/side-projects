# ServiceSpecRollbackConfig

Specification for the rollback strategy of the service.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**parallelism** | **int** | Maximum number of tasks to be rolled back in one iteration (0 means unlimited parallelism).  | [optional] 
**delay** | **int** | Amount of time between rollback iterations, in nanoseconds.  | [optional] 
**failure_action** | **str** | Action to take if an rolled back task fails to run, or stops running during the rollback.  | [optional] 
**monitor** | **int** | Amount of time to monitor each rolled back task for failures, in nanoseconds.  | [optional] 
**max_failure_ratio** | **float** | The fraction of tasks that may fail during a rollback before the failure action is invoked, specified as a floating point number between 0 and 1.  | [optional] 
**order** | **str** | The order of operations when rolling back a task. Either the old task is shut down before the new task is started, or the new task is started before the old task is shut down.  | [optional] 

## Example

```python
from docker_client.generated.models.service_spec_rollback_config import ServiceSpecRollbackConfig

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceSpecRollbackConfig from a JSON string
service_spec_rollback_config_instance = ServiceSpecRollbackConfig.from_json(json)
# print the JSON string representation of the object
print(ServiceSpecRollbackConfig.to_json())

# convert the object into a dict
service_spec_rollback_config_dict = service_spec_rollback_config_instance.to_dict()
# create an instance of ServiceSpecRollbackConfig from a dict
service_spec_rollback_config_from_dict = ServiceSpecRollbackConfig.from_dict(service_spec_rollback_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


