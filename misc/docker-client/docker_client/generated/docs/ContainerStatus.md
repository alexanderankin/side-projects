# ContainerStatus

represents the status of a container.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**container_id** | **str** |  | [optional] 
**pid** | **int** |  | [optional] 
**exit_code** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_status import ContainerStatus

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerStatus from a JSON string
container_status_instance = ContainerStatus.from_json(json)
# print the JSON string representation of the object
print(ContainerStatus.to_json())

# convert the object into a dict
container_status_dict = container_status_instance.to_dict()
# create an instance of ContainerStatus from a dict
container_status_from_dict = ContainerStatus.from_dict(container_status_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


