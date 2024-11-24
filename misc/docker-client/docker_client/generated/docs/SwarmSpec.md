# SwarmSpec

User modifiable swarm configuration.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the swarm. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**orchestration** | [**SwarmSpecOrchestration**](SwarmSpecOrchestration.md) |  | [optional] 
**raft** | [**SwarmSpecRaft**](SwarmSpecRaft.md) |  | [optional] 
**dispatcher** | [**SwarmSpecDispatcher**](SwarmSpecDispatcher.md) |  | [optional] 
**ca_config** | [**SwarmSpecCAConfig**](SwarmSpecCAConfig.md) |  | [optional] 
**encryption_config** | [**SwarmSpecEncryptionConfig**](SwarmSpecEncryptionConfig.md) |  | [optional] 
**task_defaults** | [**SwarmSpecTaskDefaults**](SwarmSpecTaskDefaults.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.swarm_spec import SwarmSpec

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpec from a JSON string
swarm_spec_instance = SwarmSpec.from_json(json)
# print the JSON string representation of the object
print(SwarmSpec.to_json())

# convert the object into a dict
swarm_spec_dict = swarm_spec_instance.to_dict()
# create an instance of SwarmSpec from a dict
swarm_spec_from_dict = SwarmSpec.from_dict(swarm_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


