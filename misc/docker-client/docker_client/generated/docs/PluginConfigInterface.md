# PluginConfigInterface

The interface between Docker and the plugin

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**types** | [**List[PluginInterfaceType]**](PluginInterfaceType.md) |  | 
**socket** | **str** |  | 
**protocol_scheme** | **str** | Protocol to use for clients connecting to the plugin. | [optional] 

## Example

```python
from docker_client.generated.models.plugin_config_interface import PluginConfigInterface

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfigInterface from a JSON string
plugin_config_interface_instance = PluginConfigInterface.from_json(json)
# print the JSON string representation of the object
print(PluginConfigInterface.to_json())

# convert the object into a dict
plugin_config_interface_dict = plugin_config_interface_instance.to_dict()
# create an instance of PluginConfigInterface from a dict
plugin_config_interface_from_dict = PluginConfigInterface.from_dict(plugin_config_interface_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


