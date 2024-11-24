# ImagePruneResponse


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**images_deleted** | [**List[ImageDeleteResponseItem]**](ImageDeleteResponseItem.md) | Images that were deleted | [optional] 
**space_reclaimed** | **int** | Disk space reclaimed in bytes | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.image_prune_response import ImagePruneResponse

# TODO update the JSON string below
json = "{}"
# create an instance of ImagePruneResponse from a JSON string
image_prune_response_instance = ImagePruneResponse.from_json(json)
# print the JSON string representation of the object
print(ImagePruneResponse.to_json())

# convert the object into a dict
image_prune_response_dict = image_prune_response_instance.to_dict()
# create an instance of ImagePruneResponse from a dict
image_prune_response_from_dict = ImagePruneResponse.from_dict(image_prune_response_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


