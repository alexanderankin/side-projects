# FilesystemChange

Change in the container's filesystem. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**path** | **str** | Path to file or directory that has changed.  | 
**kind** | [**ChangeType**](ChangeType.md) |  | 

## Example

```python
from docker_client.generated.models.filesystem_change import FilesystemChange

# TODO update the JSON string below
json = "{}"
# create an instance of FilesystemChange from a JSON string
filesystem_change_instance = FilesystemChange.from_json(json)
# print the JSON string representation of the object
print(FilesystemChange.to_json())

# convert the object into a dict
filesystem_change_dict = filesystem_change_instance.to_dict()
# create an instance of FilesystemChange from a dict
filesystem_change_from_dict = FilesystemChange.from_dict(filesystem_change_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


