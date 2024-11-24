# EndpointIPAMConfig

EndpointIPAMConfig represents an endpoint's IPAM configuration. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**ipv4_address** | **str** |  | [optional] 
**ipv6_address** | **str** |  | [optional] 
**link_local_ips** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.endpoint_ipam_config import EndpointIPAMConfig

# TODO update the JSON string below
json = "{}"
# create an instance of EndpointIPAMConfig from a JSON string
endpoint_ipam_config_instance = EndpointIPAMConfig.from_json(json)
# print the JSON string representation of the object
print(EndpointIPAMConfig.to_json())

# convert the object into a dict
endpoint_ipam_config_dict = endpoint_ipam_config_instance.to_dict()
# create an instance of EndpointIPAMConfig from a dict
endpoint_ipam_config_from_dict = EndpointIPAMConfig.from_dict(endpoint_ipam_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


