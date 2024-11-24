# VolumeCreateOptions

Volume configuration

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** | The new volume&#39;s name. If not specified, Docker generates a name.  | [optional] 
**driver** | **str** | Name of the volume driver to use. | [optional] [default to 'local']
**driver_opts** | **Dict[str, str]** | A mapping of driver options and values. These options are passed directly to the driver and are driver specific.  | [optional] 
**labels** | **Dict[str, str]** | User-defined key/value metadata. | [optional] 
**cluster_volume_spec** | [**ClusterVolumeSpec**](ClusterVolumeSpec.md) |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.volume_create_options import VolumeCreateOptions

# TODO update the JSON string below
json = "{}"
# create an instance of VolumeCreateOptions from a JSON string
volume_create_options_instance = VolumeCreateOptions.from_json(json)
# print the JSON string representation of the object
print(VolumeCreateOptions.to_json())

# convert the object into a dict
volume_create_options_dict = volume_create_options_instance.to_dict()
# create an instance of VolumeCreateOptions from a dict
volume_create_options_from_dict = VolumeCreateOptions.from_dict(volume_create_options_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


