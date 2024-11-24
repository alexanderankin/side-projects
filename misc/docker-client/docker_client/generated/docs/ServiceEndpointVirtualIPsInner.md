# ServiceEndpointVirtualIPsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**network_id** | **str** |  | [optional] 
**addr** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.models.service_endpoint_virtual_ips_inner import ServiceEndpointVirtualIPsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ServiceEndpointVirtualIPsInner from a JSON string
service_endpoint_virtual_ips_inner_instance = ServiceEndpointVirtualIPsInner.from_json(json)
# print the JSON string representation of the object
print(ServiceEndpointVirtualIPsInner.to_json())

# convert the object into a dict
service_endpoint_virtual_ips_inner_dict = service_endpoint_virtual_ips_inner_instance.to_dict()
# create an instance of ServiceEndpointVirtualIPsInner from a dict
service_endpoint_virtual_ips_inner_from_dict = ServiceEndpointVirtualIPsInner.from_dict(service_endpoint_virtual_ips_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


