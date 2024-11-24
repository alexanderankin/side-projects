# IdResponse

Response to an API call that returns just an Id

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The id of the newly created object. | 

## Example

```python
from docker_client.generated.models.id_response import IdResponse

# TODO update the JSON string below
json = "{}"
# create an instance of IdResponse from a JSON string
id_response_instance = IdResponse.from_json(json)
# print the JSON string representation of the object
print(IdResponse.to_json())

# convert the object into a dict
id_response_dict = id_response_instance.to_dict()
# create an instance of IdResponse from a dict
id_response_from_dict = IdResponse.from_dict(id_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


