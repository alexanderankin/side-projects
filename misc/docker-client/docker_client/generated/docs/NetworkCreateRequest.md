# NetworkCreateRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The network&#39;s name. | 
**check_duplicate** | **bool** | Deprecated: CheckDuplicate is now always enabled.  | [optional] 
**driver** | **str** | Name of the network driver plugin to use. | [optional] [default to 'bridge']
**scope** | **str** | The level at which the network exists (e.g. &#x60;swarm&#x60; for cluster-wide or &#x60;local&#x60; for machine level).  | [optional] 
**internal** | **bool** | Restrict external access to the network. | [optional] 
**attachable** | **bool** | Globally scoped network is manually attachable by regular containers from workers in swarm mode.  | [optional] 
**ingress** | **bool** | Ingress network is the network which provides the routing-mesh in swarm mode.  | [optional] 
**config_only** | **bool** | Creates a config-only network. Config-only networks are placeholder networks for network configurations to be used by other networks. Config-only networks cannot be used directly to run containers or services.  | [optional] [default to False]
**config_from** | [**ConfigReference**](ConfigReference.md) |  | [optional] 
**ipam** | [**IPAM**](IPAM.md) |  | [optional] 
**enable_ipv6** | **bool** | Enable IPv6 on the network. | [optional] 
**options** | **Dict[str, str]** | Network specific options to be used by the drivers. | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 

## Example

```python
from docker_client.generated.models.network_create_request import NetworkCreateRequest

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkCreateRequest from a JSON string
network_create_request_instance = NetworkCreateRequest.from_json(json)
# print the JSON string representation of the object
print(NetworkCreateRequest.to_json())

# convert the object into a dict
network_create_request_dict = network_create_request_instance.to_dict()
# create an instance of NetworkCreateRequest from a dict
network_create_request_from_dict = NetworkCreateRequest.from_dict(network_create_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


