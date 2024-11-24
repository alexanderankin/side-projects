# PluginConfigRootfs


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**type** | **str** |  | [optional] 
**diff_ids** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.models.plugin_config_rootfs import PluginConfigRootfs

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfigRootfs from a JSON string
plugin_config_rootfs_instance = PluginConfigRootfs.from_json(json)
# print the JSON string representation of the object
print(PluginConfigRootfs.to_json())

# convert the object into a dict
plugin_config_rootfs_dict = plugin_config_rootfs_instance.to_dict()
# create an instance of PluginConfigRootfs from a dict
plugin_config_rootfs_from_dict = PluginConfigRootfs.from_dict(plugin_config_rootfs_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


