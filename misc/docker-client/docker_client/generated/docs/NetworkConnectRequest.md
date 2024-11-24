# NetworkConnectRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**container** | **str** | The ID or name of the container to connect to the network. | [optional] 
**endpoint_config** | [**EndpointSettings**](EndpointSettings.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.network_connect_request import NetworkConnectRequest

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkConnectRequest from a JSON string
network_connect_request_instance = NetworkConnectRequest.from_json(json)
# print the JSON string representation of the object
print(NetworkConnectRequest.to_json())

# convert the object into a dict
network_connect_request_dict = network_connect_request_instance.to_dict()
# create an instance of NetworkConnectRequest from a dict
network_connect_request_from_dict = NetworkConnectRequest.from_dict(network_connect_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


