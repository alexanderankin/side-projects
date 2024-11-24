# SwarmSpecOrchestration

Orchestration configuration.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**task_history_retention_limit** | **int** | The number of historic tasks to keep per instance or node. If negative, never remove completed or failed tasks.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_spec_orchestration import SwarmSpecOrchestration

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecOrchestration from a JSON string
swarm_spec_orchestration_instance = SwarmSpecOrchestration.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecOrchestration.to_json())

# convert the object into a dict
swarm_spec_orchestration_dict = swarm_spec_orchestration_instance.to_dict()
# create an instance of SwarmSpecOrchestration from a dict
swarm_spec_orchestration_from_dict = SwarmSpecOrchestration.from_dict(swarm_spec_orchestration_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


