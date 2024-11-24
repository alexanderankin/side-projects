# ImageInspectRootFS

Information about the image's RootFS, including the layer IDs. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** |  | 
**layers** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.models.image_inspect_root_fs import ImageInspectRootFS

# TODO update the JSON string below
json = "{}"
# create an instance of ImageInspectRootFS from a JSON string
image_inspect_root_fs_instance = ImageInspectRootFS.from_json(json)
# print the JSON string representation of the object
print(ImageInspectRootFS.to_json())

# convert the object into a dict
image_inspect_root_fs_dict = image_inspect_root_fs_instance.to_dict()
# create an instance of ImageInspectRootFS from a dict
image_inspect_root_fs_from_dict = ImageInspectRootFS.from_dict(image_inspect_root_fs_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


