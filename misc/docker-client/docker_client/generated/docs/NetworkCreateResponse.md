# NetworkCreateResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of the created network. | [optional] 
**warning** | **str** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.network_create_response import NetworkCreateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of NetworkCreateResponse from a JSON string
network_create_response_instance = NetworkCreateResponse.from_json(json)
# print the JSON string representation of the object
print(NetworkCreateResponse.to_json())

# convert the object into a dict
network_create_response_dict = network_create_response_instance.to_dict()
# create an instance of NetworkCreateResponse from a dict
network_create_response_from_dict = NetworkCreateResponse.from_dict(network_create_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


