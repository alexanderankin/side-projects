# MountBindOptions

Optional configuration for the `bind` type.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**propagation** | **str** | A propagation mode with the value &#x60;[r]private&#x60;, &#x60;[r]shared&#x60;, or &#x60;[r]slave&#x60;. | [optional] 
**non_recursive** | **bool** | Disable recursive bind mount. | [optional] [default to False]
**create_mountpoint** | **bool** | Create mount point on host if missing | [optional] [default to False]
**read_only_non_recursive** | **bool** | Make the mount non-recursively read-only, but still leave the mount recursive (unless NonRecursive is set to &#x60;true&#x60; in conjunction).  Added in v1.44, before that version all read-only mounts were non-recursive by default. To match the previous behaviour this will default to &#x60;true&#x60; for clients on versions prior to v1.44.  | [optional] [default to False]
**read_only_force_recursive** | **bool** | Raise an error if the mount cannot be made recursively read-only. | [optional] [default to False]

## Example

```python
from docker_client.generated.docker_client.generated.models.mount_bind_options import MountBindOptions

# TODO update the JSON string below
json = "{}"
# create an instance of MountBindOptions from a JSON string
mount_bind_options_instance = MountBindOptions.from_json(json)
# print the JSON string representation of the object
print(MountBindOptions.to_json())

# convert the object into a dict
mount_bind_options_dict = mount_bind_options_instance.to_dict()
# create an instance of MountBindOptions from a dict
mount_bind_options_from_dict = MountBindOptions.from_dict(mount_bind_options_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


