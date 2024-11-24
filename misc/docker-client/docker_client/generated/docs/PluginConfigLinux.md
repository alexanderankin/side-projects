# PluginConfigLinux


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**capabilities** | **List[str]** |  | 
**allow_all_devices** | **bool** |  | 
**devices** | [**List[PluginDevice]**](PluginDevice.md) |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_config_linux import PluginConfigLinux

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfigLinux from a JSON string
plugin_config_linux_instance = PluginConfigLinux.from_json(json)
# print the JSON string representation of the object
print(PluginConfigLinux.to_json())

# convert the object into a dict
plugin_config_linux_dict = plugin_config_linux_instance.to_dict()
# create an instance of PluginConfigLinux from a dict
plugin_config_linux_from_dict = PluginConfigLinux.from_dict(plugin_config_linux_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


