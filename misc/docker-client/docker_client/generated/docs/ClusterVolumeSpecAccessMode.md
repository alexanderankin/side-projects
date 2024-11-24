# ClusterVolumeSpecAccessMode

Defines how the volume is used by tasks. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**scope** | **str** | The set of nodes this volume can be used on at one time. - &#x60;single&#x60; The volume may only be scheduled to one node at a time. - &#x60;multi&#x60; the volume may be scheduled to any supported number of nodes at a time.  | [optional] [default to 'single']
**sharing** | **str** | The number and way that different tasks can use this volume at one time. - &#x60;none&#x60; The volume may only be used by one task at a time. - &#x60;readonly&#x60; The volume may be used by any number of tasks, but they all must mount the volume as readonly - &#x60;onewriter&#x60; The volume may be used by any number of tasks, but only one may mount it as read/write. - &#x60;all&#x60; The volume may have any number of readers and writers.  | [optional] [default to 'none']
**mount_volume** | **object** | Options for using this volume as a Mount-type volume.      Either MountVolume or BlockVolume, but not both, must be     present.   properties:     FsType:       type: \&quot;string\&quot;       description: |         Specifies the filesystem type for the mount volume.         Optional.     MountFlags:       type: \&quot;array\&quot;       description: |         Flags to pass when mounting the volume. Optional.       items:         type: \&quot;string\&quot; BlockVolume:   type: \&quot;object\&quot;   description: |     Options for using this volume as a Block-type volume.     Intentionally empty.  | [optional] 
**secrets** | [**List[ClusterVolumeSpecAccessModeSecretsInner]**](ClusterVolumeSpecAccessModeSecretsInner.md) | Swarm Secrets that are passed to the CSI storage plugin when operating on this volume.  | [optional] 
**accessibility_requirements** | [**ClusterVolumeSpecAccessModeAccessibilityRequirements**](ClusterVolumeSpecAccessModeAccessibilityRequirements.md) |  | [optional] 
**capacity_range** | [**ClusterVolumeSpecAccessModeCapacityRange**](ClusterVolumeSpecAccessModeCapacityRange.md) |  | [optional] 
**availability** | **str** | The availability of the volume for use in tasks. - &#x60;active&#x60; The volume is fully available for scheduling on the cluster - &#x60;pause&#x60; No new workloads should use the volume, but existing workloads are not stopped. - &#x60;drain&#x60; All workloads using this volume should be stopped and rescheduled, and no new ones should be started.  | [optional] [default to 'active']

## Example

```python
from docker_client.generated.models.cluster_volume_spec_access_mode import ClusterVolumeSpecAccessMode

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeSpecAccessMode from a JSON string
cluster_volume_spec_access_mode_instance = ClusterVolumeSpecAccessMode.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeSpecAccessMode.to_json())

# convert the object into a dict
cluster_volume_spec_access_mode_dict = cluster_volume_spec_access_mode_instance.to_dict()
# create an instance of ClusterVolumeSpecAccessMode from a dict
cluster_volume_spec_access_mode_from_dict = ClusterVolumeSpecAccessMode.from_dict(cluster_volume_spec_access_mode_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


