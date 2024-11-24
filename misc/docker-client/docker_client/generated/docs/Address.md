# Address

Address represents an IPv4 or IPv6 IP address.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**addr** | **str** | IP address. | [optional] 
**prefix_len** | **int** | Mask length of the IP address. | [optional] 

## Example

```python
from docker_client.generated.models.address import Address

# TODO update the JSON string below
json = "{}"
# create an instance of Address from a JSON string
address_instance = Address.from_json(json)
# print the JSON string representation of the object
print(Address.to_json())

# convert the object into a dict
address_dict = address_instance.to_dict()
# create an instance of Address from a dict
address_from_dict = Address.from_dict(address_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


