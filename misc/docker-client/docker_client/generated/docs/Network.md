# Network


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the network.  | [optional] 
**id** | **str** | ID that uniquely identifies a network on a single machine.  | [optional] 
**created** | **str** | Date and time at which the network was created in [RFC 3339](https://www.ietf.org/rfc/rfc3339.txt) format with nano-seconds.  | [optional] 
**scope** | **str** | The level at which the network exists (e.g. &#x60;swarm&#x60; for cluster-wide or &#x60;local&#x60; for machine level)  | [optional] 
**driver** | **str** | The name of the driver used to create the network (e.g. &#x60;bridge&#x60;, &#x60;overlay&#x60;).  | [optional] 
**enable_ipv6** | **bool** | Whether the network was created with IPv6 enabled.  | [optional] 
**ipam** | [**IPAM**](IPAM.md) |  | [optional] 
**internal** | **bool** | Whether the network is created to only allow internal networking connectivity.  | [optional] [default to False]
**attachable** | **bool** | Whether a global / swarm scope network is manually attachable by regular containers from workers in swarm mode.  | [optional] [default to False]
**ingress** | **bool** | Whether the network is providing the routing-mesh for the swarm cluster.  | [optional] [default to False]
**config_from** | [**ConfigReference**](ConfigReference.md) |  | [optional] 
**config_only** | **bool** | Whether the network is a config-only network. Config-only networks are placeholder networks for network configurations to be used by other networks. Config-only networks cannot be used directly to run containers or services.  | [optional] [default to False]
**containers** | [**Dict[str, NetworkContainer]**](NetworkContainer.md) | Contains endpoints attached to the network.  | [optional] 
**options** | **Dict[str, str]** | Network-specific options uses when creating the network.  | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**peers** | [**List[PeerInfo]**](PeerInfo.md) | List of peer nodes for an overlay network. This field is only present for overlay networks, and omitted for other network types.  | [optional] 

## Example

```python
from docker_client.generated.models.network import Network

# TODO update the JSON string below
json = "{}"
# create an instance of Network from a JSON string
network_instance = Network.from_json(json)
# print the JSON string representation of the object
print(Network.to_json())

# convert the object into a dict
network_dict = network_instance.to_dict()
# create an instance of Network from a dict
network_from_dict = Network.from_dict(network_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


