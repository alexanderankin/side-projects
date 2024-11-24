# ClusterVolumeSpec

Cluster-specific options used to create the volume. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**group** | **str** | Group defines the volume group of this volume. Volumes belonging to the same group can be referred to by group name when creating Services.  Referring to a volume by group instructs Swarm to treat volumes in that group interchangeably for the purpose of scheduling. Volumes with an empty string for a group technically all belong to the same, emptystring group.  | [optional] 
**access_mode** | [**ClusterVolumeSpecAccessMode**](ClusterVolumeSpecAccessMode.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.cluster_volume_spec import ClusterVolumeSpec

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeSpec from a JSON string
cluster_volume_spec_instance = ClusterVolumeSpec.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeSpec.to_json())

# convert the object into a dict
cluster_volume_spec_dict = cluster_volume_spec_instance.to_dict()
# create an instance of ClusterVolumeSpec from a dict
cluster_volume_spec_from_dict = ClusterVolumeSpec.from_dict(cluster_volume_spec_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


