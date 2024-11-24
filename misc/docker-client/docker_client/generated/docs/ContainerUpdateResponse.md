# ContainerUpdateResponse

OK response to ContainerUpdate operation

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**warnings** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.models.container_update_response import ContainerUpdateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerUpdateResponse from a JSON string
container_update_response_instance = ContainerUpdateResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerUpdateResponse.to_json())

# convert the object into a dict
container_update_response_dict = container_update_response_instance.to_dict()
# create an instance of ContainerUpdateResponse from a dict
container_update_response_from_dict = ContainerUpdateResponse.from_dict(container_update_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


