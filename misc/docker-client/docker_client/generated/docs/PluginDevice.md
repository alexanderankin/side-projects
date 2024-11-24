# PluginDevice


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | 
**description** | **str** |  | 
**settable** | **List[str]** |  | 
**path** | **str** |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_device import PluginDevice

# TODO update the JSON string below
json = "{}"
# create an instance of PluginDevice from a JSON string
plugin_device_instance = PluginDevice.from_json(json)
# print the JSON string representation of the object
print(PluginDevice.to_json())

# convert the object into a dict
plugin_device_dict = plugin_device_instance.to_dict()
# create an instance of PluginDevice from a dict
plugin_device_from_dict = PluginDevice.from_dict(plugin_device_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


