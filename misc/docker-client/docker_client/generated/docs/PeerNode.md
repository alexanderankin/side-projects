# PeerNode

Represents a peer-node in the swarm

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**node_id** | **str** | Unique identifier of for this node in the swarm. | [optional] 
**addr** | **str** | IP address and ports at which this node can be reached.  | [optional] 

## Example

```python
from docker_client.generated.models.peer_node import PeerNode

# TODO update the JSON string below
json = "{}"
# create an instance of PeerNode from a JSON string
peer_node_instance = PeerNode.from_json(json)
# print the JSON string representation of the object
print(PeerNode.to_json())

# convert the object into a dict
peer_node_dict = peer_node_instance.to_dict()
# create an instance of PeerNode from a dict
peer_node_from_dict = PeerNode.from_dict(peer_node_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


