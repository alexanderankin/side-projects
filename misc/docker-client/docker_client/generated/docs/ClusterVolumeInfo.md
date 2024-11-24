# ClusterVolumeInfo

Information about the global status of the volume. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**capacity_bytes** | **int** | The capacity of the volume in bytes. A value of 0 indicates that the capacity is unknown.  | [optional] 
**volume_context** | **Dict[str, str]** | A map of strings to strings returned from the storage plugin when the volume is created.  | [optional] 
**volume_id** | **str** | The ID of the volume as returned by the CSI storage plugin. This is distinct from the volume&#39;s ID as provided by Docker. This ID is never used by the user when communicating with Docker to refer to this volume. If the ID is blank, then the Volume has not been successfully created in the plugin yet.  | [optional] 
**accessible_topology** | **List[Dict[str, str]]** | The topology this volume is actually accessible from.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.cluster_volume_info import ClusterVolumeInfo

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeInfo from a JSON string
cluster_volume_info_instance = ClusterVolumeInfo.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeInfo.to_json())

# convert the object into a dict
cluster_volume_info_dict = cluster_volume_info_instance.to_dict()
# create an instance of ClusterVolumeInfo from a dict
cluster_volume_info_from_dict = ClusterVolumeInfo.from_dict(cluster_volume_info_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


