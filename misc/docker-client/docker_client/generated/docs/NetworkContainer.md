# NetworkContainer


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | [optional] 
**endpoint_id** | **str** |  | [optional] 
**mac_address** | **str** |  | [optional] 
**ipv4_address** | **str** |  | [optional] 
**ipv6_address** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.network_container import NetworkContainer

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkContainer from a JSON string
network_container_instance = NetworkContainer.from_json(json)
# print the JSON string representation of the object
print(NetworkContainer.to_json())

# convert the object into a dict
network_container_dict = network_container_instance.to_dict()
# create an instance of NetworkContainer from a dict
network_container_from_dict = NetworkContainer.from_dict(network_container_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


