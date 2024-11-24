# PluginConfigUser


## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**uid** | **int** |  | [optional] 
**gid** | **int** |  | [optional] 

## Example

```python
from docker_client.generated.models.plugin_config_user import PluginConfigUser

# TODO update the JSON string below
json = "{}"
# create an instance of PluginConfigUser from a JSON string
plugin_config_user_instance = PluginConfigUser.from_json(json)
# print the JSON string representation of the object
print(PluginConfigUser.to_json())

# convert the object into a dict
plugin_config_user_dict = plugin_config_user_instance.to_dict()
# create an instance of PluginConfigUser from a dict
plugin_config_user_from_dict = PluginConfigUser.from_dict(plugin_config_user_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


