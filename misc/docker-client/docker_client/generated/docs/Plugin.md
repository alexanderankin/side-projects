# Plugin

A plugin for the Engine API

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**id** | **str** |  | [optional] 
**name** | **str** |  | 
**enabled** | **bool** | True if the plugin is running. False if the plugin is not running, only installed. | 
**settings** | [**PluginSettings**](PluginSettings.md) |  | 
**plugin_reference** | **str** | plugin remote reference used to push/pull the plugin | [optional] 
**config** | [**PluginConfig**](PluginConfig.md) |  | 

## Example

```python
from docker_client.generated.models.plugin import Plugin

# TODO update the JSON string below
json = "{}"
# create an instance of Plugin from a JSON string
plugin_instance = Plugin.from_json(json)
# print the JSON string representation of the object
print(Plugin.to_json())

# convert the object into a dict
plugin_dict = plugin_instance.to_dict()
# create an instance of Plugin from a dict
plugin_from_dict = Plugin.from_dict(plugin_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


