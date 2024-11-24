# PluginPrivilege

Describes a permission the user has to accept upon installing the plugin. 

## Properties

Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**name** | **str** |  | [optional] 
**description** | **str** |  | [optional] 
**value** | **List[str]** |  | [optional] 

## Example

```python
from docker_client.generated.docker_client.generated.models.plugin_privilege import PluginPrivilege

# TODO update the JSON string below
json = "{}"
# create an instance of PluginPrivilege from a JSON string
plugin_privilege_instance = PluginPrivilege.from_json(json)
# print the JSON string representation of the object
print(PluginPrivilege.to_json())

# convert the object into a dict
plugin_privilege_dict = plugin_privilege_instance.to_dict()
# create an instance of PluginPrivilege from a dict
plugin_privilege_from_dict = PluginPrivilege.from_dict(plugin_privilege_dict)
```
[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


