# SwarmInitRequest


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**listen_addr** | **str** | Listen address used for inter-manager communication, as well as determining the networking interface used for the VXLAN Tunnel Endpoint (VTEP). This can either be an address/port combination in the form &#x60;192.168.1.1:4567&#x60;, or an interface followed by a port number, like &#x60;eth0:4567&#x60;. If the port number is omitted, the default swarm listening port is used.  | [optional] 
**advertise_addr** | **str** | Externally reachable address advertised to other nodes. This can either be an address/port combination in the form &#x60;192.168.1.1:4567&#x60;, or an interface followed by a port number, like &#x60;eth0:4567&#x60;. If the port number is omitted, the port number from the listen address is used. If &#x60;AdvertiseAddr&#x60; is not specified, it will be automatically detected when possible.  | [optional] 
**data_path_addr** | **str** | Address or interface to use for data path traffic (format: &#x60;&lt;ip|interface&gt;&#x60;), for example,  &#x60;192.168.1.1&#x60;, or an interface, like &#x60;eth0&#x60;. If &#x60;DataPathAddr&#x60; is unspecified, the same address as &#x60;AdvertiseAddr&#x60; is used.  The &#x60;DataPathAddr&#x60; specifies the address that global scope network drivers will publish towards other  nodes in order to reach the containers running on this node. Using this parameter it is possible to separate the container data traffic from the management traffic of the cluster.  | [optional] 
**data_path_port** | **int** | DataPathPort specifies the data path port number for data traffic. Acceptable port range is 1024 to 49151. if no port is set or is set to 0, default port 4789 will be used.  | [optional] 
**default_addr_pool** | **List[str]** | Default Address Pool specifies default subnet pools for global scope networks.  | [optional] 
**force_new_cluster** | **bool** | Force creation of a new swarm. | [optional] 
**subnet_size** | **int** | SubnetSize specifies the subnet size of the networks created from the default subnet pool.  | [optional] 
**spec** | [**SwarmSpec**](SwarmSpec.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.swarm_init_request import SwarmInitRequest

# TODO update the JSON string below
json = "{}"
# create an instance of SwarmInitRequest from a JSON string
swarm_init_request_instance = SwarmInitRequest.from_json(json)
# print the JSON string representation of the object
print(SwarmInitRequest.to_json())

# convert the object into a dict
swarm_init_request_dict = swarm_init_request_instance.to_dict()
# create an instance of SwarmInitRequest from a dict
swarm_init_request_from_dict = SwarmInitRequest.from_dict(swarm_init_request_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


