# MountTmpfsOptions

Optional configuration for the `tmpfs` type.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**size_bytes** | **int** | The size for the tmpfs mount in bytes. | [optional] 
**mode** | **int** | The permission mode for the tmpfs mount in an integer. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.mount_tmpfs_options import MountTmpfsOptions

# TODO update the JSON string below
json = "{}"
# create an instance of MountTmpfsOptions from a JSON string
mount_tmpfs_options_instance = MountTmpfsOptions.from_json(json)
# print the JSON string representation of the object
print(MountTmpfsOptions.to_json())

# convert the object into a dict
mount_tmpfs_options_dict = mount_tmpfs_options_instance.to_dict()
# create an instance of MountTmpfsOptions from a dict
mount_tmpfs_options_from_dict = MountTmpfsOptions.from_dict(mount_tmpfs_options_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


