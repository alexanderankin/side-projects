# PluginInterfaceType


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**prefix** | **str** |  | 
**capability** | **str** |  | 
**version** | **str** |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_interface_type import PluginInterfaceType

# TODO update the JSON string below
json = "{}"
# create an instance of PluginInterfaceType from a JSON string
plugin_interface_type_instance = PluginInterfaceType.from_json(json)
# print the JSON string representation of the object
print(PluginInterfaceType.to_json())

# convert the object into a dict
plugin_interface_type_dict = plugin_interface_type_instance.to_dict()
# create an instance of PluginInterfaceType from a dict
plugin_interface_type_from_dict = PluginInterfaceType.from_dict(plugin_interface_type_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


