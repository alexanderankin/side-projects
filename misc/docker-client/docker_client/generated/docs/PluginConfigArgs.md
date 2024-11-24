# PluginConfigArgs


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | 
**description** | **str** |  | 
**settable** | **List[str]** |  | 
**value** | **List[str]** |  | 

## Example

```python
from docker_client.generated.models.plugin_config_args import PluginConfigArgs

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfigArgs from a JSON string
plugin_config_args_instance = PluginConfigArgs.from_json(json)
# print the JSON string representation of the object
print(PluginConfigArgs.to_json())

# convert the object into a dict
plugin_config_args_dict = plugin_config_args_instance.to_dict()
# create an instance of PluginConfigArgs from a dict
plugin_config_args_from_dict = PluginConfigArgs.from_dict(plugin_config_args_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


