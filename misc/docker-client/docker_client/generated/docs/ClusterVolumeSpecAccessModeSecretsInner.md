# ClusterVolumeSpecAccessModeSecretsInner

One cluster volume secret entry. Defines a key-value pair that is passed to the plugin. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**key** | **str** | Key is the name of the key of the key-value pair passed to the plugin.  | [optional] 
**secret** | **str** | Secret is the swarm Secret object from which to read data. This can be a Secret name or ID. The Secret data is retrieved by swarm and used as the value of the key-value pair passed to the plugin.  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.cluster_volume_spec_access_mode_secrets_inner import ClusterVolumeSpecAccessModeSecretsInner

# TODO update the JSON string below
json = "{}"
# create an instance of ClusterVolumeSpecAccessModeSecretsInner from a JSON string
cluster_volume_spec_access_mode_secrets_inner_instance = ClusterVolumeSpecAccessModeSecretsInner.from_json(json)
# print the JSON string representation of the object
print(ClusterVolumeSpecAccessModeSecretsInner.to_json())

# convert the object into a dict
cluster_volume_spec_access_mode_secrets_inner_dict = cluster_volume_spec_access_mode_secrets_inner_instance.to_dict()
# create an instance of ClusterVolumeSpecAccessModeSecretsInner from a dict
cluster_volume_spec_access_mode_secrets_inner_from_dict = ClusterVolumeSpecAccessModeSecretsInner.from_dict(cluster_volume_spec_access_mode_secrets_inner_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


