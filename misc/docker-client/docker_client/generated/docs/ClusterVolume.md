# ClusterVolume

Options and information specific to, and only present on, Swarm CSI cluster volumes. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** | The Swarm ID of this volume. Because cluster volumes are Swarm objects, they have an ID, unlike non-cluster volumes. This ID can be used to refer to the Volume instead of the name.  | [optional] 
**version** | [**ObjectVersion**](ObjectVersion.md) |  | [optional] 
**created_at** | **str** |  | [optional] 
**updated_at** | **str** |  | [optional] 
**spec** | [**ClusterVolumeSpec**](ClusterVolumeSpec.md) |  | [optional] 
**info** | [**ClusterVolumeInfo**](ClusterVolumeInfo.md) |  | [optional] 
**publish_status** | [**List[ClusterVolumePublishStatusInner]**](ClusterVolumePublishStatusInner.md) | The status of the volume as it pertains to its publishing and use on specific nodes  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.cluster_volume import ClusterVolume

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolume from a JSON string
cluster_volume_instance = ClusterVolume.from_json(json)
# print the JSON string representation of the object
print(ClusterVolume.to_json())

# convert the object into a dict
cluster_volume_dict = cluster_volume_instance.to_dict()
# create an instance of ClusterVolume from a dict
cluster_volume_from_dict = ClusterVolume.from_dict(cluster_volume_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


