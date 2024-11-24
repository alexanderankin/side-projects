# VolumePruneResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**volumes_deleted** | **List[str]** | Volumes that were deleted | [optional] 
**space_reclaimed** | **int** | Disk space reclaimed in bytes | [optional] 

## Example

```python
from docker_client.generated.models.volume_prune_response import VolumePruneResponse

# TODO update the JSON string below
json = "{}"
# create an instance of VolumePruneResponse from a JSON string
volume_prune_response_instance = VolumePruneResponse.from_json(json)
# print the JSON string representation of the object
print(VolumePruneResponse.to_json())

# convert the object into a dict
volume_prune_response_dict = volume_prune_response_instance.to_dict()
# create an instance of VolumePruneResponse from a dict
volume_prune_response_from_dict = VolumePruneResponse.from_dict(volume_prune_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


