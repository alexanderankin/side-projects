# EndpointSettings

Configuration for a network endpoint.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ipam_config** | [**EndpointIPAMConfig**](EndpointIPAMConfig.md) |  | [optional] 
**links** | **List[str]** |  | [optional] 
**mac_address** | **str** | MAC address for the endpoint on this network. The network driver might ignore this parameter.  | [optional] 
**aliases** | **List[str]** |  | [optional] 
**network_id** | **str** | Unique ID of the network.  | [optional] 
**endpoint_id** | **str** | Unique ID for the service endpoint in a Sandbox.  | [optional] 
**gateway** | **str** | Gateway address for this network.  | [optional] 
**ip_address** | **str** | IPv4 address.  | [optional] 
**ip_prefix_len** | **int** | Mask length of the IPv4 address.  | [optional] 
**ipv6_gateway** | **str** | IPv6 gateway address.  | [optional] 
**global_ipv6_address** | **str** | Global IPv6 address.  | [optional] 
**global_ipv6_prefix_len** | **int** | Mask length of the global IPv6 address.  | [optional] 
**driver_opts** | **Dict[str, str]** | DriverOpts is a mapping of driver options and values. These options are passed directly to the driver and are driver specific.  | [optional] 
**dns_names** | **List[str]** | List of all DNS names an endpoint has on a specific network. This list is based on the container name, network aliases, container short ID, and hostname.  These DNS names are non-fully qualified but can contain several dots. You can get fully qualified DNS names by appending &#x60;.&lt;network-name&gt;&#x60;. For instance, if container name is &#x60;my.ctr&#x60; and the network is named &#x60;testnet&#x60;, &#x60;DNSNames&#x60; will contain &#x60;my.ctr&#x60; and the FQDN will be &#x60;my.ctr.testnet&#x60;.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.endpoint_settings import EndpointSettings

# TODO update the JSON string below
json = "{}"
# create an instance of EndpointSettings from a JSON string
endpoint_settings_instance = EndpointSettings.from_json(json)
# print the JSON string representation of the object
print(EndpointSettings.to_json())

# convert the object into a dict
endpoint_settings_dict = endpoint_settings_instance.to_dict()
# create an instance of EndpointSettings from a dict
endpoint_settings_from_dict = EndpointSettings.from_dict(endpoint_settings_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


