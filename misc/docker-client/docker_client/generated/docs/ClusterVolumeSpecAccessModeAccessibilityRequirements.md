# ClusterVolumeSpecAccessModeAccessibilityRequirements

Requirements for the accessible topology of the volume. These fields are optional. For an in-depth description of what these fields mean, see the CSI specification. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**requisite** | **List[Dict[str, str]]** | A list of required topologies, at least one of which the volume must be accessible from.  | [optional] 
**preferred** | **List[Dict[str, str]]** | A list of topologies that the volume should attempt to be provisioned in.  | [optional] 

## Example

```python
from docker_client.generated.models.cluster_volume_spec_access_mode_accessibility_requirements import ClusterVolumeSpecAccessModeAccessibilityRequirements

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeSpecAccessModeAccessibilityRequirements from a JSON string
cluster_volume_spec_access_mode_accessibility_requirements_instance = ClusterVolumeSpecAccessModeAccessibilityRequirements.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeSpecAccessModeAccessibilityRequirements.to_json())

# convert the object into a dict
cluster_volume_spec_access_mode_accessibility_requirements_dict = cluster_volume_spec_access_mode_accessibility_requirements_instance.to_dict()
# create an instance of ClusterVolumeSpecAccessModeAccessibilityRequirements from a dict
cluster_volume_spec_access_mode_accessibility_requirements_from_dict = ClusterVolumeSpecAccessModeAccessibilityRequirements.from_dict(cluster_volume_spec_access_mode_accessibility_requirements_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


