# SwarmSpecDispatcher

Dispatcher configuration.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**heartbeat_period** | **int** | The delay for an agent to send a heartbeat to the dispatcher.  | [optional] 

## Example

```python
from docker_client.generated.models.swarm_spec_dispatcher import SwarmSpecDispatcher

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmSpecDispatcher from a JSON string
swarm_spec_dispatcher_instance = SwarmSpecDispatcher.from_json(json)
# print the JSON string representation of the object
print(SwarmSpecDispatcher.to_json())

# convert the object into a dict
swarm_spec_dispatcher_dict = swarm_spec_dispatcher_instance.to_dict()
# create an instance of SwarmSpecDispatcher from a dict
swarm_spec_dispatcher_from_dict = SwarmSpecDispatcher.from_dict(swarm_spec_dispatcher_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


