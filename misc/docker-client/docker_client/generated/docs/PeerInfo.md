# PeerInfo

PeerInfo represents one peer of an overlay network. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | ID of the peer-node in the Swarm cluster. | [optional] 
**ip** | **str** | IP-address of the peer-node in the Swarm cluster. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.peer_info import PeerInfo

# TODO update the JSON string below
json = "{}"
# create an instance of PeerInfo from a JSON string
peer_info_instance = PeerInfo.from_json(json)
# print the JSON string representation of the object
print(PeerInfo.to_json())

# convert the object into a dict
peer_info_dict = peer_info_instance.to_dict()
# create an instance of PeerInfo from a dict
peer_info_from_dict = PeerInfo.from_dict(peer_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


