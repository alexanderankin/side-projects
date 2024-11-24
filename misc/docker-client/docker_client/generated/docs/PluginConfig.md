# PluginConfig

The config of a plugin.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**docker_version** | **str** | Docker Version used to create the plugin | [optional] 
**description** | **str** |  | 
**documentation** | **str** |  | 
**interface** | [**PluginConfigInterface**](PluginConfigInterface.md) |  | 
**entrypoint** | **List[str]** |  | 
**work_dir** | **str** |  | 
**user** | [**PluginConfigUser**](PluginConfigUser.md) |  | [optional] 
**network** | [**PluginConfigNetwork**](PluginConfigNetwork.md) |  | 
**linux** | [**PluginConfigLinux**](PluginConfigLinux.md) |  | 
**propagated_mount** | **str** |  | 
**ipc_host** | **bool** |  | 
**pid_host** | **bool** |  | 
**mounts** | [**List[PluginMount]**](PluginMount.md) |  | 
**env** | [**List[PluginEnv]**](PluginEnv.md) |  | 
**args** | [**PluginConfigArgs**](PluginConfigArgs.md) |  | 
**rootfs** | [**PluginConfigRootfs**](PluginConfigRootfs.md) |  | [optional] 

## Example

```python
from docker_client.generated.models.plugin_config import PluginConfig

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfig from a JSON string
plugin_config_instance = PluginConfig.from_json(json)
# print the JSON string representation of the object
print(PluginConfig.to_json())

# convert the object into a dict
plugin_config_dict = plugin_config_instance.to_dict()
# create an instance of PluginConfig from a dict
plugin_config_from_dict = PluginConfig.from_dict(plugin_config_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


