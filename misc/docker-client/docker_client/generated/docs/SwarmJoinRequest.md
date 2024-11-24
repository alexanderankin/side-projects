# SwarmJoinRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**listen_addr** | **str** | Listen address used for inter-manager communication if the node gets promoted to manager, as well as determining the networking interface used for the VXLAN Tunnel Endpoint (VTEP).  | [optional] 
**advertise_addr** | **str** | Externally reachable address advertised to other nodes. This can either be an address/port combination in the form &#x60;192.168.1.1:4567&#x60;, or an interface followed by a port number, like &#x60;eth0:4567&#x60;. If the port number is omitted, the port number from the listen address is used. If &#x60;AdvertiseAddr&#x60; is not specified, it will be automatically detected when possible.  | [optional] 
**data_path_addr** | **str** | Address or interface to use for data path traffic (format: &#x60;&lt;ip|interface&gt;&#x60;), for example,  &#x60;192.168.1.1&#x60;, or an interface, like &#x60;eth0&#x60;. If &#x60;DataPathAddr&#x60; is unspecified, the same address as &#x60;AdvertiseAddr&#x60; is used.  The &#x60;DataPathAddr&#x60; specifies the address that global scope network drivers will publish towards other nodes in order to reach the containers running on this node. Using this parameter it is possible to separate the container data traffic from the management traffic of the cluster.  | [optional] 
**remote_addrs** | **List[str]** | Addresses of manager nodes already participating in the swarm.  | [optional] 
**join_token** | **str** | Secret token for joining this swarm. | [optional] 

## Example

```python
from docker_client.generated.models.swarm_join_request import SwarmJoinRequest

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmJoinRequest from a JSON string
swarm_join_request_instance = SwarmJoinRequest.from_json(json)
# print the JSON string representation of the object
print(SwarmJoinRequest.to_json())

# convert the object into a dict
swarm_join_request_dict = swarm_join_request_instance.to_dict()
# create an instance of SwarmJoinRequest from a dict
swarm_join_request_from_dict = SwarmJoinRequest.from_dict(swarm_join_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


