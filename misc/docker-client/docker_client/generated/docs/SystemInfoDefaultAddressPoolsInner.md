# SystemInfoDefaultAddressPoolsInner


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**base** | **str** | The network address in CIDR format | [optional] 
**size** | **int** | The network pool size | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.system_info_default_address_pools_inner import SystemInfoDefaultAddressPoolsInner

# TODO update the JSON string below
json = "{}"
# create an instance of SystemInfoDefaultAddressPoolsInner from a JSON string
system_info_default_address_pools_inner_instance = SystemInfoDefaultAddressPoolsInner.from_json(json)
# print the JSON string representation of the object
print(SystemInfoDefaultAddressPoolsInner.to_json())

# convert the object into a dict
system_info_default_address_pools_inner_dict = system_info_default_address_pools_inner_instance.to_dict()
# create an instance of SystemInfoDefaultAddressPoolsInner from a dict
system_info_default_address_pools_inner_from_dict = SystemInfoDefaultAddressPoolsInner.from_dict(system_info_default_address_pools_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


