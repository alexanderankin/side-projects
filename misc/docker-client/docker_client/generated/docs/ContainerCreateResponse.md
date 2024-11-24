# ContainerCreateResponse

OK response to ContainerCreate operation

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The ID of the created container | 
**warnings** | **List[str]** | Warnings encountered when creating the container | 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_create_response import ContainerCreateResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerCreateResponse from a JSON string
container_create_response_instance = ContainerCreateResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerCreateResponse.to_json())

# convert the object into a dict
container_create_response_dict = container_create_response_instance.to_dict()
# create an instance of ContainerCreateResponse from a dict
container_create_response_from_dict = ContainerCreateResponse.from_dict(container_create_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


