# ContainerTopResponse

OK response to ContainerTop operation

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**titles** | **List[str]** | The ps column titles | [optional] 
**processes** | **List[List[str]]** | Each process running in the container, where each is process is an array of values corresponding to the titles.  | [optional] 

## Example

```python
from docker_client.generated.models.container_top_response import ContainerTopResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerTopResponse from a JSON string
container_top_response_instance = ContainerTopResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerTopResponse.to_json())

# convert the object into a dict
container_top_response_dict = container_top_response_instance.to_dict()
# create an instance of ContainerTopResponse from a dict
container_top_response_from_dict = ContainerTopResponse.from_dict(container_top_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


