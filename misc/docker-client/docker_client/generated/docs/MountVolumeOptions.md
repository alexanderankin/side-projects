# MountVolumeOptions

Optional configuration for the `volume` type.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**no_copy** | **bool** | Populate volume with data from the target. | [optional] [default to False]
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**driver_config** | [**MountVolumeOptionsDriverConfig**](MountVolumeOptionsDriverConfig.md) |  | [optional] 
**subpath** | **str** | Source path inside the volume. Must be relative without any back traversals. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.mount_volume_options import MountVolumeOptions

# TODO update the JSON string below
json = "{}"
# create an instance of MountVolumeOptions from a JSON string
mount_volume_options_instance = MountVolumeOptions.from_json(json)
# print the JSON string representation of the object
print(MountVolumeOptions.to_json())

# convert the object into a dict
mount_volume_options_dict = mount_volume_options_instance.to_dict()
# create an instance of MountVolumeOptions from a dict
mount_volume_options_from_dict = MountVolumeOptions.from_dict(mount_volume_options_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


