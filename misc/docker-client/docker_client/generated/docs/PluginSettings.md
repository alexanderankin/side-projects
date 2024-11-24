# PluginSettings

Settings that can be modified by users.

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**mounts** | [**List[PluginMount]**](PluginMount.md) |  | 
**env** | **List[str]** |  | 
**args** | **List[str]** |  | 
**devices** | [**List[PluginDevice]**](PluginDevice.md) |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_settings import PluginSettings

# TODO update the JSON string below
json = "{}"
# create an instance of PluginSettings from a JSON string
plugin_settings_instance = PluginSettings.from_json(json)
# print the JSON string representation of the object
print(PluginSettings.to_json())

# convert the object into a dict
plugin_settings_dict = plugin_settings_instance.to_dict()
# create an instance of PluginSettings from a dict
plugin_settings_from_dict = PluginSettings.from_dict(plugin_settings_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


