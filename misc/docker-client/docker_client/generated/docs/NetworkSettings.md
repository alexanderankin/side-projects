# NetworkSettings

NetworkSettings exposes the network settings in the API

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**bridge** | **str** | Name of the default bridge interface when dockerd&#39;s --bridge flag is set.  | [optional] 
**sandbox_id** | **str** | SandboxID uniquely represents a container&#39;s network stack. | [optional] 
**hairpin_mode** | **bool** | Indicates if hairpin NAT should be enabled on the virtual interface.  Deprecated: This field is never set and will be removed in a future release.  | [optional] 
**link_local_ipv6_address** | **str** | IPv6 unicast address using the link-local prefix.  Deprecated: This field is never set and will be removed in a future release.  | [optional] 
**link_local_ipv6_prefix_len** | **int** | Prefix length of the IPv6 unicast address.  Deprecated: This field is never set and will be removed in a future release.  | [optional] 
**ports** | **Dict[str, Optional[List[PortBinding]]]** | PortMap describes the mapping of container ports to host ports, using the container&#39;s port-number and protocol as key in the format &#x60;&lt;port&gt;/&lt;protocol&gt;&#x60;, for example, &#x60;80/udp&#x60;.  If a container&#39;s port is mapped for multiple protocols, separate entries are added to the mapping table.  | [optional] 
**sandbox_key** | **str** | SandboxKey is the full path of the netns handle | [optional] 
**secondary_ip_addresses** | [**List[Address]**](Address.md) | Deprecated: This field is never set and will be removed in a future release. | [optional] 
**secondary_ipv6_addresses** | [**List[Address]**](Address.md) | Deprecated: This field is never set and will be removed in a future release. | [optional] 
**endpoint_id** | **str** | EndpointID uniquely represents a service endpoint in a Sandbox.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**gateway** | **str** | Gateway address for the default \&quot;bridge\&quot; network.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**global_ipv6_address** | **str** | Global IPv6 address for the default \&quot;bridge\&quot; network.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**global_ipv6_prefix_len** | **int** | Mask length of the global IPv6 address.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**ip_address** | **str** | IPv4 address for the default \&quot;bridge\&quot; network.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**ip_prefix_len** | **int** | Mask length of the IPv4 address.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**ipv6_gateway** | **str** | IPv6 gateway address for this network.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**mac_address** | **str** | MAC address for the container on the default \&quot;bridge\&quot; network.  &lt;p&gt;&lt;br /&gt;&lt;/p&gt;  &gt; **Deprecated**: This field is only propagated when attached to the &gt; default \&quot;bridge\&quot; network. Use the information from the \&quot;bridge\&quot; &gt; network inside the &#x60;Networks&#x60; map instead, which contains the same &gt; information. This field was deprecated in Docker 1.9 and is scheduled &gt; to be removed in Docker 17.12.0  | [optional] 
**networks** | [**Dict[str, EndpointSettings]**](EndpointSettings.md) | Information about all networks that the container is connected to.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.network_settings import NetworkSettings

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkSettings from a JSON string
network_settings_instance = NetworkSettings.from_json(json)
# print the JSON string representation of the object
print(NetworkSettings.to_json())

# convert the object into a dict
network_settings_dict = network_settings_instance.to_dict()
# create an instance of NetworkSettings from a dict
network_settings_from_dict = NetworkSettings.from_dict(network_settings_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


