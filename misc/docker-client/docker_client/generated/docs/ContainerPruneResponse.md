# ContainerPruneResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**containers_deleted** | **List[str]** | Container IDs that were deleted | [optional] 
**space_reclaimed** | **int** | Disk space reclaimed in bytes | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.container_prune_response import ContainerPruneResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ContainerPruneResponse from a JSON string
container_prune_response_instance = ContainerPruneResponse.from_json(json)
# print the JSON string representation of the object
print(ContainerPruneResponse.to_json())

# convert the object into a dict
container_prune_response_dict = container_prune_response_instance.to_dict()
# create an instance of ContainerPruneResponse from a dict
container_prune_response_from_dict = ContainerPruneResponse.from_dict(container_prune_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


