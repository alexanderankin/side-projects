# ClusterVolumeSpecAccessModeCapacityRange

The desired capacity that the volume should be created with. If empty, the plugin will decide the capacity. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**required_bytes** | **int** | The volume must be at least this big. The value of 0 indicates an unspecified minimum  | [optional] 
**limit_bytes** | **int** | The volume must not be bigger than this. The value of 0 indicates an unspecified maximum.  | [optional] 

## Example

```python
from docker_client.generated.models.cluster_volume_spec_access_mode_capacity_range import ClusterVolumeSpecAccessModeCapacityRange

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeSpecAccessModeCapacityRange from a JSON string
cluster_volume_spec_access_mode_capacity_range_instance = ClusterVolumeSpecAccessModeCapacityRange.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeSpecAccessModeCapacityRange.to_json())

# convert the object into a dict
cluster_volume_spec_access_mode_capacity_range_dict = cluster_volume_spec_access_mode_capacity_range_instance.to_dict()
# create an instance of ClusterVolumeSpecAccessModeCapacityRange from a dict
cluster_volume_spec_access_mode_capacity_range_from_dict = ClusterVolumeSpecAccessModeCapacityRange.from_dict(cluster_volume_spec_access_mode_capacity_range_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


