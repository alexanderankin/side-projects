# SwarmSpecTaskDefaultsLogDriver

The log driver to use for tasks created in the orchestrator if unspecified by a service.  Updating this value only affects new tasks. Existing tasks continue to use their previously configured log driver until recreated. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The log driver to use as a default for new tasks.  | [optional] 
**options** | **Dict[str, str]** | Driver-specific options for the selected log driver, specified as key/value pairs.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_task_defaults_log_driver import SwarmSpecTaskDefaultsLogDriver

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecTaskDefaultsLogDriver from a JSON string
swarm_spec_task_defaults_log_driver_instance = SwarmSpecTaskDefaultsLogDriver.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecTaskDefaultsLogDriver.to_json())

# convert the object into a dict
swarm_spec_task_defaults_log_driver_dict = swarm_spec_task_defaults_log_driver_instance.to_dict()
# create an instance of SwarmSpecTaskDefaultsLogDriver from a dict
swarm_spec_task_defaults_log_driver_from_dict = SwarmSpecTaskDefaultsLogDriver.from_dict(swarm_spec_task_defaults_log_driver_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


