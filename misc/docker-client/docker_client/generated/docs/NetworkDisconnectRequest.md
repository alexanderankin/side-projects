# NetworkDisconnectRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**container** | **str** | The ID or name of the container to disconnect from the network.  | [optional] 
**force** | **bool** | Force the container to disconnect from the network.  | [optional] 

## Example

```python
from docker_client.generated.models.network_disconnect_request import NetworkDisconnectRequest

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkDisconnectRequest from a JSON string
network_disconnect_request_instance = NetworkDisconnectRequest.from_json(json)
# print the JSON string representation of the object
print(NetworkDisconnectRequest.to_json())

# convert the object into a dict
network_disconnect_request_dict = network_disconnect_request_instance.to_dict()
# create an instance of NetworkDisconnectRequest from a dict
network_disconnect_request_from_dict = NetworkDisconnectRequest.from_dict(network_disconnect_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


