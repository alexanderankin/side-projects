# BuildPruneResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**caches_deleted** | **List[str]** |  | [optional] 
**space_reclaimed** | **int** | Disk space reclaimed in bytes | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.build_prune_response import BuildPruneResponse

# TODO update the JSON string below
json = "{}"
# create an instance of BuildPruneResponse from a JSON string
build_prune_response_instance = BuildPruneResponse.from_json(json)
# print the JSON string representation of the object
print(BuildPruneResponse.to_json())

# convert the object into a dict
build_prune_response_dict = build_prune_response_instance.to_dict()
# create an instance of BuildPruneResponse from a dict
build_prune_response_from_dict = BuildPruneResponse.from_dict(build_prune_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


