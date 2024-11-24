# SwarmSpecTaskDefaults

Defaults for creating tasks in this cluster.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**log_driver** | [**SwarmSpecTaskDefaultsLogDriver**](SwarmSpecTaskDefaultsLogDriver.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_task_defaults import SwarmSpecTaskDefaults

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecTaskDefaults from a JSON string
swarm_spec_task_defaults_instance = SwarmSpecTaskDefaults.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecTaskDefaults.to_json())

# convert the object into a dict
swarm_spec_task_defaults_dict = swarm_spec_task_defaults_instance.to_dict()
# create an instance of SwarmSpecTaskDefaults from a dict
swarm_spec_task_defaults_from_dict = SwarmSpecTaskDefaults.from_dict(swarm_spec_task_defaults_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


