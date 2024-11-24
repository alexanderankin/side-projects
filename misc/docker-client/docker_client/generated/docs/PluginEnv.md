# PluginEnv


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | 
**description** | **str** |  | 
**settable** | **List[str]** |  | 
**value** | **str** |  | 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_env import PluginEnv

# TODO update the JSON string below
json = "{}"
# create an instance of PluginEnv from a JSON string
plugin_env_instance = PluginEnv.from_json(json)
# print the JSON string representation of the object
print(PluginEnv.to_json())

# convert the object into a dict
plugin_env_dict = plugin_env_instance.to_dict()
# create an instance of PluginEnv from a dict
plugin_env_from_dict = PluginEnv.from_dict(plugin_env_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


