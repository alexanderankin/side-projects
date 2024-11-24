# MountVolumeOptionsDriverConfig

Map of driver specific options

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | Name of the driver to use to create the volume. | [optional] 
**options** | **Dict[str, str]** | key/value map of driver specific options. | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.mount_volume_options_driver_config import MountVolumeOptionsDriverConfig

# TODO update the JSON string below
json = "{}"
# create an instance of MountVolumeOptionsDriverConfig from a JSON string
mount_volume_options_driver_config_instance = MountVolumeOptionsDriverConfig.from_json(json)
# print the JSON string representation of the object
print(MountVolumeOptionsDriverConfig.to_json())

# convert the object into a dict
mount_volume_options_driver_config_dict = mount_volume_options_driver_config_instance.to_dict()
# create an instance of MountVolumeOptionsDriverConfig from a dict
mount_volume_options_driver_config_from_dict = MountVolumeOptionsDriverConfig.from_dict(mount_volume_options_driver_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


